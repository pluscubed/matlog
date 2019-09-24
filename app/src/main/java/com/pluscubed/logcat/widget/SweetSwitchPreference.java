package com.pluscubed.logcat.widget;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

public class SweetSwitchPreference extends SwitchPreference {
    public SweetSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public SweetSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SweetSwitchPreference(Context context) {
        super(context);
        init();
    }

    private void init(){
//        setWidgetLayoutResource(R.layout.ate_preference_switch_support);
//        setChecked(getPersistedBoolean(false));
    }
}
