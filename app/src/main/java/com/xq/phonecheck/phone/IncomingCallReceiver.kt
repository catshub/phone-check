package com.xq.phonecheck.phone

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.xq.phonecheck.data.CallerLogStore
import com.xq.phonecheck.identity.CallerIdentityResolver
import com.xq.phonecheck.phone.FloatingCallButton

@Suppress("DEPRECATION")
class IncomingCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return

        if (state != TelephonyManager.EXTRA_STATE_RINGING) {
            FloatingCallButton.hide(context)
            return
        }

        val isScreeningRoleHolder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(android.app.role.RoleManager::class.java)
                ?.isRoleHeld(android.app.role.RoleManager.ROLE_CALL_SCREENING) == true
        } else {
            false
        }

        // Avoid recording/showing the same call twice when the primary screening path is active.
        if (isScreeningRoleHolder) return

        val contactsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
        if (!contactsGranted) return

        val rawNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: return
        val identity = CallerIdentityResolver.resolve(context, rawNumber)
        CallerLogStore.append(context, identity)
        CallerNotification.show(context, identity)
        FloatingCallButton.show(context, identity.number)
    }
}
