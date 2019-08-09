package com.pluscubed.logcat.widget.dialogs.adapters;

import android.view.View;

import androidx.annotation.DrawableRes;

public class ControlsItem {
    private @DrawableRes int icon;
    private CharSequence title;
    private View.OnClickListener action;


    public ControlsItem(@DrawableRes int icon, CharSequence title, View.OnClickListener listener) {
        this.icon = icon;
        this.title = title;
        this.action = listener;
    }

    public CharSequence getTitle() {
        return title;
    }

    public @DrawableRes int getIcon() {
        return icon;
    }

    public View.OnClickListener getAction() {
        return action;
    }
}
