package com.reviewgist.rg;

import java.net.URLEncoder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.ViewById;
import com.reviewgist.rg.R;
import com.reviewgist.rg.reuse.ViewRotator;
import com.reviewgist.rg.util.FontHelper;

@EActivity(R.layout.article_activity)
public class ArticleReaderActivity extends Activity {

    private static final String WEB_VIEW_SAVED_STATE_KEY = "webViewSavedState";
    public static final String EXTRA_HNPOST = "HNPOST";
    public static final String EXTRA_HTMLPROVIDER_OVERRIDE = "HTMLPROVIDER_OVERRIDE";

    private static final String HTMLPROVIDER_PREFIX_VIEWTEXT = "http://viewtext.org/article?url=";
    private static final String HTMLPROVIDER_PREFIX_GOOGLE = "http://www.google.com/gwt/x?u=";

    @ViewById(R.id.article_webview)
    WebView mWebView;

    @ViewById(R.id.actionbar)
    FrameLayout mActionbarContainer;

    @ViewById(R.id.actionbar_title_button)
    Button mActionbarTitle;

    @ViewById(R.id.actionbar_share)
    ImageView mActionbarShare;

    @ViewById(R.id.actionbar_back)
    ImageView mActionbarBack;

    @ViewById(R.id.actionbar_refresh)
    ImageView mActionbarRefresh;

    @SystemService
    LayoutInflater mInflater;

    String mHtmlProvider;
    
    String mReviewUrl;

    boolean mIsLoading;
	private Bundle mWebViewSavedState;

    @AfterViews
    public void init() {
    	mReviewUrl = (String) getIntent().getSerializableExtra(EXTRA_HNPOST);
        mActionbarTitle.setTypeface(FontHelper.getComfortaa(this, true));
        mActionbarTitle.setText(getString(R.string.article));
        mActionbarTitle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(ArticleReaderActivity.this, null);
                //i.putExtra(CommentsActivity.EXTRA_HNPOST, mPost);
                if (getIntent().getStringExtra(EXTRA_HTMLPROVIDER_OVERRIDE) != null)
                    i.putExtra(EXTRA_HTMLPROVIDER_OVERRIDE, getIntent().getStringExtra(EXTRA_HTMLPROVIDER_OVERRIDE));
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });

        mActionbarBack.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        mActionbarRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mWebView.getProgress() < 100 && mIsLoading) {
                    mWebView.stopLoading();
                    mIsLoading = false;
                    ViewRotator.stopRotating(mActionbarRefresh);
                } else {
                    mIsLoading = true;
                    ViewRotator.startRotating(mActionbarRefresh);
                    mWebView.loadUrl(mReviewUrl);
                }
            }
        });

        
        mHtmlProvider = Settings.getHtmlProvider(this);
        mWebView.loadUrl(mReviewUrl);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new HNReaderWebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100 && mIsLoading) {
                    mIsLoading = false;
                    ViewRotator.stopRotating(mActionbarRefresh);
                } else if (!mIsLoading) {
                    // Most probably, user tapped on a link in the webview -
                    // let's spin the refresh icon:
                    mIsLoading = true;
                    ViewRotator.startRotating(mActionbarRefresh);
                }
            }
        });
        if(mWebViewSavedState != null) {
            mWebView.restoreState(mWebViewSavedState);
        }

        mIsLoading = true;
        ViewRotator.startRotating(mActionbarRefresh);
    }

    public static String getArticleViewURL(String htmlProvider, Context c) {
    	String encodedURL = URLEncoder.encode(htmlProvider);
    	return encodedURL;
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack())
            mWebView.goBack();
        else
            super.onBackPressed();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	Bundle webViewSavedState = new Bundle();
    	mWebView.saveState(webViewSavedState);
    	outState.putBundle(WEB_VIEW_SAVED_STATE_KEY, webViewSavedState);
    	super.onSaveInstanceState(outState);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if(savedInstanceState != null) {
    		mWebViewSavedState = savedInstanceState.getBundle(WEB_VIEW_SAVED_STATE_KEY);
    	}
    }

    @Override
    protected void onDestroy() {
    	mWebView.loadData("", "text/html", "utf-8"); //Destroy any players (e.g. Youtube, Soundcloud) if any
    	//Calling mWebView.destroy(); would not always work according to here: http://stackoverflow.com/questions/6201615/how-do-i-stop-flash-after-leaving-a-webview?rq=1
    	
    	super.onDestroy();
    }

    private class HNReaderWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

}