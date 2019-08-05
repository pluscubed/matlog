package com.pluscubed.logcat;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;

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

    @ColorInt
    public static int getColorFromAttr(Context context, @AttrRes int attr) {
        TypedValue typedValue = new TypedValue();
        if (context != null && context.getTheme().resolveAttribute(attr, typedValue, true))
            return typedValue.data;
        else
            return Color.RED;
    }
}
