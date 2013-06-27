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
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.googlecode.androidannotations.api.SdkVersionHelper;
import com.reviewgist.rg.R.id;
import com.reviewgist.rg.R.layout;

public final class ProductSummaryActivity_
    extends ProductSummaryActivity
{


    @Override
    public void onCreate(Bundle savedInstanceState) {
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(layout.product_summary_activity);
    }

    private void init_(Bundle savedInstanceState) {
        mInflater = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    private void afterSetContentView_() {
        mProductSummary = ((TextView) findViewById(id.comments_list_item_text));
        mActionbarBack = ((ImageView) findViewById(id.actionbar_back));
        mProductRating = ((RatingBar) findViewById(id.product_summary_setRating));
        mActionbarRefresh = ((ImageView) findViewById(id.actionbar_refresh));
        mActionbarTitle = ((TextView) findViewById(id.actionbar_title));
        mProductImage = ((ImageView) findViewById(id.product_image));
        mProductPrice = ((Button) findViewById(id.product_full_review));
        mReviewName = ((TextView) findViewById(id.product_summary_review_name));
        {
            View view = findViewById(id.product_full_review);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    public void onClick(View view) {
                        productPriceClicked();
                    }

                }
                );
            }
        }
        {
            View view = findViewById(id.actionbar_refresh);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    public void onClick(View view) {
                        refreshClicked();
                    }

                }
                );
            }
        }
        {
            View view = findViewById(id.actionbar);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    public void onClick(View view) {
                        actionBarClicked();
                    }

                }
                );
            }
        }
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

    public static ProductSummaryActivity_.IntentBuilder_ intent(Context context) {
        return new ProductSummaryActivity_.IntentBuilder_(context);
    }

    public static class IntentBuilder_ {

        private Context context_;
        private final Intent intent_;

        public IntentBuilder_(Context context) {
            context_ = context;
            intent_ = new Intent(context, ProductSummaryActivity_.class);
        }

        public Intent get() {
            return intent_;
        }

        public ProductSummaryActivity_.IntentBuilder_ flags(int flags) {
            intent_.setFlags(flags);
            return this;
        }

        public void start() {
            context_.startActivity(intent_);
        }

    }

}
