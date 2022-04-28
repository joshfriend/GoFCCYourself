@file:Suppress("HasPlatformType")

package com.fueledbycaffeine.gofccyourself.service

import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import android.telecom.TelecomManager
import android.telephony.PhoneNumberUtils
import com.fueledbycaffeine.gofccyourself.ScreeningPreferences
import timber.log.Timber
import java.util.Locale

class StupidCallScreeningService : CallScreeningService() {
  companion object {
    private val log get() = Timber.tag("📞🔫🤖")
  }

  private val prefs by lazy { ScreeningPreferences(this) }

  /**
   * Called when a new incoming or outgoing call is added which is not in the user's contact list.
   *
   * https://issuetracker.google.com/issues/130081372
   */
  override fun onScreenCall(details: Call.Details) {
    if (details.callDirection == Call.Details.DIRECTION_INCOMING) {
      // https://www.fcc.gov/call-authentication
      when (details.callerNumberVerificationStatus) {
        Connection.VERIFICATION_STATUS_NOT_VERIFIED -> log.w("No caller verification was performed for ${details.formattedPhoneNumber}!")
        Connection.VERIFICATION_STATUS_FAILED -> log.e("Caller ${details.formattedPhoneNumber} FAILED verification!")
        Connection.VERIFICATION_STATUS_PASSED -> log.i("Caller ${details.formattedPhoneNumber} is verified...")
      }

      // Always called, even for known contacts (when holding READ_CONTACTS permission, to allow
      // screening spoofed numbers)
      // https://issuetracker.google.com/issues/141363242
      //
      // Call.Details includes caller name info as of R preview 2, but this seems to always be null
      // because the screening service is called before the lookup completes
      // https://issuetracker.google.com/issues/151898484
      //
      // Handle may be null if caller id is blocked.
      val caller = details.handle?.let { getContactName(it.schemeSpecificPart) }

      val rejectDueToVerificationStatus = when (details.callerNumberVerificationStatus) {
        Connection.VERIFICATION_STATUS_FAILED -> prefs.declineAuthenticationFailures
        Connection.VERIFICATION_STATUS_NOT_VERIFIED -> prefs.declineUnauthenticatedCallers
        else -> false
      }

      val rejectDueToUnknownCaller = caller == null && prefs.declineUnknownCallers

      val response = if (rejectDueToVerificationStatus || rejectDueToUnknownCaller) {
        buildRejectionResponse()
      } else {
        buildAcceptResponse()
      }

      respondToCall(details, response)
    }
  }

  private fun buildAcceptResponse(): CallResponse {
    return CallResponse.Builder().build()
  }

  private fun buildRejectionResponse(): CallResponse {
    log.d("Reject? ${prefs.isServiceEnabled}, Notify? ${!prefs.skipCallNotification}, Log? ${!prefs.skipCallLog}")
    return CallResponse.Builder()
      .setDisallowCall(prefs.isServiceEnabled)
      .setRejectCall(prefs.isServiceEnabled)
      // These last two options don't work, calls will not be logged and notifications will not show
      // https://issuetracker.google.com/issues/151859054
      .setSkipNotification(prefs.skipCallNotification)
      .setSkipCallLog(prefs.skipCallLog)
      .build()
  }

  private fun getContactName(phoneNumber: String): String? {
    val uri = Uri.withAppendedPath(
      ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
      Uri.encode(phoneNumber)
    )

    val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
    val cursor = contentResolver.query(uri, projection, null, null, null)
    return cursor?.use {
      when (cursor.moveToFirst()) {
        true -> cursor.getString(0)
        else -> null
      }
    }
  }
}

private val Call.Details.formattedPhoneNumber: String get() {
  return when (val phoneNumber = handle?.schemeSpecificPart) {
    null -> "BLOCKED"
    else -> PhoneNumberUtils.formatNumber(
      phoneNumber,
      Locale.getDefault().country
    )
  }
}
