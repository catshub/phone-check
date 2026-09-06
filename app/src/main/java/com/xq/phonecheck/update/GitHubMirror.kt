package com.xq.phonecheck.update

object GitHubMirror {
    private val mirrors = listOf(
        "https://ghproxy.net/",
        "https://ghfast.top/",
        "https://gh.llkk.cc/"
    )

    fun mirror(url: String): String? {
        if (!url.startsWith("https://github.com/")) return null
        return mirrors.firstOrNull() + url
    }
}
