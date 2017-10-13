package com.webview.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    // ~~~~~~~~~~ Fullscreen ~~~~~~~~~~

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @Override
        public void run() {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        hide();
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        hide();
    }

    // ~~~~~~~~~~ Start application ~~~~~~~~~~

    private String result;

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if(Locale.getDefault().getCountry().toString().toLowerCase().contains("ru")) {
            String myUrl = "http://freegeoip.net/json/";
            HttpGetRequest getRequest = new HttpGetRequest();
            try {
                result = getRequest.execute(myUrl).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if (result.contains("Russia")) {
                mWebView = (WebView) findViewById(R.id.webView);
                mWebView.getSettings().setJavaScriptEnabled(true);
                mWebView.loadUrl(getResources().getString(R.string.url));
                mWebView.setWebViewClient(new MyWebViewClient());

                mWebView.setDownloadListener(new DownloadListener() {
                    public void onDownloadStart(String url, String userAgent,
                                                String contentDisposition, String mimetype,
                                                long contentLength) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });
            } else {
                mWebView = (WebView) findViewById(R.id.webView);
                mWebView.setVisibility(View.GONE);
                LinearLayout mainL = (LinearLayout) findViewById(R.id.mainLayout);
                LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                TextView txtV = new TextView(this);
                txtV.setText(getResources().getText(R.string.sorry));
                txtV.setLayoutParams(viewParams);
                mainL.addView(txtV);
            }
        } else {
            mWebView = (WebView) findViewById(R.id.webView);
            mWebView.setVisibility(View.GONE);
            LinearLayout mainL = (LinearLayout) findViewById(R.id.mainLayout);
            LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            TextView txtV = new TextView(this);
            txtV.setText(getResources().getText(R.string.sorry));
            txtV.setLayoutParams(viewParams);
            mainL.addView(txtV);
        }
    }

    // ~~~~~~~~~~ Pressing back to exit ~~~~~~~~~~

    private boolean exitFlag;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                if (exitFlag) {
                    finish();
                } else {
                    Toast.makeText(this, R.string.notice_exit, Toast.LENGTH_SHORT).show();
                    exitFlag = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exitFlag = false;
                        }
                    }, 2000);
                }
                return true;
            }
        }
        return true;
    }

}

class MyWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
        view.loadUrl(url);
        return true;
    }
}