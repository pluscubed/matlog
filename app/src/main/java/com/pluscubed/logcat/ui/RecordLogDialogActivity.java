package com.pluscubed.logcat.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pluscubed.logcat.R;
import com.pluscubed.logcat.helper.DialogHelper;
import com.pluscubed.logcat.helper.PreferenceHelper;
import com.pluscubed.logcat.helper.WidgetHelper;
import com.pluscubed.logcat.util.ThemeWrapper;
import com.pluscubed.logcat.widget.dialogs.SweetViewDialog;

import java.util.Arrays;
import java.util.List;

public class RecordLogDialogActivity extends BaseActivity {

    public static final String EXTRA_QUERY_SUGGESTIONS = "suggestions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fix window background overlay in dialog activities
        getTheme().applyStyle(R.style.DialogOverlay, true);
        showDialog();
    }

    private void showDialog() {
        final String[] suggestions = (getIntent() != null && getIntent().hasExtra(EXTRA_QUERY_SUGGESTIONS))
                ? getIntent().getStringArrayExtra(EXTRA_QUERY_SUGGESTIONS) : new String[]{};

        BottomSheetDialogFragment fragment = ShowRecordLogDialog.newInstance(suggestions);
        fragment.show(getSupportFragmentManager(), "showRecordLogDialog");
    }

    public static class ShowRecordLogDialog extends BottomSheetDialogFragment {

        public static final String QUERY_SUGGESTIONS = "suggestions";

        public static ShowRecordLogDialog newInstance(String[] suggestions) {
            ShowRecordLogDialog dialog = new ShowRecordLogDialog();
            Bundle args = new Bundle();
            args.putStringArray(QUERY_SUGGESTIONS, suggestions);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public void onResume() {
            super.onResume();
            getDialog().setCancelable(false);
            getDialog().setCanceledOnTouchOutside(false);
        }

        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //noinspection ConstantConditions
            final List<String> suggestions = Arrays.asList(getArguments().getStringArray(QUERY_SUGGESTIONS));

            //String logFilename = DialogHelper.createLogFilename();

            String defaultLogLevel = Character.toString(PreferenceHelper.getDefaultLogLevelPreference(getActivity()));
            final StringBuilder queryFilterText = new StringBuilder();
            final StringBuilder logLevelText = new StringBuilder(defaultLogLevel);
            final Activity activity = getActivity();

            View dialogContent = activity.getLayoutInflater().inflate(R.layout.dialog_record_log, null);
            EditText editText = dialogContent.findViewById(R.id.edit_query);
            ImageButton buttonEdit = dialogContent.findViewById(R.id.buttonEdit);
            buttonEdit.setOnClickListener(view -> editText.setEnabled(!editText.isEnabled()));

            SweetViewDialog dialog = new SweetViewDialog(getActivity());
            dialog.setTitle(R.string.record_log);
            dialog.setView(dialogContent);
            dialog.setPositive(android.R.string.ok, view -> {
                if (DialogHelper.isInvalidFilename(editText.getText())) {
                    Toast.makeText(getActivity(), R.string.enter_good_filename, Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    String filename = editText.getText().toString();
                    Runnable runnable = activity::finish;
                    DialogHelper.startRecordingWithProgressDialog(filename,
                            queryFilterText.toString(), logLevelText.toString(), runnable, getActivity());
                }
            });
            dialog.setNegative(android.R.string.cancel, view -> {
                dialog.dismiss();
                getActivity().finish();
            });
            dialog.setNeutral(R.string.text_filter_ellipsis, view -> {
                DialogHelper.showFilterDialogForRecording(getActivity(), queryFilterText.toString(),
                        logLevelText.toString(), suggestions,
                        result -> {
                            queryFilterText.replace(0, queryFilterText.length(), result.getFilterQuery());
                            logLevelText.replace(0, logLevelText.length(), result.getLogLevel());
                        });
            });
            //dialog.show();
            //noinspection ConstantConditions
            DialogHelper.initFilenameInputDialog(editText);

            return dialog;
        }
    }
}
