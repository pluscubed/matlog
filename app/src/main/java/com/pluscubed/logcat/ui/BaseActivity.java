package com.pluscubed.logcat.ui;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.pluscubed.logcat.util.ThemeWrapper;


/**
 * Created by Snow Volf on 16.07.2019, 21:24
 */

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    /**
     * Ресивер изменения темы
     */
    private final BroadcastReceiver mThemeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SettingsActivity.class.equals(BaseActivity.this.getClass())) {
                finish();
                startActivity(getIntent());
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else recreate();
        }
    };

    public BaseActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Регистрация ресивера
        LocalBroadcastManager.getInstance(this).registerReceiver(mThemeReceiver,
                new IntentFilter("org.openintents.action.REFRESH_THEME"));
        // Применение текущей темы
        ThemeWrapper.applyTheme(this);

        if (ThemeWrapper.isLightTheme()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        getWindow().setNavigationBarColor(ThemeWrapper.resolveNavBarColor(this));
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        // Отписываемся от ресивера
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mThemeReceiver);
        super.onDestroy();
    }
}

