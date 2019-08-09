package com.pluscubed.logcat.widget.dialogs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pluscubed.logcat.R;
import com.pluscubed.logcat.widget.dialogs.adapters.ControlsItem;
import com.pluscubed.logcat.widget.dialogs.adapters.DialogControlsAdapter;

import java.util.ArrayList;

/**
 * Created by Snow Volf on 26.08.2017, 21:38
 */

public class SweetContentDialog extends BottomSheetDialog {
    private Context mContext;
    private FrameLayout mContentFrame;
    private TextView mCaption, mContentView;
    private RecyclerView mControllerView;
    private ArrayList<ControlsItem> mControls = new ArrayList<>();
    private boolean mDismissOnTouch = false;

    public SweetContentDialog(@NonNull Context context) {
        super(context);
        mContext = context;
        initContentView();
    }

    private void initContentView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_simple_content, null);
        mCaption = view.findViewById(R.id.content_caption);
        mContentFrame = view.findViewById(R.id.content_frame);
        mContentView = view.findViewById(R.id.content);
        mControllerView = view.findViewById(R.id.list);
        setContentView(view);
    }

    @Override
    public void setTitle(CharSequence title) {
        mCaption.setText(title);
    }

    public void setMessage(int resId) {
        mContentView.setText(resId);
    }

    public void setMessage(StringBuilder sb) {
        mContentView.setText(sb);
    }

    public void setMessage(CharSequence text) {
        mContentView.setText(text);
    }

    public void setMessage(Spanned text){
        mContentView.setText(text);
    }

    public void setView(@LayoutRes int resId){
        setView(LayoutInflater.from(getContext()).inflate(resId, null));
    }

    public void setIcon(@DrawableRes int resId){
       setIcon(AppCompatResources.getDrawable(mContext, resId));
    }

    public void setIcon(Drawable icon){
        mCaption.setCompoundDrawablesRelative(icon, null, null, null);
    }

    public void setView(View view) {
        mContentFrame.removeAllViews();
        mContentFrame.addView(view);
    }

    public void setPositive(@DrawableRes int resId, CharSequence text, View.OnClickListener listener) {
        mControls.add(new ControlsItem(resId, text, listener));
    }

    public void setNegative(@DrawableRes int resId, CharSequence text, View.OnClickListener listener) {
        mControls.add(new ControlsItem(resId, text, listener));
    }

    public void setNeutral(@DrawableRes int resId, CharSequence text, View.OnClickListener listener) {
        mControls.add(new ControlsItem(resId, text, listener));
    }

    public void setDismissOnTouch(boolean dismissOnTouch) {
        this.mDismissOnTouch = dismissOnTouch;
    }

    @Override
    public void show() {
        super.show();
        if (mControls.size() > 0) {
            final DialogControlsAdapter adapter = new DialogControlsAdapter(mControls);
            adapter.setItemClickListener((menuItem, position) -> {
                if (menuItem.getAction() != null) {
                    menuItem.getAction().onClick(null);
                    if (mDismissOnTouch)
                        dismiss();
                } else {
                    dismiss();
                }
            });
            mControllerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mControllerView.setAdapter(adapter);
        }
    }
}
