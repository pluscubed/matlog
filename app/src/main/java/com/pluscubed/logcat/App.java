package com.pluscubed.logcat;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.pluscubed.logcat.util.CrashlyticsWrapper;

public class App extends Application {
    private static App instance;
    private SharedPreferences preferences;

    public App() {
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        CrashlyticsWrapper.initCrashlytics(this);
    }

    public static App get() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public SharedPreferences getPreferences() {
        if (preferences == null){
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
        }
        return preferences;
    }
}
