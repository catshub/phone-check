package com.xq.phonecheck.identity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

object ContactDirectory {
    private const val WECHAT_MIME_PREFIX = "com.tencent.mm"

    fun lookup(context: Context, rawNumber: String): CallerIdentity? {
        val number = PhoneNumbers.normalize(rawNumber)
        if (number.isEmpty() || !hasContactsPermission(context)) return null

        val contact = findContact(context, number) ?: return null
        val wechatId = findWechatId(context, contact.contactId)

        return CallerIdentity(
            number = number,
            source = if (wechatId == null) IdentitySource.CONTACT else IdentitySource.CONTACT_WITH_WECHAT,
            displayName = contact.displayName,
            wechatId = wechatId,
            reason = if (wechatId == null) {
                "号码存在于本机通讯录；未找到同步到通讯录的微信资料"
            } else {
                "号码存在于本机通讯录，并读取到微信同步资料"
            }
        )
    }

    private data class ContactMatch(
        val contactId: Long,
        val displayName: String
    )

    private fun findContact(context: Context, number: String): ContactMatch? {
        val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI
            .buildUpon()
            .appendPath(number)
            .build()

        context.contentResolver.query(
            uri,
            arrayOf(
                ContactsContract.PhoneLookup._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME_PRIMARY
            ),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME_PRIMARY)
                if (idIndex >= 0) {
                    val name = if (nameIndex >= 0) cursor.getString(nameIndex) else null
                    return ContactMatch(cursor.getLong(idIndex), name ?: number)
                }
            }
        }
        return null
    }

    private fun findWechatId(context: Context, contactId: Long): String? {
        context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(
                ContactsContract.Data.DATA1,
                ContactsContract.Data.MIMETYPE
            ),
            "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} LIKE ?",
            arrayOf(contactId.toString(), "%$WECHAT_MIME_PREFIX%"),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val dataIndex = cursor.getColumnIndex(ContactsContract.Data.DATA1)
                if (dataIndex >= 0) {
                    val value = cursor.getString(dataIndex)?.trim()
                    if (!value.isNullOrBlank()) return value
                }
            }
        }
        return null
    }

    private fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
    }
}
