package com.pluscubed.logcat.util;

import android.app.Activity;

/**
 * Wrapper for fdroid build flavor to build without Crashlytics
 */
public class CrashlyticsWrapper {
    public static void initCrashlytics(Activity activity) {
        // Do nothing
    }
}
