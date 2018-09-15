package com.pluscubed.logcat.util;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.pluscubed.logcat.BuildConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Wrapper for play build flavor to initialize Crashlytics.
 */
public class CrashlyticsWrapper {
    public static void initCrashlytics(Context context) {
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        Fabric.with(context, crashlyticsKit);
    }
}
