package com.xq.phonecheck.phone

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.core.content.ContextCompat
import com.xq.phonecheck.data.CallerLogStore
import com.xq.phonecheck.identity.CallerIdentityResolver
import com.xq.phonecheck.phone.FloatingCallButton

class CallScreeningServiceImpl : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val response = CallScreeningService.CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSilenceCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()

        runCatching {
            val rawNumber = callDetails.handle?.schemeSpecificPart
            val identity = CallerIdentityResolver.resolve(applicationContext, rawNumber)
            CallerLogStore.append(this, identity)

            val notificationAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            if (notificationAllowed) {
                CallerNotification.show(this, identity)
            }
            FloatingCallButton.show(this, identity.number)
        }

        respondToCall(callDetails, response)
    }
}
