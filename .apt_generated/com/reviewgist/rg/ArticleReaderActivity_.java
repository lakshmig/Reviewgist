//
// DO NOT EDIT THIS FILE, IT HAS BEEN GENERATED USING AndroidAnnotations.
//


package com.reviewgist.rg;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.googlecode.androidannotations.api.SdkVersionHelper;
import com.reviewgist.rg.R.id;
import com.reviewgist.rg.R.layout;

public final class ArticleReaderActivity_
    extends ArticleReaderActivity
{


    @Override
    public void onCreate(Bundle savedInstanceState) {
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(layout.article_activity);
    }

    private void init_(Bundle savedInstanceState) {
        mInflater = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    private void afterSetContentView_() {
        mActionbarContainer = ((FrameLayout) findViewById(id.actionbar));
        mWebView = ((WebView) findViewById(id.article_webview));
        mActionbarShare = ((ImageView) findViewById(id.actionbar_share));
        mActionbarBack = ((ImageView) findViewById(id.actionbar_back));
        mActionbarTitle = ((Button) findViewById(id.actionbar_title_button));
        mActionbarRefresh = ((ImageView) findViewById(id.actionbar_refresh));
        init();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        afterSetContentView_();
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        super.setContentView(view, params);
        afterSetContentView_();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        afterSetContentView_();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (((SdkVersionHelper.getSdkInt()< 5)&&(keyCode == KeyEvent.KEYCODE_BACK))&&(event.getRepeatCount() == 0)) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    public static ArticleReaderActivity_.IntentBuilder_ intent(Context context) {
        return new ArticleReaderActivity_.IntentBuilder_(context);
    }

    public static class IntentBuilder_ {

        private Context context_;
        private final Intent intent_;

        public IntentBuilder_(Context context) {
            context_ = context;
            intent_ = new Intent(context, ArticleReaderActivity_.class);
        }

        public Intent get() {
            return intent_;
        }

        public ArticleReaderActivity_.IntentBuilder_ flags(int flags) {
            intent_.setFlags(flags);
            return this;
        }

        public void start() {
            context_.startActivity(intent_);
        }

    }

}
