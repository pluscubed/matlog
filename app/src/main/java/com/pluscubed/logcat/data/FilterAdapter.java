package com.pluscubed.logcat.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pluscubed.logcat.R;
import com.pluscubed.logcat.db.CatlogDBHelper;
import com.pluscubed.logcat.db.FilterItem;

import java.util.List;

import androidx.annotation.NonNull;

public class FilterAdapter extends ArrayAdapter<FilterItem> {

    public FilterAdapter(Context context, List<FilterItem> items) {
        super(context, R.layout.list_item_filter, items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_filter, parent, false);
        }

        final FilterItem filterItem = getItem(position);

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(filterItem.getText());
        // add listener to the delete button
        ImageView button = convertView.findViewById(android.R.id.button1);
        button.setOnClickListener(v -> {
            //delete
            CatlogDBHelper dbHelper = null;
            try {
                dbHelper = new CatlogDBHelper(getContext());
                dbHelper.deleteFilter(filterItem.getId());
            } finally {
                if (dbHelper != null) {
                    dbHelper.close();
                }
            }
            remove(filterItem);
            notifyDataSetChanged();
        });

        return convertView;
    }

}
