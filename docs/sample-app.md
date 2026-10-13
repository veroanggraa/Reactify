# Sample App

The `app` module (Android) and `iosApp` (iOS, via `samples-shared`) both run the same demo: an
invite-friends list, a bottom-nav indicator, and a segmented control, switchable from one screen.

`samples-shared` is a Kotlin Multiplatform module holding the demo's UI and state, consumed by
both `app` (wrapped in an Android `Activity`) and `iosApp` (wrapped in a `UIViewController` via
`MainViewController()`), so the demo logic and screens are written once and run natively on both
platforms.
