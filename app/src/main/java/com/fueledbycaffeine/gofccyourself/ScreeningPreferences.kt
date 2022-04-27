package com.fueledbycaffeine.gofccyourself

import android.content.Context
import androidx.preference.PreferenceManager

class ScreeningPreferences(context: Context) {
  companion object {
    private const val PREF_SERVICE_ENABLED = "service_enabled"
    private const val PREF_SKIP_NOTIFICATION = "skip_notification"
    private const val PREF_SKIP_CALL_LOG = "skip_call_log"
    private const val PREF_DECLINE_UNKNOWN_CALLERS = "decline_unknown_callers"
    private const val PREF_DECLINE_UNAUTHENTICATED_CALLERS = "decline_unauthenticated_callers"
    private const val PREF_DECLINE_AUTH_FAILURES = "decline_auth_failures"
  }

  private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

  /**
   * Should the app perform any screening of calls?
   * Defaults to false
   */
  var isServiceEnabled: Boolean
    get() = sharedPrefs.getBoolean(PREF_SERVICE_ENABLED, false)
    set(value) = sharedPrefs.edit().putBoolean(PREF_SERVICE_ENABLED, value).apply()

  /**
   * Should the app hide blocked calls from the dialer app's call logs?
   * Defaults to false
   */
  var skipCallNotification: Boolean
    get() = sharedPrefs.getBoolean(PREF_SKIP_NOTIFICATION, false)
    set(value) = sharedPrefs.edit().putBoolean(PREF_SKIP_NOTIFICATION, value).apply()

  /**
   * Should the app suppress missed call notifications for blocked calls?
   * Defaults to false
   */
  var skipCallLog: Boolean
    get() = sharedPrefs.getBoolean(PREF_SKIP_CALL_LOG, false)
    set(value) = sharedPrefs.edit().putBoolean(PREF_SKIP_CALL_LOG, value).apply()

  /**
   * Should the app decline callers who are not found in the user's contacts?
   * Defaults to false
   */
  var declineUnknownCallers: Boolean
    get() = sharedPrefs.getBoolean(PREF_DECLINE_UNKNOWN_CALLERS, false)
    set(value) = sharedPrefs.edit().putBoolean(PREF_DECLINE_UNKNOWN_CALLERS, value).apply()

  /**
   * Should the app decline callers who did not have SHAKEN/STIR checks performed?
   * Defaults to false
   */
  var declineUnauthenticatedCallers: Boolean
    get() = sharedPrefs.getBoolean(PREF_DECLINE_UNAUTHENTICATED_CALLERS, false)
    set(value) = sharedPrefs.edit().putBoolean(PREF_DECLINE_UNAUTHENTICATED_CALLERS, value).apply()

  /**
   * Should the app decline callers who fail SHAKEN/STIR checks?
   * Defaults to true
   */
  var declineAuthenticationFailures: Boolean
    get() = sharedPrefs.getBoolean(PREF_DECLINE_AUTH_FAILURES, true)
    set(value) = sharedPrefs.edit().putBoolean(PREF_DECLINE_AUTH_FAILURES, value).apply()
}
