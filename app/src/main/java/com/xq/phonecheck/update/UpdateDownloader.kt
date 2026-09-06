package com.xq.phonecheck.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

object UpdateDownloader {
    fun download(context: Context, url: String, version: String) {
        val downloadUrl = GitHubMirror.mirror(url) ?: url
        val fileName = "phone-check-$version.apk"
        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("PhoneCheck $version")
            setDescription("正在通过国内镜像下载更新")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverMetered(true)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }

        context.getSystemService(DownloadManager::class.java)?.enqueue(request)
    }
}
