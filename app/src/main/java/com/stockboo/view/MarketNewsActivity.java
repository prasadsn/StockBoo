package com.stockboo.view;

import com.stockboo.view.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.stockboo.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MarketNewsActivity extends Activity {

    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_news);
        getActionBar().hide();
        dialog = new ProgressDialog(this);
        dialog.setMax(100);
        dialog.setMessage("Loading ....");
        dialog.setIndeterminate(false);
        dialog.setProgress(0);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        String webLink = getIntent().getStringExtra("link");
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new StockBooWebViewClient());
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(webLink);
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
    }

    private class StockBooWebViewClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            dialog.setProgress(newProgress);
            if(newProgress > 30)
                dialog.hide();
        }
    }

}
