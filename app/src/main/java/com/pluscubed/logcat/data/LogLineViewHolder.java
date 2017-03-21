package com.pluscubed.logcat.data;

import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.pluscubed.logcat.R;

/**
 * ViewHolder to show log entries
 *
 * @author nlawson
 */
public class LogLineViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener, View.OnClickListener, View.OnLongClickListener {
    // id for context menu entry
    public static final int CONTEXT_MENU_FILTER_ID = 0;
    public static final int CONTEXT_MENU_COPY_ID = 1;

    final TextView levelTextView;
    final TextView outputTextView;
    final TextView tagTextView;
    final TextView pidTextView;
    final TextView timestampTextView;

    LogLine logLine;

    private final OnClickListener clickListener;

    public LogLineViewHolder(View view, final OnClickListener clickListener) {
        super(view);

        this.clickListener = clickListener;

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        pidTextView = (TextView) view.findViewById(R.id.pid_text);
        timestampTextView = (TextView) view.findViewById(R.id.timestamp_text);
        tagTextView = (TextView) view.findViewById(R.id.tag_text);
        levelTextView = (TextView) view.findViewById(R.id.log_level_text);
        outputTextView = (TextView) view.findViewById(R.id.log_output_text);
    }

    @Override
    public void onClick(View v) {
        clickListener.onClick(v, logLine);
    }

    @Override
    public boolean onLongClick(View v) {
        PopupMenu menu = new PopupMenu(v.getContext(), v);
        menu.getMenu().add(0, CONTEXT_MENU_FILTER_ID, 0, R.string.filter_choice);
        menu.getMenu().add(0, CONTEXT_MENU_COPY_ID, 0, R.string.copy_to_clipboard);
        menu.setOnMenuItemClickListener(LogLineViewHolder.this);
        menu.show();
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return clickListener.onMenuItemClick(item, logLine);
    }

    public interface OnClickListener {
        void onClick(View itemView, LogLine logLine);
        boolean onMenuItemClick(MenuItem item, LogLine logLine);
    }
}
