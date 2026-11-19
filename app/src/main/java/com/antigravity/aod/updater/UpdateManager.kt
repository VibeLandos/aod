package com.antigravity.aod.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.antigravity.aod.data.AodPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class GithubRelease(
    val tagName: String,
    val name: String,
    val body: String,
    val downloadUrl: String,
    val assetApiUrl: String,
    val fileName: String,
    val size: Long
)

sealed interface UpdateState {
    object Idle : UpdateState
    object Checking : UpdateState
    data class UpdateAvailable(val release: GithubRelease, val currentVersion: String) : UpdateState
    data class UpToDate(val currentVersion: String) : UpdateState
    data class Downloading(
        val progress: Float,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val release: GithubRelease
    ) : UpdateState
    data class ReadyToInstall(val apkFile: File, val release: GithubRelease) : UpdateState
    data class Error(val message: String) : UpdateState
}

class UpdateManager(private val context: Context) {

    private val prefs = AodPreferences.getInstance(context)

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    fun resetState() {
        _updateState.value = UpdateState.Idle
    }

    suspend fun checkForUpdates(currentVersion: String): UpdateState = withContext(Dispatchers.IO) {
        _updateState.value = UpdateState.Checking
        try {
            val settings = prefs.settingsFlow.value
            val repo = settings.githubRepo.trim().ifEmpty { "VibeLandos/aod" }
            val token = settings.githubToken.trim()

            val apiUrl = "https://api.github.com/repos/$repo/releases/latest"
            val url = URL(apiUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "AOD-Android-App")
                if (token.isNotEmpty()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
            }

            val responseCode = connection.responseCode
            if (responseCode == 404) {
                val errorMsg = if (token.isEmpty()) {
                    "Репозиторий не найден или приватный. Сделайте $repo публичным или укажите GitHub токен в настройках."
                } else {
                    "Релизы в репозитории $repo не найдены (404)."
                }
                val state = UpdateState.Error(errorMsg)
                _updateState.value = state
                return@withContext state
            }

            if (responseCode !in 200..299) {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                val state = UpdateState.Error("Ошибка GitHub API ($responseCode): ${errorBody.take(150)}")
                _updateState.value = state
                return@withContext state
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)

            val tagName = json.optString("tag_name", "").trim()
            val releaseName = json.optString("name", tagName)
            val body = json.optString("body", "Нет описания изменений.")
            val assets = json.optJSONArray("assets")

            if (assets == null || assets.length() == 0) {
                val state = UpdateState.Error("В релизе $tagName нет прикрепленных файлов.")
                _updateState.value = state
                return@withContext state
            }

            var apkAsset: JSONObject? = null
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val assetName = asset.optString("name", "")
                if (assetName.endsWith(".apk", ignoreCase = true)) {
                    apkAsset = asset
                    break
                }
            }

            if (apkAsset == null) {
                val state = UpdateState.Error("В релизе $tagName не найден .apk файл.")
                _updateState.value = state
                return@withContext state
            }

            val apkDownloadUrl = apkAsset.optString("browser_download_url", "")
            val assetApiUrl = apkAsset.optString("url", "")
            val apkFileName = apkAsset.optString("name", "aod-$tagName.apk")
            val apkSize = apkAsset.optLong("size", 0L)

            val release = GithubRelease(
                tagName = tagName,
                name = releaseName,
                body = body,
                downloadUrl = apkDownloadUrl,
                assetApiUrl = assetApiUrl,
                fileName = apkFileName,
                size = apkSize
            )

            val isNewer = isVersionNewer(tagName, currentVersion)
            val resultState = if (isNewer) {
                UpdateState.UpdateAvailable(release, currentVersion)
            } else {
                UpdateState.UpToDate(currentVersion)
            }

            _updateState.value = resultState
            resultState
        } catch (e: Exception) {
            val state = UpdateState.Error("Не удалось проверить обновления: ${e.localizedMessage ?: e.message}")
            _updateState.value = state
            state
        }
    }

    suspend fun downloadAndInstall(release: GithubRelease) = withContext(Dispatchers.IO) {
        try {
            val settings = prefs.settingsFlow.value
            val token = settings.githubToken.trim()

            // Target file in app's external files directory
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.cacheDir
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            val destinationFile = File(downloadDir, release.fileName)
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            // Decide URL & Headers
            val isPrivateWithToken = token.isNotEmpty()
            val initialUrl = if (isPrivateWithToken && release.assetApiUrl.isNotEmpty()) {
                release.assetApiUrl
            } else {
                release.downloadUrl
            }

            downloadFileWithProgress(
                initialUrl = initialUrl,
                destinationFile = destinationFile,
                token = if (isPrivateWithToken) token else null,
                totalSizeHint = release.size
            ) { downloaded, total ->
                val progress = if (total > 0) downloaded.toFloat() / total.toFloat() else 0f
                _updateState.value = UpdateState.Downloading(
                    progress = progress,
                    downloadedBytes = downloaded,
                    totalBytes = total,
                    release = release
                )
            }

            _updateState.value = UpdateState.ReadyToInstall(destinationFile, release)

            // Trigger prompt to install immediately
            withContext(Dispatchers.Main) {
                installApk(destinationFile)
            }
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("Ошибка скачивания: ${e.localizedMessage ?: e.message}")
        }
    }

    private fun downloadFileWithProgress(
        initialUrl: String,
        destinationFile: File,
        token: String?,
        totalSizeHint: Long,
        onProgress: (downloaded: Long, total: Long) -> Unit
    ) {
        var currentUrl = initialUrl
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            var redirects = 0
            while (redirects < 5) {
                val urlObj = URL(currentUrl)
                connection = (urlObj.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 20000
                    readTimeout = 20000
                    setRequestProperty("User-Agent", "AOD-Android-App")
                    if (token != null && currentUrl.contains("api.github.com")) {
                        setRequestProperty("Authorization", "Bearer $token")
                        setRequestProperty("Accept", "application/octet-stream")
                    }
                }

                val code = connection.responseCode
                if (code == HttpURLConnection.HTTP_MOVED_TEMP ||
                    code == HttpURLConnection.HTTP_MOVED_PERM ||
                    code == HttpURLConnection.HTTP_SEE_OTHER ||
                    code == 307 || code == 308
                ) {
                    val newLocation = connection.getHeaderField("Location")
                    connection.disconnect()
                    currentUrl = newLocation
                    redirects++
                } else if (code in 200..299) {
                    break
                } else {
                    throw RuntimeException("Сервер вернул код $code при скачивании APK")
                }
            }

            val finalConn = connection ?: throw RuntimeException("Не удалось установить соединение")
            val contentLength = finalConn.contentLengthLong.let { if (it > 0) it else totalSizeHint }

            inputStream = finalConn.inputStream
            outputStream = FileOutputStream(destinationFile)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalRead = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead
                onProgress(totalRead, contentLength)
            }
            outputStream.flush()
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
            try { outputStream?.close() } catch (_: Exception) {}
            try { connection?.disconnect() } catch (_: Exception) {}
        }
    }

    fun installApk(file: File) {
        if (!file.exists()) {
            _updateState.value = UpdateState.Error("Файл обновления не найден на устройстве")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canInstall = context.packageManager.canRequestPackageInstalls()
            if (!canInstall) {
                // Open unknown sources settings for this app
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                return
            }
        }

        try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("Ошибка запуска установщика: ${e.localizedMessage}")
        }
    }

    companion object {
        fun isVersionNewer(remoteTag: String, currentVersion: String): Boolean {
            val cleanRemote = remoteTag.removePrefix("v").removePrefix("V").trim()
            val cleanCurrent = currentVersion.removePrefix("v").removePrefix("V").trim()

            val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            return false
        }
    }
}
