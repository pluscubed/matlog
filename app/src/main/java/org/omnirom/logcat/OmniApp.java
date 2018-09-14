package org.omnirom.logcat;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

public class OmniApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
}
