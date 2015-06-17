package com.pluscubed.logcat.ui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.MatrixCursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filter.FilterListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.pluscubed.logcat.LogcatRecordingService;
import com.pluscubed.logcat.R;
import com.pluscubed.logcat.data.ColorScheme;
import com.pluscubed.logcat.data.FilterAdapter;
import com.pluscubed.logcat.data.LogFileAdapter;
import com.pluscubed.logcat.data.LogLine;
import com.pluscubed.logcat.data.LogLineAdapter;
import com.pluscubed.logcat.data.SavedLog;
import com.pluscubed.logcat.data.SearchCriteria;
import com.pluscubed.logcat.data.SendLogDetails;
import com.pluscubed.logcat.data.SenderAppAdapter;
import com.pluscubed.logcat.data.SortedFilterArrayAdapter;
import com.pluscubed.logcat.data.TagAndProcessIdAdapter;
import com.pluscubed.logcat.db.CatlogDBHelper;
import com.pluscubed.logcat.db.FilterItem;
import com.pluscubed.logcat.helper.BuildHelper;
import com.pluscubed.logcat.helper.DialogHelper;
import com.pluscubed.logcat.helper.PreferenceHelper;
import com.pluscubed.logcat.helper.SaveLogHelper;
import com.pluscubed.logcat.helper.ServiceHelper;
import com.pluscubed.logcat.helper.UpdateHelper;
import com.pluscubed.logcat.intents.Intents;
import com.pluscubed.logcat.reader.LogcatReader;
import com.pluscubed.logcat.reader.LogcatReaderLoader;
import com.pluscubed.logcat.util.ArrayUtil;
import com.pluscubed.logcat.util.LogLineAdapterUtil;
import com.pluscubed.logcat.util.StringUtil;
import com.pluscubed.logcat.util.UtilLogger;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LogcatActivity extends AppCompatActivity implements FilterListener {

    private static final int REQUEST_CODE_SETTINGS = 1;

    // how often to check to see if we've gone over the max size
    private static final int UPDATE_CHECK_INTERVAL = 200;

    // how many suggestions to keep in the autosuggestions text
    private static final int MAX_NUM_SUGGESTIONS = 1000;

    // id for context menu entry
    private static final int CONTEXT_MENU_FILTER_ID = 0;
    private static final int CONTEXT_MENU_COPY_ID = 1;

    private static UtilLogger log = new UtilLogger(LogcatActivity.class);

    private View mRootLayout, mainFilenameLayout/*, clearButton, expandButton, pauseButton*/;
    /*private AutoCompleteTextView searchEditText;*/
    private ProgressBar darkProgressBar, lightProgressBar;
    private LogLineAdapter mLogListAdapter;
    private LogReaderAsyncTask mTask;
    /*private ImageView expandButtonImage, pauseButtonImage;*/
    private TextView filenameTextView;
    private View borderView1, borderView2, borderView3, borderView4;
    private ListView mListView;

    private String mSearchingString;
    private int firstVisibleItem = -1;
    private boolean mAutoscrollToBottom = true;
    private boolean collapsedMode;
    private boolean partialSelectMode;
    private List<LogLine> partiallySelectedLogLines = new ArrayList<>(2);
    private Set<String> mSearchSuggestionsSet = new HashSet<>();
    private CursorAdapter mSearchSuggestionsAdapter;
    //private SortedFilterArrayAdapter<String> searchSuggestionsAdapter;

    private String currentlyOpenLog = null;

    private Handler handler = new Handler(Looper.getMainLooper());
    private MenuItem mSearchViewMenuItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logcat);

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogLine logLine = mLogListAdapter.getItem(position);

                if (partialSelectMode) {
                    logLine.setHighlighted(true);
                    partiallySelectedLogLines.add(logLine);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mLogListAdapter.notifyDataSetChanged();
                        }
                    });

                    if (partiallySelectedLogLines.size() == 2) {
                        // last line
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                completePartialSelect();
                            }
                        });
                    }
                } else {
                    logLine.setExpanded(!logLine.isExpanded());
                    mLogListAdapter.notifyDataSetChanged();
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        collapsedMode = !PreferenceHelper.getExpandedByDefaultPreference(this);

        log.d("initial collapsed mode is %s", collapsedMode);

        mSearchSuggestionsAdapter = new SimpleCursorAdapter(this,
                R.layout.simple_dropdown,
                null,
                new String[]{"suggestion"},
                new int[]{android.R.id.text1},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        registerForContextMenu(mListView);
        setUpWidgets();
        setUpAdapter();
        updateBackgroundColor();
        runUpdatesIfNecessaryAndShowInitialMessage();
    }


    private void runUpdatesIfNecessaryAndShowInitialMessage() {

        if (UpdateHelper.areUpdatesNecessary(this)) {
            // show progress dialog while updates are running

            final MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .content(R.string.dialog_loading_updates)
                    .progress(true, 0)
                    .show();

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    UpdateHelper.runUpdatesIfNecessary(LogcatActivity.this);
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    showInitialMessageAndStartupLog();
                }


            }.execute((Void) null);

        } else {
            showInitialMessageAndStartupLog();
        }

    }

    private void addFiltersToSuggestions() {
        CatlogDBHelper dbHelper = null;
        try {
            dbHelper = new CatlogDBHelper(this);

            for (FilterItem filterItem : dbHelper.findFilterItems()) {
                addToAutocompleteSuggestions(filterItem.getText());
            }
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }

    }


    private void showInitialMessageAndStartupLog() {

        Intent intent = getIntent();

        if (intent == null || !intent.hasExtra("filename")) {
            startUpMainLog();
        } else {
            String filename = intent.getStringExtra("filename");
            openLog(filename);
        }

        boolean isFirstRun = PreferenceHelper.getFirstRunPreference(getApplicationContext());
        if (isFirstRun) {

            View view = View.inflate(this, R.layout.intro_dialog, null);
            TextView textView = (TextView) view.findViewById(R.id.first_run_text_view_2);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setLinkTextColor(ColorStateList.valueOf(getResources().getColor(R.color.linkColorBlue)));
            new AlertDialog.Builder(this)
                    .setTitle(R.string.first_run_title)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    PreferenceHelper.setFirstRunPreference(getApplicationContext(), false);
                                    dialog.dismiss();
                                    doAfterInitialMessage(getIntent());
                                }
                            })
                    .setCancelable(false)
                    .setIcon(R.drawable.ic_launcher).show();

        } else {
            doAfterInitialMessage(getIntent());
        }


    }

    private void doAfterInitialMessage(Intent intent) {

        // handle an intent that was sent from an external application

        if (intent != null && Intents.ACTION_LAUNCH.equals(intent.getAction())) {

            String filter = intent.getStringExtra(Intents.EXTRA_FILTER);
            String level = intent.getStringExtra(Intents.EXTRA_LEVEL);

            if (!TextUtils.isEmpty(filter)) {
                silentlySetSearchText(filter);
            }


            if (!TextUtils.isEmpty(level)) {
                CharSequence[] logLevels = getResources().getStringArray(R.array.log_levels_values);
                int logLevelLimit = ArrayUtil.indexOf(logLevels, level.toUpperCase(Locale.US));

                if (logLevelLimit == -1) {
                    String invalidLevel = String.format(getString(R.string.toast_invalid_level), level);
                    Toast.makeText(this, invalidLevel, Toast.LENGTH_LONG).show();
                } else {
                    mLogListAdapter.setLogLevelLimit(logLevelLimit);
                    logLevelChanged();
                }

            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mListView.getCount() > 0) {
            // scroll to bottom, since for some reason it always scrolls to the top, which is annoying
            mListView.setSelection(mListView.getCount() - 1);
        }
    }

    private void restartMainLog() {
        mLogListAdapter.clear();

        startUpMainLog();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        doAfterInitialMessage(intent);

        // launched from the widget or notification
        if (intent != null && !Intents.ACTION_LAUNCH.equals(intent.getAction()) && intent.hasExtra("filename")) {
            String filename = intent.getStringExtra("filename");
            openLog(filename);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        log.d("onActivityResult()");

        // preferences may have changed
        PreferenceHelper.clearCache();

        collapsedMode = !PreferenceHelper.getExpandedByDefaultPreference(getApplicationContext());


        if (requestCode == REQUEST_CODE_SETTINGS && resultCode == RESULT_OK) {
            onSettingsActivityResult(data);
        }
        mLogListAdapter.notifyDataSetChanged();
        updateBackgroundColor();
        updateDisplayedFilename();
    }

    private void onSettingsActivityResult(final Intent data) {
        handler.post(new Runnable() {

            @Override
            public void run() {

                updateBackgroundColor();

                if (data.hasExtra("bufferChanged") && data.getBooleanExtra("bufferChanged", false)
                        && currentlyOpenLog == null) {
                    // log buffer changed, so update list
                    restartMainLog();
                } else {
                    // settings activity returned - text size might have changed, so update list
                    expandOrCollapseAll(false);
                    mLogListAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    private void startUpMainLog() {

        Runnable mainLogRunnable = new Runnable() {

            @Override
            public void run() {
                if (mLogListAdapter != null) {
                    mLogListAdapter.clear();
                }
                mTask = new LogReaderAsyncTask();
                mTask.execute((Void) null);
            }
        };

        if (mTask != null) {
            // do only after current log is depleted, to avoid splicing the streams together
            // (Don't cross the streams!)
            mTask.unpause();
            mTask.setOnFinished(mainLogRunnable);
            mTask.killReader();
            mTask = null;
        } else {
            // no main log currently running; just start up the main log now
            mainLogRunnable.run();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        log.d("onPause() called");

        cancelPartialSelect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log.d("onDestroy() called");

        if (mTask != null) {
            mTask.unpause();
            mTask.killReader();
            mTask = null;
        }
    }

    private void populateSuggestionsAdapter(String query) {
        final MatrixCursor c = new MatrixCursor(new String[]{BaseColumns._ID, "suggestion"});
        List<String> suggestionsForQuery = getSuggestionsForQuery(query);
        for (int i = 0, suggestionsForQuerySize = suggestionsForQuery.size(); i < suggestionsForQuerySize; i++) {
            String suggestion = suggestionsForQuery.get(i);
            c.addRow(new Object[]{i, suggestion});
        }
        mSearchSuggestionsAdapter.changeCursor(c);
    }

    private List<String> getSuggestionsForQuery(String query) {
        List<String> suggestions = new ArrayList<>(mSearchSuggestionsSet);
        Collections.sort(suggestions, String.CASE_INSENSITIVE_ORDER);
        List<String> actualSuggestions = new ArrayList<>();
        if (query != null) {
            for (String suggestion : suggestions) {
                if (suggestion.toLowerCase().startsWith(query.toLowerCase())) {
                    actualSuggestions.add(suggestion);
                }
            }
        }
        return actualSuggestions;
    }

    @Override
    public void onBackPressed() {
        if (mSearchViewMenuItem.isActionViewExpanded()) {
            mSearchViewMenuItem.collapseActionView();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        //used to workaround issue where the search text is cleared on expanding the SearchView
        final boolean[] triggerQuery = new boolean[]{true};

        mSearchViewMenuItem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchViewMenuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (triggerQuery[0]) {
                    log.d("filtering: %s", newText);
                    filter(newText);
                    populateSuggestionsAdapter(newText);
                }
                triggerQuery[0] = true;
                return false;
            }
        });
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                List<String> suggestions = getSuggestionsForQuery(mSearchingString);
                searchView.setQuery(suggestions.get(position), true);
                return false;
            }
        });
        searchView.setSuggestionsAdapter(mSearchSuggestionsAdapter);
        if (mSearchingString != null && !mSearchingString.isEmpty()) {
            triggerQuery[0] = false;
            mSearchViewMenuItem.expandActionView();
            searchView.setQuery(mSearchingString, true);
        }

        boolean showingMainLog = (mTask != null);

        MenuItem mainLogMenuItem = menu.findItem(R.id.menu_main_log);
        MenuItem saveLogMenuItem = menu.findItem(R.id.menu_save_log);
        MenuItem saveAsLogMenuItem = menu.findItem(R.id.menu_save_as_log);

        mainLogMenuItem.setEnabled(!showingMainLog);
        mainLogMenuItem.setVisible(!showingMainLog);
        List<String> bufferNames = PreferenceHelper.getBufferNames(this);

        // change the displayed menu name depending on how many logs are to be shown
        String mainLogTitle;
        switch (bufferNames.size()) {
            case 1:
                mainLogTitle = String.format(getString(R.string.play_x1_log), bufferNames.get(0));
                break;
            case 2:
                mainLogTitle = String.format(getString(R.string.play_x2_log),
                        bufferNames.get(0), bufferNames.get(1));
                break;
            default: // 3
                mainLogTitle = getString(R.string.play_x3_log);
                break;

        }

        mainLogMenuItem.setTitle(mainLogTitle);

        saveLogMenuItem.setEnabled(showingMainLog);
        saveLogMenuItem.setVisible(showingMainLog);

        saveAsLogMenuItem.setEnabled(!showingMainLog);
        saveAsLogMenuItem.setVisible(!showingMainLog);

        boolean recordingInProgress = ServiceHelper.checkIfServiceIsRunning(getApplicationContext(), LogcatRecordingService.class);

        MenuItem recordMenuItem = menu.findItem(R.id.menu_record_log);
        MenuItem stopRecordingMenuItem = menu.findItem(R.id.menu_stop_recording_log);

        recordMenuItem.setEnabled(!recordingInProgress);
        recordMenuItem.setVisible(!recordingInProgress);

        stopRecordingMenuItem.setEnabled(recordingInProgress);
        stopRecordingMenuItem.setVisible(recordingInProgress);

        MenuItem crazyLoggerMenuItem = menu.findItem(R.id.menu_crazy_logger_service);
        crazyLoggerMenuItem.setEnabled(UtilLogger.DEBUG_MODE);
        crazyLoggerMenuItem.setVisible(UtilLogger.DEBUG_MODE);

        MenuItem partialSelectMenuItem = menu.findItem(R.id.menu_partial_select);
        partialSelectMenuItem.setEnabled(!partialSelectMode);
        partialSelectMenuItem.setVisible(!partialSelectMode);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_play_pause:
                pauseOrUnpause(item);
                return true;
            case R.id.menu_expand_all:
                expandOrCollapseAll(true);
                if (collapsedMode) {
                    item.setIcon(R.drawable.ic_expand_more_white_24dp);
                    item.setTitle("Expand All");
                } else {
                    item.setIcon(R.drawable.ic_expand_less_white_24dp);
                    item.setTitle("Collapse All");
                }
                return true;
            case R.id.menu_clear:
                if (mLogListAdapter != null) {
                    mLogListAdapter.clear();
                }
                Toast.makeText(this, R.string.log_cleared, Toast.LENGTH_LONG).show();
                return true;

            case R.id.menu_log_level:
                showLogLevelDialog();
                return true;
            case R.id.menu_open_log:
                showOpenLogDialog();
                return true;
            case R.id.menu_save_log:
            case R.id.menu_save_as_log:
                showSaveLogDialog();
                return true;
            case R.id.menu_record_log:
                showRecordLogDialog();
                return true;
            case R.id.menu_stop_recording_log:
                DialogHelper.stopRecordingLog(this);
                return true;
            case R.id.menu_send_log:
                showSendLogDialog();
                return true;
            case R.id.menu_main_log:
                startUpMainLog();
                return true;
            case R.id.menu_delete_saved_log:
                startDeleteSavedLogsDialog();
                return true;
            case R.id.menu_settings:
                startSettingsActivity();
                return true;
            case R.id.menu_crazy_logger_service:
                ServiceHelper.startOrStopCrazyLogger(this);
                return true;
            case R.id.menu_partial_select:
                startPartialSelectMode();
                return true;
            case R.id.menu_filters:
                showFiltersDialog();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, CONTEXT_MENU_FILTER_ID, 0, R.string.filter_choice);
        menu.add(0, CONTEXT_MENU_COPY_ID, 0, R.string.copy_to_clipboard);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        LogLine logLine = mLogListAdapter.getItem(info.position);
        if (logLine != null) {
            switch (item.getItemId()) {
                case CONTEXT_MENU_COPY_ID:
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                    clipboard.setPrimaryClip(ClipData.newPlainText(null, logLine.getOriginalLine()));
                    Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                    return true;
                case CONTEXT_MENU_FILTER_ID:

                    if (logLine.getProcessId() == -1) {
                        // invalid line
                        return false;
                    }

                    showSearchByDialog(logLine);
                    return true;
            }
        }
        return false;
    }

    private void showSearchByDialog(final LogLine logLine) {
        List<CharSequence> choices = Arrays.<CharSequence>asList(getResources().getStringArray(R.array.filter_choices));
        List<CharSequence> choicesSubtexts = Arrays.<CharSequence>asList(logLine.getTag(), Integer.toString(logLine.getProcessId()));

        int tagColor = LogLineAdapterUtil.getOrCreateTagColor(this, logLine.getTag());

        TagAndProcessIdAdapter textAndSubtextAdapter = new TagAndProcessIdAdapter(this, choices, choicesSubtexts, tagColor, -1);

        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.filter_choice)
                .setIcon(R.drawable.abc_ic_search_api_mtrl_alpha)
                .setSingleChoiceItems(textAndSubtextAdapter, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 0) { // tag
                            // determine the right way to phrase this tag query - e.g.
                            // tag:myTag or tag:"my tag"
                            String tagQuery = (logLine.getTag().contains(" "))
                                    ? ('"' + logLine.getTag() + '"')
                                    : logLine.getTag();
                            silentlySetSearchText(SearchCriteria.TAG_KEYWORD + tagQuery);
                        } else { // which == 1, i.e. process id
                            silentlySetSearchText(SearchCriteria.PID_KEYWORD + logLine.getProcessId());
                        }

                        // put the cursor at the end
                        /*searchEditText.setSelection(searchEditText.length());*/
                        dialog.dismiss();

                    }
                })
                .show();
    }

    private void showRecordLogDialog() {

        // start up the dialog-like activity
        String[] suggestions = ArrayUtil.toArray(new ArrayList<>(mSearchSuggestionsSet), String.class);

        Intent intent = new Intent(LogcatActivity.this, ShowRecordLogDialogActivity.class);
        intent.putExtra(ShowRecordLogDialogActivity.EXTRA_QUERY_SUGGESTIONS, suggestions);

        startActivity(intent);
    }

    private void showFiltersDialog() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("t", "Started async task");
                final List<FilterItem> filters = new ArrayList<>();
                filters.add(FilterItem.create(-1, null)); // dummy for the "add filter" option

                CatlogDBHelper dbHelper = null;
                try {
                    dbHelper = new CatlogDBHelper(LogcatActivity.this);
                    filters.addAll(dbHelper.findFilterItems());
                } finally {
                    if (dbHelper != null) {
                        dbHelper.close();
                    }
                }

                Collections.sort(filters);

                Log.e("t", "finish async task doinbackground");

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        final FilterAdapter filterAdapter = new FilterAdapter(LogcatActivity.this, filters);

                        new AlertDialog.Builder(LogcatActivity.this)
                                .setCancelable(true)
                                .setTitle(R.string.title_filters)
                                .setNegativeButton(android.R.string.cancel, null)
                                .setSingleChoiceItems(filterAdapter, 0, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0) { // dummy 'add filter' item
                                            showAddFilterDialog(filterAdapter);
                                        } else {
                                            // load filter
                                            String text = filterAdapter.getItem(which).getText();
                                            silentlySetSearchText(text);
                                            dialog.dismiss();
                                        }
                                    }
                                })
                                .show();
                    }
                });
            }
        }).start();
    }

    private void showAddFilterDialog(final FilterAdapter filterAdapter) {

        // show a popup to add a new filter text
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final AutoCompleteTextView editText =
                (AutoCompleteTextView) inflater.inflate(R.layout.new_filter_text_view, null, false);

        // show suggestions as the user types
        List<String> suggestions = new ArrayList<>(mSearchSuggestionsSet);
        SortedFilterArrayAdapter<String> suggestionAdapter = new SortedFilterArrayAdapter<>(
                this, R.layout.simple_dropdown, suggestions);
        editText.setAdapter(suggestionAdapter);

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.add_filter)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // dismiss soft keyboard
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        handleNewFilterText(editText.getText().toString(), filterAdapter);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setView(editText)
                .create();

        // ensures that the soft keyboard doesn't weirdly pop up at startup
        //alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // when 'Done' is clicked (i.e. enter button), do the same as when "OK" is clicked
        editText.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // dismiss soft keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                    handleNewFilterText(editText.getText().toString(), filterAdapter);

                    alertDialog.dismiss();
                    return true;
                }
                return false;
            }
        });

        alertDialog.show();

    }

    protected void handleNewFilterText(String text, final FilterAdapter filterAdapter) {
        final String trimmed = text.trim();
        if (!TextUtils.isEmpty(trimmed)) {

            new AsyncTask<Void, Void, FilterItem>() {

                @Override
                protected FilterItem doInBackground(Void... params) {
                    CatlogDBHelper dbHelper = null;
                    try {
                        dbHelper = new CatlogDBHelper(LogcatActivity.this);
                        return dbHelper.addFilter(trimmed);


                    } finally {
                        if (dbHelper != null) {
                            dbHelper.close();
                        }
                    }
                }

                @Override
                protected void onPostExecute(FilterItem filterItem) {
                    super.onPostExecute(filterItem);

                    if (filterItem != null) { // null indicates duplicate
                        filterAdapter.add(filterItem);
                        filterAdapter.sort(FilterItem.DEFAULT_COMPARATOR);
                        filterAdapter.notifyDataSetChanged();

                        addToAutocompleteSuggestions(trimmed);
                    }
                }
            }.execute((Void) null);
        }
    }

    private void startPartialSelectMode() {

        boolean hideHelp = PreferenceHelper.getHidePartialSelectHelpPreference(this);

        if (hideHelp) {
            partialSelectMode = true;
            partiallySelectedLogLines.clear();
            Toast.makeText(this, R.string.toast_started_select_partial, Toast.LENGTH_SHORT).show();
        } else {

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View helpView = inflater.inflate(R.layout.partial_select_help, null);
            // don't show the scroll bar
            helpView.setVerticalScrollBarEnabled(false);
            helpView.setHorizontalScrollBarEnabled(false);
            final CheckBox checkBox = (CheckBox) helpView.findViewById(android.R.id.checkbox);

            new AlertDialog.Builder(this)
                    .setTitle(R.string.menu_title_partial_select)
                    .setCancelable(true)
                    .setView(helpView)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            partialSelectMode = true;
                            partiallySelectedLogLines.clear();
                            Toast.makeText(LogcatActivity.this, R.string.toast_started_select_partial, Toast.LENGTH_SHORT).show();

                            if (checkBox.isChecked()) {
                                // hide this help dialog in the future
                                PreferenceHelper.setHidePartialSelectHelpPreference(LogcatActivity.this, true);
                            }

                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    private void startSettingsActivity() {

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SETTINGS);
    }

    private void expandOrCollapseAll(boolean change) {

        collapsedMode = change ? !collapsedMode : collapsedMode;

        int oldFirstVisibleItem = firstVisibleItem;

        for (LogLine logLine : mLogListAdapter.getTrueValues()) {
            if (logLine != null) {
                logLine.setExpanded(!collapsedMode);
            }
        }

       /* expandButtonImage.setImageResource(
                collapsedMode ? R.drawable.ic_menu_more_32 : R.drawable.ic_menu_less_32);*/

        mLogListAdapter.notifyDataSetChanged();

        // ensure that we either stay autoscrolling at the bottom of the list...

        if (mAutoscrollToBottom) {

            mListView.setSelection(mListView.getCount() - 1);

            // ... or that whatever was the previous first visible item is still the current first 
            // visible item after expanding/collapsing

        } else if (oldFirstVisibleItem != -1) {

            mListView.setSelection(oldFirstVisibleItem);
        }


    }


    private void startDeleteSavedLogsDialog() {

        if (!SaveLogHelper.checkSdCard(this)) {
            return;
        }

        List<CharSequence> filenames = new ArrayList<CharSequence>(SaveLogHelper.getLogFilenames());

        if (filenames.isEmpty()) {
            Toast.makeText(this, R.string.no_saved_logs, Toast.LENGTH_SHORT).show();
            return;
        }

        final CharSequence[] filenameArray = ArrayUtil.toArray(filenames, CharSequence.class);

        final LogFileAdapter dropdownAdapter = new LogFileAdapter(
                this, filenames, -1, true);

        final TextView messageTextView = new TextView(LogcatActivity.this);
        messageTextView.setText(R.string.select_logs_to_delete);
        messageTextView.setPadding(3, 3, 3, 3);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.manage_saved_logs)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.delete_all, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean[] allChecked = new boolean[dropdownAdapter.getCount()];

                        for (int i = 0; i < allChecked.length; i++) {
                            allChecked[i] = true;
                        }
                        verifyDelete(filenameArray, allChecked, dialog);

                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        verifyDelete(filenameArray, dropdownAdapter.getCheckedItems(), dialog);

                    }
                })
                .setView(messageTextView)
                .setSingleChoiceItems(dropdownAdapter, 0, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dropdownAdapter.checkOrUncheck(which);

                    }
                });

        builder.show();

    }

    protected void verifyDelete(final CharSequence[] filenameArray,
                                final boolean[] checkedItems, final DialogInterface parentDialog) {

        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(this);

        int deleteCount = 0;

        for (boolean checkedItem : checkedItems) {
            if (checkedItem) {
                deleteCount++;
            }
        }


        final int finalDeleteCount = deleteCount;

        if (finalDeleteCount > 0) {

            builder.setTitle(R.string.delete_saved_log)
                    .setCancelable(true)
                    .setMessage(String.format(getText(R.string.are_you_sure).toString(), finalDeleteCount))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // ok, delete

                            for (int i = 0; i < checkedItems.length; i++) {
                                if (checkedItems[i]) {
                                    SaveLogHelper.deleteLogIfExists(filenameArray[i].toString());
                                }
                            }

                            String toastText = String.format(getText(R.string.files_deleted).toString(), finalDeleteCount);
                            Toast.makeText(LogcatActivity.this, toastText, Toast.LENGTH_SHORT).show();

                            dialog.dismiss();
                            parentDialog.dismiss();

                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }


    }

    private void showSendLogDialog() {

        CharSequence[] items = new CharSequence[]{getText(R.string.as_attachment), getText(R.string.as_text)};

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View includeDeviceInfoView = inflater.inflate(R.layout.include_device_info, null, false);
        final CheckBox includeDeviceInfoCheckBox = (CheckBox) includeDeviceInfoView.findViewById(android.R.id.checkbox);

        // allow user to choose whether or not to include device info in report, use preferences for persistence
        includeDeviceInfoCheckBox.setChecked(PreferenceHelper.getIncludeDeviceInfoPreference(this));
        includeDeviceInfoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceHelper.setIncludeDeviceInfoPreference(LogcatActivity.this, isChecked);
            }
        });

        new AlertDialog.Builder(this)
                .setTitle(R.string.send_log_title)
                .setView(includeDeviceInfoView)
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showSendLogToWhichAppDialogue(which == 1, includeDeviceInfoCheckBox.isChecked());
                        dialog.dismiss();
                    }
                })
                .show();

    }

    private void showSendLogToWhichAppDialogue(final boolean asText, final boolean includeDeviceInfo) {

        if (!(currentlyOpenLog == null && asText) && !SaveLogHelper.checkSdCard(this)) {
            // if asText is false, then we need to check to make sure we can access the sdcard
            return;
        }

        String title = getString(asText ? R.string.send_as_text : R.string.send_as_attachment);

        // determine the attachment type
        SendLogDetails.AttachmentType attachmentType = asText
                ? SendLogDetails.AttachmentType.None
                : (includeDeviceInfo
                ? SendLogDetails.AttachmentType.Zip
                : SendLogDetails.AttachmentType.Text);

        final SenderAppAdapter senderAppAdapter = new SenderAppAdapter(this, asText, attachmentType);

        new AlertDialog.Builder(LogcatActivity.this)
                .setTitle(title)
                .setCancelable(true)
                .setSingleChoiceItems(senderAppAdapter, -1, new DialogInterface.OnClickListener() {

                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                        sendLogToTargetApp(asText, includeDeviceInfo, senderAppAdapter, which);
                    }
                })
                .show();


    }

    protected void sendLogToTargetApp(final boolean asText, final boolean includeDeviceInfo,
                                      final SenderAppAdapter senderAppAdapter, final int which) {

        final AlertDialogWrapper.Builder getBodyProgressDialog = new AlertDialogWrapper.Builder(this);
        getBodyProgressDialog.setCancelable(false);

        // do in the background to avoid jank
        AsyncTask<Void, Void, SendLogDetails> getBodyTask = new AsyncTask<Void, Void, SendLogDetails>() {

            private MaterialDialog mDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (asText || currentlyOpenLog == null || includeDeviceInfo) {

                    getBodyProgressDialog.setTitle(R.string.dialog_please_wait);
                    getBodyProgressDialog.setMessage(getString(R.string.dialog_compiling_log));
                    mDialog = (MaterialDialog) getBodyProgressDialog.show();
                }
            }

            @Override
            protected SendLogDetails doInBackground(Void... params) {
                return getSendLogDetailsInBackground(asText, includeDeviceInfo);
            }

            @Override
            protected void onPostExecute(SendLogDetails sendLogDetails) {
                super.onPostExecute(sendLogDetails);

                senderAppAdapter.respondToClick(which, sendLogDetails.getSubject(), sendLogDetails.getBody(),
                        sendLogDetails.getAttachmentType(), sendLogDetails.getAttachment());
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }
        };
        getBodyTask.execute((Void) null);

    }

    private SendLogDetails getSendLogDetailsInBackground(boolean asText, boolean includeDeviceInfo) {
        SendLogDetails sendLogDetails = new SendLogDetails();
        StringBuilder body = new StringBuilder();

        List<File> files = new ArrayList<>();

        if (!asText) {
            if (currentlyOpenLog != null) { // use saved log file
                files.add(SaveLogHelper.getFile(currentlyOpenLog));
            } else { // create a temp file to hold the current, unsaved log
                File tempLogFile = SaveLogHelper.saveTemporaryFile(this,
                        SaveLogHelper.TEMP_LOG_FILENAME, null, getCurrentLogAsListOfStrings());
                files.add(tempLogFile);
            }
        }

        if (includeDeviceInfo) {
            // include device info
            String deviceInfo = BuildHelper.getBuildInformationAsString();
            if (asText) {
                // append to top of body
                body.append(deviceInfo).append('\n');
            } else {
                // or create as separate file called device.txt
                File tempFile = SaveLogHelper.saveTemporaryFile(this,
                        SaveLogHelper.TEMP_DEVICE_INFO_FILENAME, deviceInfo, null);
                files.add(tempFile);
            }
        }

        if (asText) {
            body.append(getCurrentLogAsCharSequence());
        }

        sendLogDetails.setBody(body.toString());
        sendLogDetails.setSubject(getString(R.string.subject_log_report));

        // either zip up multiple files or just attach the one file
        switch (files.size()) {
            case 0: // no attachments
                sendLogDetails.setAttachmentType(SendLogDetails.AttachmentType.None);
                break;
            case 1: // one plaintext file attachment
                sendLogDetails.setAttachmentType(SendLogDetails.AttachmentType.Text);
                sendLogDetails.setAttachment(files.get(0));
                break;
            default: // 2 files - need to zip them up
                File zipFile = SaveLogHelper.saveTemporaryZipFile(SaveLogHelper.TEMP_ZIP_FILENAME, files);
                File tmpDirectory = SaveLogHelper.getTempDirectory();
                for (File file : files) {
                    // delete original files
                    if (file.getParentFile().equals(tmpDirectory)) { // only delete temporary files
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
                sendLogDetails.setAttachmentType(SendLogDetails.AttachmentType.Zip);
                sendLogDetails.setAttachment(zipFile);
                break;
        }

        return sendLogDetails;
    }

    private List<CharSequence> getCurrentLogAsListOfStrings() {

        List<CharSequence> result = new ArrayList<>(mLogListAdapter.getCount());

        for (int i = 0; i < mLogListAdapter.getCount(); i++) {
            result.add(mLogListAdapter.getItem(i).getOriginalLine());
        }

        return result;
    }

    private CharSequence getCurrentLogAsCharSequence() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < mLogListAdapter.getCount(); i++) {
            stringBuilder.append(mLogListAdapter.getItem(i).getOriginalLine()).append('\n');
        }

        return stringBuilder;
    }

    private void showSaveLogDialog() {

        if (!SaveLogHelper.checkSdCard(this)) {
            return;
        }

        final EditText editText = DialogHelper.createEditTextForFilenameSuggestingDialog(this);

        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {


                if (DialogHelper.isInvalidFilename(editText.getText())) {
                    Toast.makeText(LogcatActivity.this, R.string.enter_good_filename, Toast.LENGTH_SHORT).show();
                } else {
                    String filename = editText.getText().toString();
                    saveLog(filename);
                }


                dialog.dismiss();

            }
        };

        DialogHelper.showFilenameSuggestingDialog(this, editText, onClickListener, null, null, R.string.save_log);
    }

    private void savePartialLog(final String filename, LogLine first, LogLine last) {

        final List<CharSequence> logLines = new ArrayList<>(mLogListAdapter.getCount());

        // filter based on first and last
        boolean started = false;
        boolean foundLast = false;
        for (int i = 0; i < mLogListAdapter.getCount(); i++) {
            LogLine logLine = mLogListAdapter.getItem(i);
            if (logLine == first) {
                started = true;
            }
            if (started) {
                logLines.add(logLine.getOriginalLine());
            }
            if (logLine == last) {
                foundLast = true;
                break;
            }
        }

        if (!foundLast || logLines.isEmpty()) {
            Toast.makeText(this, R.string.toast_invalid_selection, Toast.LENGTH_LONG).show();
            cancelPartialSelect();
            return;
        }

        AsyncTask<Void, Void, Boolean> saveTask = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                SaveLogHelper.deleteLogIfExists(filename);
                return SaveLogHelper.saveLog(logLines, filename);

            }

            @Override
            protected void onPostExecute(Boolean successfullySavedLog) {

                super.onPostExecute(successfullySavedLog);

                if (successfullySavedLog) {
                    Toast.makeText(getApplicationContext(), R.string.log_saved, Toast.LENGTH_SHORT).show();
                    openLog(filename);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.unable_to_save_log, Toast.LENGTH_LONG).show();
                }

                cancelPartialSelect();
            }


        };

        saveTask.execute((Void) null);

    }

    private void saveLog(final String filename) {

        // do in background to avoid jankiness

        final List<CharSequence> logLines = getCurrentLogAsListOfStrings();

        AsyncTask<Void, Void, Boolean> saveTask = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                SaveLogHelper.deleteLogIfExists(filename);
                return SaveLogHelper.saveLog(logLines, filename);

            }

            @Override
            protected void onPostExecute(Boolean successfullySavedLog) {

                super.onPostExecute(successfullySavedLog);

                if (successfullySavedLog) {
                    Toast.makeText(getApplicationContext(), R.string.log_saved, Toast.LENGTH_SHORT).show();
                    openLog(filename);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.unable_to_save_log, Toast.LENGTH_LONG).show();
                }
            }


        };

        saveTask.execute((Void) null);

    }

    private void showOpenLogDialog() {

        if (!SaveLogHelper.checkSdCard(this)) {
            return;
        }

        final List<CharSequence> filenames = new ArrayList<CharSequence>(SaveLogHelper.getLogFilenames());

        if (filenames.isEmpty()) {
            Toast.makeText(this, R.string.no_saved_logs, Toast.LENGTH_SHORT).show();
            return;
        }


        int logToSelect = currentlyOpenLog != null ? filenames.indexOf(currentlyOpenLog) : -1;

        ArrayAdapter<CharSequence> dropdownAdapter = new LogFileAdapter(
                this, filenames, logToSelect, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.open_log)
                .setCancelable(true)
                .setSingleChoiceItems(dropdownAdapter, logToSelect == -1 ? 0 : logToSelect, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String filename = filenames.get(which).toString();
                        openLog(filename);

                    }
                });

        builder.show();

    }

    private void openLog(final String filename) {

        // do in background to avoid jank

        final AsyncTask<Void, Void, List<LogLine>> openFileTask = new AsyncTask<Void, Void, List<LogLine>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                resetDisplayedLog(filename);

                showProgressBar();
            }

            @Override
            protected List<LogLine> doInBackground(Void... params) {

                // remove any lines at the beginning if necessary
                final int maxLines = PreferenceHelper.getDisplayLimitPreference(LogcatActivity.this);
                SavedLog savedLog = SaveLogHelper.openLog(filename, maxLines);
                List<String> lines = savedLog.getLogLines();
                List<LogLine> logLines = new ArrayList<>();
                for (String line : lines) {
                    logLines.add(LogLine.newLogLine(line, !collapsedMode));
                }

                // notify the user if the saved file was truncated
                if (savedLog.isTruncated()) {
                    handler.post(new Runnable() {
                        public void run() {
                            String toastText = String.format(getString(R.string.toast_log_truncated), maxLines);
                            Toast.makeText(LogcatActivity.this, toastText, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                return logLines;
            }

            @Override
            protected void onPostExecute(List<LogLine> logLines) {
                super.onPostExecute(logLines);
                hideProgressBar();

                for (LogLine logLine : logLines) {
                    mLogListAdapter.addWithFilter(logLine, "");
                    addToAutocompleteSuggestions(logLine);

                }

                // scroll to bottom
                mListView.setSelection(mListView.getCount() - 1);
            }
        };

        // if the main log task is running, we can only run AFTER it's been canceled

        if (mTask != null) {
            mTask.setOnFinished(new Runnable() {

                @Override
                public void run() {
                    openFileTask.execute((Void) null);

                }
            });
            mTask.unpause();
            mTask.killReader();
            mTask = null;
        } else {
            // main log not running; just open in this thread
            openFileTask.execute((Void) null);
        }


    }

    private void hideProgressBar() {
        darkProgressBar.setVisibility(View.GONE);
        lightProgressBar.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        ColorScheme colorScheme = PreferenceHelper.getColorScheme(LogcatActivity.this);
        darkProgressBar.setVisibility(colorScheme.isUseLightProgressBar() ? View.GONE : View.VISIBLE);
        lightProgressBar.setVisibility(colorScheme.isUseLightProgressBar() ? View.VISIBLE : View.GONE);
    }


    private void resetDisplayedLog(String filename) {

        mLogListAdapter.clear();
        currentlyOpenLog = filename;
        collapsedMode = !PreferenceHelper.getExpandedByDefaultPreference(getApplicationContext());
        /*clearButton.setVisibility(filename == null? View.VISIBLE : View.GONE);
        pauseButton.setVisibility(filename == null? View.VISIBLE : View.GONE);
        pauseButtonImage.setImageResource(R.drawable.ic_media_pause);
        expandButtonImage.setImageResource(
                collapsedMode ? R.drawable.ic_menu_more_32 : R.drawable.ic_menu_less_32);*/
        addFiltersToSuggestions(); // filters are what initial populate the suggestions
        updateDisplayedFilename();
        resetFilter();

    }

    private void updateDisplayedFilename() {
        mainFilenameLayout.setVisibility(currentlyOpenLog != null ? View.VISIBLE : View.GONE);
        if (currentlyOpenLog != null) {

            filenameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, PreferenceHelper.getTextSizePreference(this) + 2);
            ColorScheme colorScheme = PreferenceHelper.getColorScheme(this);
            borderView1.setBackgroundColor(colorScheme.getForegroundColor(this));
            borderView2.setBackgroundColor(colorScheme.getForegroundColor(this));
            borderView3.setBackgroundColor(colorScheme.getForegroundColor(this));
            borderView4.setBackgroundColor(colorScheme.getForegroundColor(this));
            filenameTextView.setTextColor(colorScheme.getForegroundColor(this));
            filenameTextView.setBackgroundColor(colorScheme.getBubbleBackgroundColor(this));
            filenameTextView.setText(currentlyOpenLog);
        }

    }

    private void resetFilter() {

        String defaultLogLevel = Character.toString(PreferenceHelper.getDefaultLogLevelPreference(this));
        CharSequence[] logLevels = getResources().getStringArray(R.array.log_levels_values);
        int logLevelLimit = ArrayUtil.indexOf(logLevels, defaultLogLevel);
        mLogListAdapter.setLogLevelLimit(logLevelLimit);
        logLevelChanged();

        // silently change edit text without invoking filtering
        /*searchEditText.removeTextChangedListener(this);
        searchEditText.setText("");
        searchEditText.addTextChangedListener(this);*/

    }

    private void showLogLevelDialog() {

        String[] logLevels = getResources().getStringArray(R.array.log_levels);

        // put the word "default" after whatever the default log level is
        String defaultLogLevel = Character.toString(PreferenceHelper.getDefaultLogLevelPreference(this));
        int index = ArrayUtil.indexOf(getResources().getStringArray(R.array.log_levels_values), defaultLogLevel);

        logLevels[index] = logLevels[index] + " " + getString(R.string.default_in_parens);

        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(this);

        builder.setTitle(R.string.log_level)
                .setCancelable(true)
                .setSingleChoiceItems(logLevels, mLogListAdapter.getLogLevelLimit(), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mLogListAdapter.setLogLevelLimit(which);
                        logLevelChanged();
                        dialog.dismiss();

                    }
                });

        builder.show();
    }

    private void setUpWidgets() {

        /*searchEditText = (AutoCompleteTextView) findViewById(R.id.main_edit_text);
        searchEditText.addTextChangedListener(this);
        searchEditText.setOnEditorActionListener(this);
        searchEditText.setOnClickListener(this);

        searchEditText.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }
        });*/


        /*searchEditText.setAdapter(searchSuggestionsAdapter);*/

        darkProgressBar = (ProgressBar) findViewById(R.id.main_dark_progress_bar);
        lightProgressBar = (ProgressBar) findViewById(R.id.main_light_progress_bar);

        mRootLayout = findViewById(R.id.main_background);

        /*clearButton = findViewById(R.id.main_clear_button);
        expandButton = findViewById(R.id.main_more_button);
        pauseButton = findViewById(R.id.main_pause_button);
        expandButtonImage = (ImageView) findViewById(R.id.main_expand_button_image);
        pauseButtonImage = (ImageView) findViewById(R.id.main_pause_button_image);
*/

        /*for (View view : new View[]{clearButton, expandButton, pauseButton}) {
            view.setOnClickListener(this);
        }
        clearButton.setOnLongClickListener(this);*/

        filenameTextView = (TextView) findViewById(R.id.main_filename_text_view);
        mainFilenameLayout = findViewById(R.id.main_filename_linear_layout);
        borderView1 = findViewById(R.id.main_border_view_1);
        borderView2 = findViewById(R.id.main_border_view_2);
        borderView3 = findViewById(R.id.main_border_view_3);
        borderView4 = findViewById(R.id.main_border_view_4);

    }

    private void setUpAdapter() {

        mLogListAdapter = new LogLineAdapter(this, R.layout.list_item_logcat, new ArrayList<LogLine>());

        mListView.setAdapter(mLogListAdapter);

        mListView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                // update what the first viewable item is
                LogcatActivity.this.firstVisibleItem = firstVisibleItem;

                // if the bottom of the list isn't visible anymore, then stop autoscrolling
                mAutoscrollToBottom = (firstVisibleItem + visibleItemCount == totalItemCount);

                // only hide the fast scroll if we're unpaused and at the bottom of the list
                boolean enableFastScroll = mTask == null || mTask.isPaused() || !mAutoscrollToBottom;
                mListView.setFastScrollEnabled(enableFastScroll);

            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // do nothing

            }
        });
    }

    private void completePartialSelect() {

        if (!SaveLogHelper.checkSdCard(this)) {
            cancelPartialSelect();
            return;
        }

        final EditText editText = DialogHelper.createEditTextForFilenameSuggestingDialog(this);

        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {


                if (DialogHelper.isInvalidFilename(editText.getText())) {
                    cancelPartialSelect();
                    Toast.makeText(LogcatActivity.this, R.string.enter_good_filename, Toast.LENGTH_SHORT).show();

                } else {
                    String filename = editText.getText().toString();
                    savePartialLog(filename, partiallySelectedLogLines.get(0), partiallySelectedLogLines.get(1));
                }

                dialog.dismiss();

            }
        };


        DialogInterface.OnClickListener onCancelListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                cancelPartialSelect();
            }
        };

        DialogHelper.showFilenameSuggestingDialog(this, editText, onClickListener, null, onCancelListener, R.string.save_log);

    }

    private void cancelPartialSelect() {
        partialSelectMode = false;

        boolean changed = false;
        for (LogLine logLine : partiallySelectedLogLines) {
            if (logLine.isHighlighted()) {
                logLine.setHighlighted(false);
                changed = true;
            }
        }
        partiallySelectedLogLines.clear();
        if (changed) {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    mLogListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void silentlySetSearchText(String text) {
        //TODO Something?
        // sets the search text without invoking autosuggestions, which are really only useful when typing

        filter(text);
        supportInvalidateOptionsMenu();
    }

    private void filter(String filterText) {
        Filter filter = mLogListAdapter.getFilter();
        filter.filter(filterText, this);
        mSearchingString = filterText;
    }

    /*@Override
    public boolean onLongClick(View v) {
        // clear button long-pressed, undo clear
        startUpMainLog();
        return true;

    }*/


    private void pauseOrUnpause(MenuItem item) {
        LogReaderAsyncTask currentTask = mTask;

        if (currentTask != null) {
            if (currentTask.isPaused()) {
                currentTask.unpause();
                item.setIcon(R.drawable.ic_pause_white_24dp);
            } else {
                currentTask.pause();
                item.setIcon(R.drawable.ic_play_arrow_white_24dp);
            }
        }
    }


    @Override
    public void onFilterComplete(int count) {
        // always scroll to the bottom when searching
        mListView.setSelection(count);

    }


    private void logLevelChanged() {
        filter(mSearchingString);
    }

    private void updateBackgroundColor() {
        ColorScheme colorScheme = PreferenceHelper.getColorScheme(this);

        final int color = colorScheme.getBackgroundColor(LogcatActivity.this);

        handler.post(new Runnable() {
            public void run() {
                mRootLayout.setBackgroundColor(color);
            }
        });

        mListView.setCacheColorHint(color);
        mListView.setDivider(new ColorDrawable(color));

    }


    private void addToAutocompleteSuggestions(LogLine logLine) {
        // add the tags to the autocompletetextview

        if (!StringUtil.isEmptyOrWhitespaceOnly(logLine.getTag())) {
            String trimmed = logLine.getTag().trim();
            addToAutocompleteSuggestions(trimmed);
        }
    }

    private void addToAutocompleteSuggestions(String trimmed) {
        if (mSearchSuggestionsSet.size() < MAX_NUM_SUGGESTIONS
                && !mSearchSuggestionsSet.contains(trimmed)) {
            mSearchSuggestionsSet.add(trimmed);
            populateSuggestionsAdapter(mSearchingString);
            //searchSuggestionsAdapter.add(trimmed);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        invalidateDarkOrLightMenuItems(this, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    public void invalidateDarkOrLightMenuItems(Context context, final Menu menu) {
        if (menu != null && menu.getClass().getSimpleName().equals("MenuBuilder")) {
            try {
                Field field = menu.getClass().getDeclaredField("mOptionalIconsVisible");
                field.setAccessible(true);
                field.setBoolean(menu, true);

                /*final boolean darkMode = ThemeUtils.isDarkMode(context);
                final int textColorPrimary = Utils.resolveColor(context, android.R.attr.textColorPrimary);

                mToolbar.post(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < menu.size(); i++) {
                            MenuItemImpl item = (MenuItemImpl) menu.getItem(i);
                            int color = darkMode || item.isActionButton() ? Color.WHITE : textColorPrimary;
                            if (item.getIcon() != null) {
                                item.getIcon().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                            }
                        }
                    }
                });*/

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class LogReaderAsyncTask extends AsyncTask<Void, LogLine, Void> {

        private final Object mLock = new Object();
        private int counter = 0;
        private volatile boolean mPaused;
        private boolean mFirstLineReceived;
        private boolean mKilled;
        private LogcatReader mReader;
        private Runnable mOnFinishedRunnable;

        @Override
        protected Void doInBackground(Void... params) {
            log.d("doInBackground()");

            try {
                // use "recordingMode" because we want to load all the existing lines at once
                // for a performance boost
                LogcatReaderLoader loader = LogcatReaderLoader.create(LogcatActivity.this, true);
                mReader = loader.loadReader();

                int maxLines = PreferenceHelper.getDisplayLimitPreference(LogcatActivity.this);

                String line;
                LinkedList<LogLine> initialLines = new LinkedList<>();
                while ((line = mReader.readLine()) != null) {
                    if (mPaused) {
                        synchronized (mLock) {
                            if (mPaused) {
                                mLock.wait();
                            }
                        }
                    }
                    LogLine logLine = LogLine.newLogLine(line, !collapsedMode);
                    if (!mReader.readyToRecord()) {
                        // "ready to record" in this case means all the initial lines have been flushed from the reader
                        initialLines.add(logLine);
                        if (initialLines.size() > maxLines) {
                            initialLines.removeFirst();
                        }
                    } else if (!initialLines.isEmpty()) {
                        // flush all the initial lines we've loaded
                        initialLines.add(logLine);
                        publishProgress(ArrayUtil.toArray(initialLines, LogLine.class));
                        initialLines.clear();
                    } else {
                        // just proceed as normal
                        publishProgress(logLine);
                    }
                }
            } catch (InterruptedException e) {
                log.d(e, "expected error");
            } catch (Exception e) {
                log.d(e, "unexpected error");
            } finally {
                killReader();
                log.d("AsyncTask has died");
            }

            return null;
        }

        public void killReader() {
            if (!mKilled) {
                synchronized (mLock) {
                    if (!mKilled && mReader != null) {
                        mReader.killQuietly();
                        mKilled = true;
                    }
                }
            }

        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            log.d("onPostExecute()");
            doWhenFinished();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            log.d("onPreExecute()");

            resetDisplayedLog(null);

            showProgressBar();
        }

        @Override
        protected void onProgressUpdate(LogLine... values) {
            super.onProgressUpdate(values);

            if (!mFirstLineReceived) {
                mFirstLineReceived = true;
                hideProgressBar();
            }
            for (LogLine logLine : values) {
                mLogListAdapter.addWithFilter(logLine, mSearchingString);

                addToAutocompleteSuggestions(logLine);
            }

            // how many logs to keep in memory?  this avoids OutOfMemoryErrors
            int maxNumLogLines = PreferenceHelper.getDisplayLimitPreference(LogcatActivity.this);

            // check to see if the list needs to be truncated to avoid out of memory errors
            if (++counter % UPDATE_CHECK_INTERVAL == 0
                    && mLogListAdapter.getTrueValues().size() > maxNumLogLines) {
                int numItemsToRemove = mLogListAdapter.getTrueValues().size() - maxNumLogLines;
                mLogListAdapter.removeFirst(numItemsToRemove);
                log.d("truncating %d lines from log list to avoid out of memory errors", numItemsToRemove);
            }

            if (mAutoscrollToBottom) {
                mListView.setSelection(mListView.getCount());
            }

        }

        private void doWhenFinished() {
            if (mPaused) {
                unpause();
            }
            if (mOnFinishedRunnable != null) {
                mOnFinishedRunnable.run();
            }
        }

        public void pause() {
            synchronized (mLock) {
                mPaused = true;
            }
        }

        public void unpause() {
            synchronized (mLock) {
                mPaused = false;
                mLock.notify();
            }
        }

        public boolean isPaused() {
            return mPaused;
        }

        public void setOnFinished(Runnable onFinished) {
            this.mOnFinishedRunnable = onFinished;
        }


    }
}