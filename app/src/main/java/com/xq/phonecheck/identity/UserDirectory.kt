package com.xq.phonecheck.identity

import android.content.Context

object UserDirectory {
    private const val PREFS = "caller_directory"
    private const val KEY_DIRECTORY = "directory_text"

    fun read(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_DIRECTORY, "") ?: ""
    }

    fun save(context: Context, text: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DIRECTORY, text)
            .apply()
    }

    fun lookup(context: Context, rawNumber: String): CallerIdentity? {
        val number = PhoneNumbers.normalize(rawNumber)
        if (number.isEmpty()) return null

        val entry = read(context).lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() && !it.startsWith("#") && PhoneNumbers.normalize(it.substringBefore(',').trim()) == number }
            ?: return null

        val fields = entry.split(',').map { it.trim() }
        val wechatId = fields.getOrNull(1)?.takeIf { it.isNotEmpty() }
        val displayName = fields.getOrNull(2)?.takeIf { it.isNotEmpty() } ?: number
        val note = fields.getOrNull(3)?.takeIf { it.isNotEmpty() }

        return CallerIdentity(
            number = number,
            source = IdentitySource.USER_DIRECTORY,
            displayName = displayName,
            wechatId = wechatId,
            note = note,
            reason = "号码由用户导入的授权号码资料库匹配"
        )
    }
}
