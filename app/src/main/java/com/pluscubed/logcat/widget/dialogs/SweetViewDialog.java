package com.pluscubed.logcat.widget.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pluscubed.logcat.R;


public class SweetViewDialog extends BottomSheetDialog {
    private Context mContext;
    private FrameLayout mContentFrame;
    private TextView mCaption;
    private Button mPositive, mNegative, mNeutral;

    public SweetViewDialog(@NonNull Context context) {
        super(context);
        mContext = context;
        initContentView();
    }

    private void initContentView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_view, null);
        mCaption = view.findViewById(R.id.content_caption);
        mContentFrame = view.findViewById(R.id.content_frame);
        mPositive = view.findViewById(R.id.positive);
        mNegative = view.findViewById(R.id.negative);
        mNeutral = view.findViewById(R.id.neutral);
        setContentView(view);
    }

    @Override
    public void setTitle(CharSequence title) {
        mCaption.setText(title);
    }

    public void setTitle(@StringRes int resId){
        mCaption.setText(resId);
    }

    public void setView(View view) {
        mContentFrame.addView(view);
    }

    public void setPositive(CharSequence text, View.OnClickListener listener) {
        mPositive.setText(text);
        mPositive.setOnClickListener(listener);
    }

    public void setNegative(CharSequence text, View.OnClickListener listener) {
        mNegative.setText(text);
        mNegative.setOnClickListener(listener);
    }

    public void setNeutral(CharSequence text, View.OnClickListener listener) {
        mNeutral.setText(text);
        mNeutral.setOnClickListener(listener);
    }

    public void setPositive(int resId, View.OnClickListener listener) {
        mPositive.setText(resId);
        mPositive.setOnClickListener(listener);
    }

    public void setNegative(int resId, View.OnClickListener listener) {
        mNegative.setText(resId);
        mNegative.setOnClickListener(listener);
    }

    public void setNeutral(int resId, View.OnClickListener listener) {
        mNeutral.setText(resId);
        mNeutral.setOnClickListener(listener);
    }

    @Override
    public void show() {
        super.show();
        mNegative.setVisibility(mNegative.getText().length() == 0 ? View.GONE : View.VISIBLE);
        mPositive.setVisibility(mPositive.getText().length() == 0 ? View.GONE : View.VISIBLE);
        mNeutral.setVisibility(mNeutral.getText().length() == 0 ? View.GONE : View.VISIBLE);
    }
}
