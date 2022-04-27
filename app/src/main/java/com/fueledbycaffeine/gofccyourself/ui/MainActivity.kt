package com.fueledbycaffeine.gofccyourself.ui

import android.Manifest
import android.app.role.RoleManager
import android.app.role.RoleManager.ROLE_CALL_SCREENING
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.fueledbycaffeine.gofccyourself.R
import com.fueledbycaffeine.gofccyourself.ScreeningPreferences
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
  companion object {
    private const val REQUEST_ID_BECOME_CALL_SCREENER = 1
    private const val REQUEST_ID_REQUEST_READ_CONTACTS_PERMISSION = 1

    private const val EXTRA_CONTACT_READ_PERMISSION_DENIED = "contact_permission_denied_forever"
  }

  private val roleManager by lazy { getSystemService(RoleManager::class.java) }
  private val prefs by lazy { ScreeningPreferences(this) }

  private val hasCallScreeningRole: Boolean
    get() = roleManager.isRoleHeld(ROLE_CALL_SCREENING)
  private val readContactsPermissionGranted: Boolean
    get() = EasyPermissions.hasPermissions(this, Manifest.permission.READ_CONTACTS)
  private var contactsAccessDeniedForever = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    contactsAccessDeniedForever = savedInstanceState
      ?.getBoolean(EXTRA_CONTACT_READ_PERMISSION_DENIED, false) ?: false

    addUiListeners()
    updateUi()
  }

  public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_ID_BECOME_CALL_SCREENER) {
      updateUi()

      when (resultCode) {
        RESULT_OK -> Timber.i("Role was granted")
        else -> Timber.e("Role was not granted")
      }
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    // Forward results to EasyPermissions
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean(EXTRA_CONTACT_READ_PERMISSION_DENIED, contactsAccessDeniedForever)
  }

  //<editor-fold desc="EasyPermissions.PermissionCallbacks">
  override fun onPermissionsGranted(requestCode: Int, grantedPermissions: List<String>) {
    if (Manifest.permission.READ_CONTACTS in grantedPermissions) {
      requestRole()
    }
  }

  override fun onPermissionsDenied(requestCode: Int, deniedPermissions: List<String>) {
    // only in onPermissionsDenied() is it certain if a permission has been permanently denied
    if (Manifest.permission.READ_CONTACTS in deniedPermissions) {
      contactsAccessDeniedForever = EasyPermissions.permissionPermanentlyDenied(
        this,
        Manifest.permission.READ_CONTACTS
      )
    }
  }
  //</editor-fold>

  private fun requestContactsPermission() {
    if (contactsAccessDeniedForever) {
      try {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", this.packageName, null)
        startActivity(intent)
        Toast.makeText(this,
          R.string.enable_read_contacts_permissions_instructions, Toast.LENGTH_LONG).show()
      } catch (e: ActivityNotFoundException) {
        Toast.makeText(this,
          R.string.enable_read_contacts_permissions_instructions_long, Toast.LENGTH_LONG).show()
      }
    } else {
      requestPermissions(
        arrayOf(Manifest.permission.READ_CONTACTS),
        REQUEST_ID_REQUEST_READ_CONTACTS_PERMISSION
      )
    }
  }

  private fun requestRole() {
    val intent = roleManager.createRequestRoleIntent(ROLE_CALL_SCREENING)
    @Suppress("DEPRECATION")
    startActivityForResult(intent,
      REQUEST_ID_BECOME_CALL_SCREENER
    )
  }

  private fun addUiListeners() {
    activateButton.setOnClickListener { requestContactsPermission() }

    enableSwitch.setOnCheckedChangeListener { _, enabled ->
      prefs.isServiceEnabled = enabled
      updateUi()
    }
    skipNotificationSwitch.setOnCheckedChangeListener { _, skip ->
      prefs.skipCallNotification = skip
      updateUi()
    }
    skipCallLogSwitch.setOnCheckedChangeListener { _, skip ->
      prefs.skipCallLog = skip
      updateUi()
    }
    declineUnknownCallers.setOnCheckedChangeListener { _, skip ->
      prefs.declineUnknownCallers = skip
      updateUi()
    }
    declineAuthenticationFailures.setOnCheckedChangeListener { _, skip ->
      prefs.declineAuthenticationFailures = skip
      updateUi()
    }
    declineUnauthenticatedCallers.setOnCheckedChangeListener { _, skip ->
      prefs.declineUnauthenticatedCallers = skip
      updateUi()
    }
  }

  private fun updateUi() {
    val isInstalled = readContactsPermissionGranted && hasCallScreeningRole
    statusLabel.text = when (isInstalled) {
      true -> getString(R.string.status_activated)
      else -> getString(R.string.status_inactive)
    }
    activateButton.isVisible = isInstalled.not()
    enableSwitch.isVisible = isInstalled
    skipNotificationSwitch.isVisible = isInstalled
    skipCallLogSwitch.isVisible = isInstalled
    declineUnknownCallers.isVisible = isInstalled
    declineAuthenticationFailures.isVisible = isInstalled
    declineUnauthenticatedCallers.isVisible = isInstalled

    enableSwitch.isChecked = prefs.isServiceEnabled

    if (Build.VERSION.SDK_INT > 100) {
      skipNotificationSwitch.isEnabled = prefs.isServiceEnabled
      skipNotificationSwitch.isChecked = prefs.skipCallNotification

      skipCallLogSwitch.isEnabled = prefs.isServiceEnabled
      skipCallLogSwitch.isChecked = prefs.skipCallLog
    } else {
      skipNotificationSwitch.isEnabled = false
      skipNotificationSwitch.isChecked = true
      skipNotificationDescription.visibility = View.VISIBLE

      skipCallLogSwitch.isEnabled = false
      skipCallLogSwitch.isChecked = true
      skipCallLogDescription.visibility = View.VISIBLE
    }


    declineUnknownCallers.isEnabled = prefs.isServiceEnabled
    declineUnknownCallers.isChecked = prefs.declineUnknownCallers

    declineAuthenticationFailures.isEnabled = prefs.isServiceEnabled
    declineAuthenticationFailures.isChecked = prefs.declineAuthenticationFailures

    declineUnauthenticatedCallers.isEnabled = prefs.isServiceEnabled
    declineUnauthenticatedCallers.isChecked = prefs.declineUnauthenticatedCallers
  }
}
