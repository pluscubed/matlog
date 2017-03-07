package com.pluscubed.logcat.util;

import android.app.Activity;

import com.crashlytics.android.Crashlytics;
import com.pluscubed.logcat.BuildConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Wrapper for gplay build flavor to initialize Crashlytics.
 */
public class CrashlyticsWrapper {
    public static void initCrashlytics(Activity activity) {
        if (!BuildConfig.DEBUG) {
            Fabric.with(activity, new Crashlytics());
        }
    }
}
