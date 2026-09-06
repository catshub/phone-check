package com.xq.phonecheck.phone

import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import androidx.core.content.ContextCompat
import com.xq.phonecheck.R
import com.xq.phonecheck.ui.WeChatJumpActivity
import android.telephony.TelephonyManager

object FloatingCallButton {
    private var button: Button? = null
    private var phoneStateReceiver: BroadcastReceiver? = null

    fun show(context: Context, phoneNumber: String) {
        if (button != null) return
        if (!Settings.canDrawOverlays(context)) return

        val appContext = context.applicationContext
        val windowManager = appContext.getSystemService(WindowManager::class.java) ?: return

        val newButton = Button(appContext).apply {
            text = appContext.getString(R.string.floating_button_text)
            isAllCaps = false
            setOnClickListener {
                val intent = Intent(appContext, WeChatJumpActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(WeChatJumpActivity.EXTRA_PHONE_NUMBER, phoneNumber)
                }
                runCatching { appContext.startActivity(intent) }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            x = 0
            y = 0
        }

        runCatching {
            windowManager.addView(newButton, params)
            button = newButton
        }

        registerPhoneStateReceiver(appContext)
    }

    fun hide(context: Context) {
        val currentButton = button ?: return
        val appContext = context.applicationContext
        val windowManager = appContext.getSystemService(WindowManager::class.java) ?: return

        runCatching {
            windowManager.removeView(currentButton)
        }
        button = null

        phoneStateReceiver?.let { receiver ->
            runCatching { appContext.unregisterReceiver(receiver) }
        }
        phoneStateReceiver = null
    }

    private fun registerPhoneStateReceiver(context: Context) {
        if (phoneStateReceiver != null) return

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                if (state != TelephonyManager.EXTRA_STATE_RINGING) {
                    hide(receiverContext)
                }
            }
        }

        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        phoneStateReceiver = receiver
    }
}
