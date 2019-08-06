package com.pluscubed.logcat.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

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
            case AMOLED:
                theme = R.style.Theme_MatLog_Amoled;
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
        applyAccent(ctx);
    }

    private static void applyAccent(Context ctx){
        int accent;
        switch (Accent.values()[getAccentIndex()]){
            case RED:
                accent = R.style.AccentRed;
                break;
            case PINK:
                accent = R.style.AccentPink;
                break;
            case PURPLE:
                accent = R.style.AccentPurple;
                break;
            case INDIGO:
                accent = R.style.AccentIndigo;
                break;
            case BLUE:
                accent = R.style.AccentBlue;
                break;
            case LBLUE:
                accent = R.style.AccentLBlue;
                break;
            case CYAN:
                accent = R.style.AccentCyan;
                break;
            case TEAL:
                accent = R.style.AccentTeal;
                break;
            case GREEN:
                accent = R.style.AccentGreen;
                break;
            case LGREEN:
                accent = R.style.AccentLGreen;
                break;
            case LIME:
                accent = R.style.AccentLime;
                break;
            case YELLOW:
                accent = R.style.AccentYellow;
                break;
            case AMBER:
                accent = R.style.AccentAmber;
                break;
            case ORANGE:
                accent = R.style.AccentOrange;
                break;
            case DORANGE:
                accent = R.style.AccentDOrange;
                break;
            case BROWN:
                accent = R.style.AccentBrown;
                break;
            case GREY:
                accent = R.style.AccentGrey;
                break;
            case BGREY:
                accent = R.style.AccentBGrey;
                break;
            default:
                accent = R.style.AccentBlue;
                break;
        }
        ctx.getTheme().applyStyle(accent, true);
    }

    /**
     * Get a saved theme number
     */
    private static int getThemeIndex() {
        return Integer.parseInt(App.get().getPreferences().getString("ui.theme", String.valueOf(Theme.LIGHT.ordinal())));
    }

    private static int getAccentIndex() {
        return  Integer.parseInt(App.get().getPreferences().getString("ui.accent",  String.valueOf(Accent.PURPLE.ordinal())));
    }

    public static boolean isLightTheme() {
        return getThemeIndex() == Theme.LIGHT.ordinal();
    }

    public static int resolveNavBarColor(Context context) {
        // Android < Oreo does not have View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR flag
        // so, we need to set it a little bit more darker
        if (isLightTheme() && Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            return App.getColorFromAttr(context, R.attr.colorPrimaryDark);
        }
        return  App.getColorFromAttr(context, R.attr.colorPrimary);
    }

    private static boolean isLightScheme(Context context) {
        ColorScheme scheme = PreferenceHelper.getColorScheme(context);
        return scheme == ColorScheme.Light
                || scheme == ColorScheme.Tmobile
                || scheme == ColorScheme.Att;
    }

    private static boolean isDarkScheme(Context context) {
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
        DARK,
        AMOLED
    }

    private enum Accent{
        RED,
        PINK,
        PURPLE,
        INDIGO,
        BLUE,
        LBLUE,
        CYAN,
        TEAL,
        GREEN,
        LGREEN,
        LIME,
        YELLOW,
        AMBER,
        ORANGE,
        DORANGE,
        BROWN,
        GREY,
        BGREY
    }

}
