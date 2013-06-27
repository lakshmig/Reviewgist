package com.reviewgist.rg;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.*;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.ViewById;
import com.reviewgist.rg.R;
import com.reviewgist.rg.model.RGLists;
import com.reviewgist.rg.util.FontHelper;

@EActivity(R.layout.product_summary_activity)
public class ProductSummaryActivity extends Activity {

    @ViewById(R.id.actionbar_title)
    TextView mActionbarTitle;

    @ViewById(R.id.actionbar_refresh)
    ImageView mActionbarRefresh;
    
    @ViewById(R.id.actionbar_back)
    ImageView mActionbarBack;
    
    @ViewById(R.id.product_image)
    ImageView mProductImage; 
    
    @ViewById(R.id.product_summary_setRating)
    RatingBar mProductRating;
    
    @ViewById(R.id.product_summary_review_name)
    TextView mReviewName;
    
    @ViewById(R.id.comments_list_item_text)
    TextView mProductSummary;
    
    @ViewById(R.id.product_full_review)
    Button mProductPrice;
    
    @SystemService
    LayoutInflater mInflater;

    int mFontSizeTitle;
    int mFontSizeDetails;

    public static final String PRODUCT_TITLE = "TITLE";
    public static final String PRODUCT_IMAGE = "IMAGE_URL";
    public static final String PRODUCT_RATING = "RRATING";
    public static final String REVIEW_NAME = "RNAME";
    public static final String REVIEW_SUMMARY = "SUMMARY";
    public static final String FULL_REVIEW_URL = "FULLURL";
    
    String mProduct_Title;
    String mImage_Url;
    String mRating;
    String mReview_Name;
    String mReview_Summary;
    String mFull_ReviewUrl;
    AQuery aq;
    
    @AfterViews
    public void init() {
    	mProduct_Title =  (String) getIntent().getSerializableExtra(PRODUCT_TITLE);
    	mImage_Url = (String) getIntent().getSerializableExtra(PRODUCT_IMAGE);
    	mRating = (String) getIntent().getSerializableExtra(PRODUCT_RATING);
    	mReview_Name = (String) getIntent().getSerializableExtra(REVIEW_NAME);
    	mReview_Summary = (String) getIntent().getSerializableExtra(REVIEW_SUMMARY);
    	mFull_ReviewUrl = (String) getIntent().getSerializableExtra(FULL_REVIEW_URL);
    	
        mActionbarRefresh.setImageDrawable(getResources().getDrawable(R.drawable.refresh));
        mActionbarTitle.setTypeface(FontHelper.getComfortaa(this, true));
        mActionbarTitle.setText(mProduct_Title);
        
        aq = new AQuery(this);   
        aq.id(R.id.product_image).image(mImage_Url, false, true);
        
        mProductRating.setRating(Float.parseFloat(mRating));
        mReviewName.setText(mReview_Name);
        mProductSummary.setText(mReview_Summary);
        
        mActionbarBack.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }
    
    
    @Override
    protected void onResume() {
        super.onResume();
        // refresh because font size could have changed:
        refreshFontSizes();
    }
    
    @Click(R.id.actionbar)
    void actionBarClicked() {
       // mPostsList.smoothScrollToPosition(0);
    }

    @Click(R.id.product_full_review)
    void productPriceClicked() {
    	if(mFull_ReviewUrl != null){
    		Activity a = ProductSummaryActivity.this;
    		 Intent i = new Intent(a, ArticleReaderActivity_.class);
    	        i.putExtra(ArticleReaderActivity.EXTRA_HNPOST, mFull_ReviewUrl);
    	        a.startActivity(i);
    	}
    }
    
    @Click(R.id.actionbar_refresh)
    void refreshClicked() {
       
    }

    private String getArticleViewURL() {
        return ArticleReaderActivity.getArticleViewURL( Settings.getHtmlProvider(this), this);
    }

    private void refreshFontSizes() {
        String fontSize = Settings.getFontSize(this);
        if (fontSize.equals(getString(R.string.pref_fontsize_small))) {
            mFontSizeTitle = 15;
            mFontSizeDetails = 11;
        } else if (fontSize.equals(getString(R.string.pref_fontsize_normal))) {
            mFontSizeTitle = 18;
            mFontSizeDetails = 12;
        } else {
            mFontSizeTitle = 22;
            mFontSizeDetails = 15;
        }
    }

}