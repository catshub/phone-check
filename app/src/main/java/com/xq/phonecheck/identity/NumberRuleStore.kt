package com.xq.phonecheck.identity

import android.content.Context

object NumberRuleStore {
    private const val PREFS = "number_rules"
    private const val KEY_RULES = "rules_text"

    fun readText(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_RULES, "") ?: ""
    }

    fun save(context: Context, text: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RULES, text)
            .apply()
    }

    fun apply(context: Context, number: String): String {
        return NumberRules.apply(number, readText(context))
    }
}
