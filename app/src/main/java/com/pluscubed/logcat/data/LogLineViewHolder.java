package com.pluscubed.logcat.data;

import android.view.View;
import android.widget.TextView;

import com.pluscubed.logcat.R;

/**
 * Improves performance of the ListView.  Watch Romain Guy's video about ListView to learn more.
 *
 * @author nlawson
 */
public class LogLineViewHolder {

    View view;
    TextView levelTextView;
    TextView outputTextView;
    TextView tagTextView;
    TextView pidTextView;
    TextView timestampTextView;

    public LogLineViewHolder(View view) {
        this.view = view;
        pidTextView = (TextView) view.findViewById(R.id.pid_text);
        timestampTextView = (TextView) view.findViewById(R.id.timestamp_text);
        tagTextView = (TextView) view.findViewById(R.id.tag_text);
        levelTextView = (TextView) view.findViewById(R.id.log_level_text);
        outputTextView = (TextView) view.findViewById(R.id.log_output_text);
    }
}
