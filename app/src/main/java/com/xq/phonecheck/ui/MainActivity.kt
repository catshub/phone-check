package com.xq.phonecheck.ui

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.xq.phonecheck.R
import com.xq.phonecheck.data.CallerLogStore
import com.xq.phonecheck.identity.CallerIdentity
import com.xq.phonecheck.identity.NumberRuleStore
import com.xq.phonecheck.identity.UserDirectory
import com.xq.phonecheck.update.UpdateChecker
import com.xq.phonecheck.update.UpdateResult

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var callLogText: TextView
    private lateinit var screeningButton: Button
    private lateinit var directoryInput: com.google.android.material.textfield.TextInputEditText
    private lateinit var rulesInput: com.google.android.material.textfield.TextInputEditText

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            refreshUi()
        }

    private val screeningRoleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshUi()
        }

    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshUi()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        callLogText = findViewById(R.id.callLogText)
        screeningButton = findViewById(R.id.requestScreeningButton)
        directoryInput = findViewById(R.id.directoryInput)
        rulesInput = findViewById(R.id.rulesInput)

        directoryInput.setText(UserDirectory.read(this))
        rulesInput.setText(NumberRuleStore.readText(this))

        findViewById<Button>(R.id.requestPermissionsButton).setOnClickListener {
            requestRequiredPermissions()
        }

        screeningButton.setOnClickListener {
            requestScreeningRole()
        }

        findViewById<Button>(R.id.requestOverlayPermissionButton).setOnClickListener {
            requestOverlayPermission()
        }

        findViewById<Button>(R.id.checkUpdateButton).setOnClickListener {
            checkForUpdate()
        }

        findViewById<Button>(R.id.saveDirectoryButton).setOnClickListener {
            UserDirectory.save(this, directoryInput.text?.toString().orEmpty())
            refreshUi()
        }

        findViewById<Button>(R.id.saveRulesButton).setOnClickListener {
            NumberRuleStore.save(this, rulesInput.text?.toString().orEmpty())
            refreshUi()
        }

        findViewById<Button>(R.id.clearLogButton).setOnClickListener {
            CallerLogStore.clear(this)
            refreshUi()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun requestRequiredPermissions() {
        val missing = requiredPermissions()
            .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
            .toTypedArray()

        if (missing.isEmpty()) {
            refreshUi()
        } else {
            permissionLauncher.launch(missing)
        }
    }

    private fun requiredPermissions(): List<String> {
        val permissions = mutableListOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions
    }

    private fun requestScreeningRole() {
        val roleManager = getSystemService(android.app.role.RoleManager::class.java) ?: return

        if (!roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_CALL_SCREENING)) {
            refreshUi()
            return
        }

        val intent = roleManager.createRequestRoleIntent(
            android.app.role.RoleManager.ROLE_CALL_SCREENING
        )
        screeningRoleLauncher.launch(intent)
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlayPermissionLauncher.launch(intent)
    }

    private fun checkForUpdate() {
        UpdateChecker.check(this) { result ->
            when (result) {
                is UpdateResult.UpdateAvailable -> showUpdateDialog(result.version, result.url)
                UpdateResult.UpToDate -> showToast(getString(R.string.update_up_to_date))
                is UpdateResult.Failure -> showToast(result.message)
            }
        }
    }

    private fun showUpdateDialog(version: String, url: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.update_available_title))
            .setMessage(version)
            .setPositiveButton(getString(R.string.update_open)) { _, _ ->
                runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            }
            .setNegativeButton(getString(R.string.update_cancel), null)
            .show()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun refreshUi() {
        val contacts = hasPermission(Manifest.permission.READ_CONTACTS)
        val phone = hasPermission(Manifest.permission.READ_PHONE_STATE)
        val notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }

        val screeningEnabled = isCallScreeningRoleHolder()
        val permissionSummary = listOf(
            "通讯录 ${if (contacts) "已授权" else "未授权"}",
            "电话状态 ${if (phone) "已授权" else "未授权"}",
            "通知 ${if (notifications) "已授权" else "未授权"}",
            "来电识别角色 ${if (screeningEnabled) "已启用" else "未启用"}",
            "悬浮窗 ${if (Settings.canDrawOverlays(this)) "已授权" else "未授权"}"
        ).joinToString("，")

        statusText.text = permissionSummary
        callLogText.text = CallerLogStore.read(this)
            .takeIf { it.isNotEmpty() }
            ?.joinToString("\n\n") { format(it) }
            ?: getString(R.string.status_ready)
    }

    private fun isCallScreeningRoleHolder(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val roleManager = getSystemService(android.app.role.RoleManager::class.java) ?: return false

        return runCatching {
            roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_CALL_SCREENING)
        }.getOrDefault(false)
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun format(identity: CallerIdentity): String {
        return buildString {
            append(identity.shortLabel)
            append("：")
            append(identity.number)
            identity.displayName?.let { append("，姓名：$it") }
            identity.wechatId?.let { append("，微信：$it") }
            identity.note?.let { append("，备注：$it") }
            append("\n")
            append(identity.reason)
        }
    }
}
