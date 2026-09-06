package com.xq.phonecheck.update

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed class UpdateResult {
    object UpToDate : UpdateResult()
    data class UpdateAvailable(val version: String, val url: String) : UpdateResult()
    data class Failure(val message: String) : UpdateResult()
}

object UpdateChecker {
    private const val RELEASE_URL = "https://api.github.com/repos/catshub/phone-check/releases/latest"
    private const val USER_AGENT = "phone-check-android"

    fun check(context: Context, callback: (UpdateResult) -> Unit) {
        val appContext = context.applicationContext
        val mainHandler = Handler(Looper.getMainLooper())

        Thread {
            val result = runCatching {
                val connection = URL(RELEASE_URL).openConnection() as HttpURLConnection
                connection.connectTimeout = 8_000
                connection.readTimeout = 8_000
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                connection.setRequestProperty("User-Agent", USER_AGENT)

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    UpdateResult.Failure("GitHub 返回 $responseCode")
                } else {
                    val body = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(body)
                    val latestVersion = json.optString("tag_name").removePrefix("v")
                    val releaseUrl = json.optString("html_url")
                    val currentVersion = appContext.packageManager
                        .getPackageInfo(appContext.packageName, 0)
                        .versionName.orEmpty()

                    if (latestVersion.isBlank() || releaseUrl.isBlank()) {
                        UpdateResult.Failure("GitHub 没有发布信息")
                    } else if (isNewer(latestVersion, currentVersion)) {
                        UpdateResult.UpdateAvailable(latestVersion, releaseUrl)
                    } else {
                        UpdateResult.UpToDate
                    }
                }
            }.getOrElse {
                UpdateResult.Failure(it.message ?: "检查更新失败")
            }

            mainHandler.post { callback(result) }
        }.start()
    }

    internal fun isNewer(latest: String, current: String): Boolean {
        val latestParts = latest.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val count = maxOf(latestParts.size, currentParts.size)

        for (index in 0 until count) {
            val latestValue = latestParts.getOrElse(index) { 0 }
            val currentValue = currentParts.getOrElse(index) { 0 }
            if (latestValue != currentValue) return latestValue > currentValue
        }
        return false
    }
}
