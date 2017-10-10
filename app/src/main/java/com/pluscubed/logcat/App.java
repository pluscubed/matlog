package com.pluscubed.logcat;

import android.app.Application;
import android.content.Context;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

public class App extends Application {
    private static App instance = null;

    public static App getInstance(){
        if (instance == null){
            instance = new App();
        }
        return instance;
    }

    public static Context getContext(){
        return getInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        Fabric.with(this, crashlyticsKit);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

    }
}
