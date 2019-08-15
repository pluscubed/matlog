package com.pluscubed.logcat.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.SwitchCompat;

import com.pluscubed.logcat.App;
import com.pluscubed.logcat.R;

public class AteSwitch extends SwitchCompat {
    public AteSwitch(Context context) {
        super(context);
        init(context);
    }

    public AteSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AteSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        setThumbResource(R.drawable.toggle_switch);
        setTrackResource(R.drawable.ate_track);

        setBackground(null);

        ColorStateList sl = new ColorStateList(new int[][]{{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked}},
                new int[]{Color.parseColor("#757575"),
                        App.getColorFromAttr(context, R.attr.colorAccent)});
        setThumbTintList(sl);
        setTrackTintList(sl);
    }

    @Override
    public boolean isShown() {
        return getParent() != null && getVisibility() == View.VISIBLE;
    }
}
