package com.xq.phonecheck.data

import android.content.Context
import com.xq.phonecheck.identity.CallerIdentity
import com.xq.phonecheck.identity.IdentitySource
import org.json.JSONArray
import org.json.JSONObject

object CallerLogStore {
    private const val PREFS = "caller_log"
    private const val KEY_ITEMS = "items"
    private const val MAX_ITEMS = 100

    @Synchronized
    fun append(context: Context, identity: CallerIdentity) {
        val current = read(context).toMutableList()
        current.add(0, identity)
        save(current.take(MAX_ITEMS), context)
    }

    @Synchronized
    fun read(context: Context): List<CallerIdentity> {
        val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ITEMS, null) ?: return emptyList()

        return runCatching {
            val array = JSONArray(json)
            List(array.length()) { index -> fromJson(array.getJSONObject(index)) }
        }.getOrDefault(emptyList())
    }

    @Synchronized
    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_ITEMS)
            .apply()
    }

    private fun save(items: List<CallerIdentity>, context: Context) {
        val array = JSONArray()
        items.forEach { array.put(toJson(it)) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ITEMS, array.toString())
            .apply()
    }

    private fun toJson(identity: CallerIdentity): JSONObject {
        return JSONObject().apply {
            put("number", identity.number)
            put("source", identity.source.name)
            put("displayName", identity.displayName)
            put("wechatId", identity.wechatId)
            put("note", identity.note)
            put("reason", identity.reason)
        }
    }

    private fun fromJson(item: JSONObject): CallerIdentity {
        return CallerIdentity(
            number = item.optString("number"),
            source = runCatching { IdentitySource.valueOf(item.optString("source")) }
                .getOrDefault(IdentitySource.UNKNOWN),
            displayName = item.optString("displayName").takeIf { it.isNotEmpty() },
            wechatId = item.optString("wechatId").takeIf { it.isNotEmpty() },
            note = item.optString("note").takeIf { it.isNotEmpty() },
            reason = item.optString("reason")
        )
    }
}
