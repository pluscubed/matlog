package com.pluscubed.logcat.util;

import android.content.Context;

/**
 * Wrapper for fdroid build flavor to build without Crashlytics
 */
public class CrashlyticsWrapper {
    public static void initCrashlytics(Context context) {
        // Do nothing
    }
}
