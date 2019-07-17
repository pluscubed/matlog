package com.pluscubed.logcat.util;

import android.app.Activity;
import android.content.Context;

import com.pluscubed.logcat.App;
import com.pluscubed.logcat.R;
import com.pluscubed.logcat.data.ColorScheme;
import com.pluscubed.logcat.helper.PreferenceHelper;

/**
 * Created by Snow Volf on 16.07.2019, 20:40
 */

public abstract class ThemeWrapper {
    /**
     * Apply theme to an Activity
     */
    public static void applyTheme(Activity ctx) {
        int theme;
        switch (Theme.values()[getThemeIndex()]) {
            case LIGHT:
                theme = R.style.Theme_MatLog_Light;
                if (isDarkScheme(ctx)){
                    PreferenceHelper.setColorScheme(ctx, ColorScheme.Light);
                }
                break;
            case DARK:
                theme = R.style.Theme_MatLog;
                if (isLightScheme(ctx)) {
                    PreferenceHelper.setColorScheme(ctx, ColorScheme.Dark);
                }
                break;
            default:
                // Force use the light theme
                theme = R.style.Theme_MatLog_Light;
                break;
        }
        ctx.setTheme(theme);
    }

    /**
     * Get a saved theme number
     */
    private static int getThemeIndex() {
        return Integer.parseInt(App.get().getPreferences().getString("ui.theme", String.valueOf(Theme.LIGHT.ordinal())));
    }

    public static boolean isLightTheme() {
        return getThemeIndex() == Theme.LIGHT.ordinal();
    }

    private static  boolean isLightScheme(Context context) {
        ColorScheme scheme = PreferenceHelper.getColorScheme(context);
        return scheme == ColorScheme.Light
                || scheme == ColorScheme.Tmobile
                || scheme == ColorScheme.Att;
    }

    private static  boolean isDarkScheme(Context context) {
        ColorScheme scheme = PreferenceHelper.getColorScheme(context);
        return  scheme == ColorScheme.Dark
                || scheme == ColorScheme.Verizon
                || scheme == ColorScheme.Sprint;
    }

    /**
     * Provided themes
     */
    public enum Theme {
        LIGHT,
        DARK
    }
}
