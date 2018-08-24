package com.pluscubed.logcat.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseIntArray;

import com.pluscubed.logcat.R;
import com.pluscubed.logcat.data.ColorScheme;
import com.pluscubed.logcat.helper.PreferenceHelper;

public class LogLineAdapterUtil {

    public static final int LOG_WTF = 100; // arbitrary int to signify 'wtf' log level

    private static final int NUM_COLORS = 17;

    private static final SparseIntArray BACKGROUND_COLORS = new SparseIntArray(6);
    private static final SparseIntArray FOREGROUND_COLORS = new SparseIntArray(6);

    static {
        BACKGROUND_COLORS.append(Log.DEBUG, R.color.background_debug);
        BACKGROUND_COLORS.append(Log.ERROR, R.color.background_error);
        BACKGROUND_COLORS.append(Log.INFO, R.color.background_info);
        BACKGROUND_COLORS.append(Log.VERBOSE, R.color.background_verbose);
        BACKGROUND_COLORS.append(Log.WARN, R.color.background_warn);
        BACKGROUND_COLORS.append(LOG_WTF, R.color.background_wtf);
        FOREGROUND_COLORS.append(Log.DEBUG, R.color.foreground_debug);
        FOREGROUND_COLORS.append(Log.ERROR, R.color.foreground_error);
        FOREGROUND_COLORS.append(Log.INFO, R.color.foreground_info);
        FOREGROUND_COLORS.append(Log.VERBOSE, R.color.foreground_verbose);
        FOREGROUND_COLORS.append(Log.WARN, R.color.foreground_warn);
        FOREGROUND_COLORS.append(LOG_WTF, R.color.foreground_wtf);
    }

    public static int getBackgroundColorForLogLevel(Context context, int logLevel) {
        int result = BACKGROUND_COLORS.get(logLevel, android.R.color.black);
        return ContextCompat.getColor(context, result);
    }

    public static int getForegroundColorForLogLevel(Context context, int logLevel) {
        int result = FOREGROUND_COLORS.get(logLevel, android.R.color.primary_text_dark);
        return ContextCompat.getColor(context, result);
    }

    public static synchronized int getOrCreateTagColor(Context context, String tag) {

        int hashCode = (tag == null) ? 0 : tag.hashCode();

        int smear = Math.abs(hashCode) % NUM_COLORS;

        return getColorAt(smear, context);

    }

    private static int getColorAt(int i, Context context) {

        ColorScheme colorScheme = PreferenceHelper.getColorScheme(context);

        int[] colorArray = colorScheme.getTagColors(context);

        return colorArray[i];

    }

    public static boolean logLevelIsAcceptableGivenLogLevelLimit(int logLevel, int logLevelLimit) {

        int minVal;
        switch (logLevel) {

            case Log.VERBOSE:
                minVal = 0;
                break;
            case Log.DEBUG:
                minVal = 1;
                break;
            case Log.INFO:
                minVal = 2;
                break;
            case Log.WARN:
                minVal = 3;
                break;
            case Log.ERROR:
                minVal = 4;
                break;
            case LOG_WTF:
                minVal = 5;
                break;
            default: // e.g. the starting line that says "output of log such-and-such"
                return true;
        }

        return minVal >= logLevelLimit;

    }
}
