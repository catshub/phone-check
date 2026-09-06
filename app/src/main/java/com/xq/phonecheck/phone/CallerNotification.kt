package com.xq.phonecheck.phone

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.xq.phonecheck.R
import com.xq.phonecheck.identity.CallerIdentity
import com.xq.phonecheck.ui.WeChatJumpActivity

object CallerNotification {
    private const val CHANNEL_ID = "caller_identity"
    private const val NOTIFICATION_ID = 2001

    fun show(context: Context, identity: CallerIdentity) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.caller_id_channel),
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)

        val text = buildString {
            append(identity.displayName ?: identity.number)
            identity.wechatId?.let { append(" · 微信：$it") }
            identity.note?.let { append(" · ${identity.note}") }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(identity.shortLabel)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (identity.number.isNotEmpty()) {
            val jumpIntent = Intent(context, WeChatJumpActivity::class.java).apply {
                putExtra(WeChatJumpActivity.EXTRA_PHONE_NUMBER, identity.number)
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getActivity(
                context,
                identity.number.hashCode(),
                jumpIntent,
                flags
            )
            builder.addAction(0, context.getString(R.string.open_wechat), pendingIntent)
        }

        manager.notify(NOTIFICATION_ID, builder.build())
    }
}
