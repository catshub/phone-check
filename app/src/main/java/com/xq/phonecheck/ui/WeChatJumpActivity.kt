package com.xq.phonecheck.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xq.phonecheck.R

class WeChatJumpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val number = intent.getStringExtra(EXTRA_PHONE_NUMBER).orEmpty()
        val launched = if (number.isNotEmpty()) {
            copyNumber(number) && startWeChat()
        } else {
            startWeChat()
        }

        val toastRes = when {
            launched && number.isNotEmpty() -> R.string.wechat_jump_toast
            launched -> R.string.status_ready
            else -> R.string.wechat_not_installed
        }
        Toast.makeText(this, toastRes, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun copyNumber(number: String): Boolean {
        return runCatching {
            val clipboard = getSystemService(ClipboardManager::class.java)
            clipboard?.setPrimaryClip(ClipData.newPlainText("phone_number", number))
        }.isSuccess
    }

    private fun startWeChat(): Boolean {
        val intent = packageManager.getLaunchIntentForPackage(WECHAT_PACKAGE) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { startActivity(intent) }.isSuccess
    }

    companion object {
        private const val WECHAT_PACKAGE = "com.tencent.mm"
        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
    }
}
