package com.pluscubed.logcat;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import com.pluscubed.logcat.util.CrashlyticsWrapper;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CrashlyticsWrapper.initCrashlytics(this);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

    }
}
