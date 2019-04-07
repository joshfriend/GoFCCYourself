_An homage to John Oliver's ["Go FCC Yourself" campaign][ars-article]_

## How it works
This app uses the new [`RoleManager`][role-manager] in Android Q to become the system [`CallScreeningService`][call-screening-service]. This service type has been available since Android 7.0 (Nougat), but was only usable if the app was the current default dialer app.

When the service receives an incoming call, it rejects the call if the number is not in the user's contact list. The call will go straight to voicemail without ringing. It can optionally hide the missed call notification and not list the call in the dialer's call log

The app currently requests the `READ_CONTACTS` permission to work around [a bug][b130081372] that causes all calls to pass through the screening service even though [the docs for `onScreenCall()`][onscreencall] specify that:

> only calls which are not in the user's contacts are passed for screening.

[ars-article]: https://arstechnica.com/tech-policy/2017/05/john-oliver-tackles-net-neutrality-again-crashes-fcc-comments-site-again/
[role-manager]: https://developer.android.com/reference/android/app/role/RoleManager
[call-screening-service]: https://developer.android.com/reference/android/telecom/CallScreeningService
[b130081372]: https://issuetracker.google.com/issues/130081372
[onscreencall]: https://developer.android.com/reference/android/telecom/CallScreeningService#onScreenCall(android.telecom.Call.Details)
