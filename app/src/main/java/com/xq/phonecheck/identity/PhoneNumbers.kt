package com.xq.phonecheck.identity

object PhoneNumbers {
    fun normalize(raw: String?): String {
        if (raw.isNullOrBlank()) return ""

        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return ""

        // Chinese mobile numbers are usually shown as +86xxxx... or 86xxxx....
        return when {
            digits.startsWith("011") -> digits.removePrefix("011")
            digits.startsWith("0086") -> digits.removePrefix("0086")
            digits.length > 11 && digits.startsWith("86") && digits.length == 13 -> digits.removePrefix("86")
            else -> digits
        }
    }

    fun isLikelyChineseMobile(normalized: String): Boolean {
        return normalized.length == 11 && normalized.startsWith("1")
    }
}
