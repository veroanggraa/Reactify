package com.veroanggra.reactify.sample

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

public fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = {
        // The Xcode project generates Info.plist from build settings, which don't support the
        // custom CADisableMinimumFrameDurationOnPhone key Compose checks for ProMotion displays.
        // This is a demo app's convenience, not a correctness issue, so skip the check.
        enforceStrictPlistSanityCheck = false
    },
) { SampleApp() }
