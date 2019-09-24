package com.pluscubed.logcat.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pluscubed.logcat.App;
import com.pluscubed.logcat.R;
import com.pluscubed.logcat.helper.PackageHelper;
import com.pluscubed.logcat.util.ThemeWrapper;
import com.pluscubed.logcat.util.UtilLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class AboutDialogActivity extends BaseActivity {
    private static final String TAG = "AboutDialogActivity";

    private static UtilLogger log = new UtilLogger(AboutDialogActivity.class);


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fix window background overlay in dialog activities
        getTheme().applyStyle(R.style.DialogOverlay, true);

        DialogFragment fragment = new AboutDialog();
        fragment.show(getFragmentManager(), "aboutDialog");

    }

    public static class AboutDialog extends DialogFragment {

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            getActivity().finish();
        }


        public void initializeWebView(WebView view) {
            // Match webview style with application theme
            String textColor = ThemeWrapper.isLightTheme() ? "#212121" : "#fff";
            String bgColor = ThemeWrapper.isLightTheme() ? "#fff" : "#212121";
            
            String text = loadTextFile(R.raw.about_body);
            String version = PackageHelper.getVersionName(getActivity());
            String changelog = loadTextFile(R.raw.changelog);

            String css = String.format(Locale.ENGLISH, loadTextFile(R.raw.about_css),
                    bgColor, textColor);

            text = String.format(text, version, changelog, css);

            WebSettings settings = view.getSettings();
            settings.setDefaultTextEncodingName("utf-8");

            view.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
        }

        private String loadTextFile(int resourceId) {

            InputStream is = getResources().openRawResource(resourceId);

            StringBuilder sb = new StringBuilder();

            try (BufferedReader buff = new BufferedReader(new InputStreamReader(is))) {
                while (buff.ready()) {
                    sb.append(buff.readLine()).append("\n");
                }
            } catch (IOException e) {
                log.e(e, "This should not happen");
            }

            return sb.toString();

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            WebView view = new WebView(getActivity());
/*
            view.setWebViewClient(new AboutWebClient());*/
            initializeWebView(view);

            return new MaterialDialog.Builder(getActivity())
                    .customView(view, false)
                    .title(R.string.about_matlog)
                    .iconRes(R.mipmap.ic_launcher)
                    .positiveText(android.R.string.ok)
                    .build();
        }


        /*private void loadExternalUrl(String url) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse(url));

            startActivity(intent);
        }*/

        /*private class AboutWebClient extends WebViewClient {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                log.d("shouldOverrideUrlLoading");

                // XXX hack to make the webview go to an external url if the hyperlink is
                // in my own HTML file - otherwise it says "Page not available" because I'm not calling
                // loadDataWithBaseURL.  But if I call loadDataWithBaseUrl using a fake URL, then
                // the links within the page itself don't work!!  Arggggh!!!

                if (url.startsWith("http") || url.startsWith("mailto") || url.startsWith("market")) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadExternalUrl(url);
                        }
                    });
                    return true;
                }
                return false;
            }
        }*/
    }
}
