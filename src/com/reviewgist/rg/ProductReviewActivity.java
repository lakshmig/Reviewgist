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
import com.reviewgist.rg.model.RGListsArray;
import com.reviewgist.rg.model.RGLists;
import com.reviewgist.rg.reuse.ImageViewFader;
import com.reviewgist.rg.reuse.ViewRotator;
import com.reviewgist.rg.task.RGPReviewsTask;
import com.reviewgist.rg.task.ITaskFinishedHandler;
import com.reviewgist.rg.util.FileUtil;
import com.reviewgist.rg.util.FontHelper;
import com.reviewgist.rg.util.Run;

@EActivity(R.layout.product_review_activity)
public class ProductReviewActivity extends Activity implements ITaskFinishedHandler<RGListsArray> {

    @ViewById(R.id.product_review_list)
    ListView mPostsList;

    @ViewById(R.id.main_empty_view)
    TextView mEmptyListPlaceholder;

    @ViewById(R.id.actionbar_title)
    TextView mActionbarTitle;

    @ViewById(R.id.actionbar_refresh)
    ImageView mActionbarRefresh;
    
    @ViewById(R.id.actionbar_back)
    ImageView mActionbarBack;
    
    @ViewById(R.id.product_image)
    ImageView mProductImage;
    
    @ViewById(R.id.product_price)
    Button mProductPrice;
    
    @SystemService
    LayoutInflater mInflater;

    RGListsArray mFeed;
    PostsAdapter mPostsListAdapter;

    int mFontSizeTitle;
    int mFontSizeDetails;

    private static final int TASKCODE_LOAD_PRODUCT_REVIEWS = 70;
    public static final String CONFIG_ID = "CONFIGID";
    public static final String DISPLAY_NAME = "DISPLAYNAME";
    String mConfig_id;
    String mDisplay_name;
    String mProduct_priceUrl;
    AQuery aq;
    
    @AfterViews
    public void init() {
    	mConfig_id =  (String) getIntent().getSerializableExtra(CONFIG_ID);
    	mDisplay_name = (String) getIntent().getSerializableExtra(DISPLAY_NAME);
    	//mProduct_image = (String) getIntent().getSerializableExtra(PIMAGE_URL);
    	//mProduct_price = (String) getIntent().getSerializableExtra(PPRICE);
        mFeed = new RGListsArray(new ArrayList<RGLists>(), null);
        mPostsListAdapter = new PostsAdapter();
        mPostsList.setAdapter(mPostsListAdapter);
        mPostsList.setEmptyView(mEmptyListPlaceholder);
        mActionbarRefresh.setImageDrawable(getResources().getDrawable(R.drawable.refresh));
        mActionbarTitle.setTypeface(FontHelper.getComfortaa(this, true));
        mActionbarTitle.setText(mDisplay_name);
        mEmptyListPlaceholder.setTypeface(FontHelper.getComfortaa(this, true));
        
        mActionbarBack.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        
        aq = new AQuery(this);   
        
        loadIntermediateFeedFromStore();
        startFeedLoading();
    }
    
    
    @Override
    protected void onResume() {
        super.onResume();
            startFeedLoading();
        // refresh because font size could have changed:
        refreshFontSizes();
        mPostsListAdapter.notifyDataSetChanged();
    }
    
    @Click(R.id.actionbar)
    void actionBarClicked() {
        mPostsList.smoothScrollToPosition(0);
    }

    @Click(R.id.product_price)
    void productPriceClicked() {
    	if(mProduct_priceUrl != null){
    		Activity a = ProductReviewActivity.this;
    		Intent i = new Intent(a, ArticleReaderActivity_.class);
    	    i.putExtra(ArticleReaderActivity.EXTRA_HNPOST, mProduct_priceUrl);
    	    a.startActivity(i);    		
    	}
    }
    
    @Click(R.id.actionbar_refresh)
    void refreshClicked() {
        if (RGPReviewsTask.isRunning(mConfig_id))
        	RGPReviewsTask.stopCurrent(mConfig_id);
        else
            startFeedLoading();
    }


    @Override
    public void onTaskFinished(int taskCode, TaskResultCode code, RGListsArray result, Object tag) {
        if (taskCode == TASKCODE_LOAD_PRODUCT_REVIEWS) {
            if (code.equals(TaskResultCode.Success) && mPostsListAdapter != null)
                showFeed(result);

            ViewRotator.stopRotating(mActionbarRefresh);
            if (code.equals(TaskResultCode.Success)) {
                ImageViewFader.startFadeOverToImage(mActionbarRefresh, R.drawable.refresh_ok, 100, this);
                Run.delayed(new Runnable() {
                    public void run() {
                        ImageViewFader.startFadeOverToImage(mActionbarRefresh, R.drawable.refresh, 300,
                            ProductReviewActivity.this);
                    }
                }, 2000);
            }

        }
    }

    private void showFeed(RGListsArray feed) {
        mFeed = feed;
        mPostsListAdapter.notifyDataSetChanged();
    }

    private void loadIntermediateFeedFromStore() {
        new GetLastRGProductReviewsTask().execute(mConfig_id);
        long start = System.currentTimeMillis();
        
        //Log.i("", "Loading intermediate feed took ms:" + (System.currentTimeMillis() - start));
    }
    
    class GetLastRGProductReviewsTask extends FileUtil.GetLastRGProductReviewsTask {
        protected void onPostExecute(RGListsArray result) {
            if (result == null) {
                // TODO: display "Loading..." instead
            } else
                showFeed(result);
        }
    }

    private void startFeedLoading() {
        RGPReviewsTask.startOrReattach(this, this, TASKCODE_LOAD_PRODUCT_REVIEWS,mConfig_id);
        mActionbarRefresh.setImageResource(R.drawable.refresh);
        ViewRotator.stopRotating(mActionbarRefresh);
        ViewRotator.startRotating(mActionbarRefresh);
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

    class PostsAdapter extends BaseAdapter {

        private static final int VIEWTYPE_POST = 0;

        @Override
        public int getCount() {
            int posts = mFeed.getPosts().size();
            if (posts == 0)
                return 0;
            else
                return posts;
        }

        @Override
        public RGLists getItem(int position) {
            if (getItemViewType(position) == VIEWTYPE_POST)
                return mFeed.getPosts().get(position);
            else
                return null;
        }

        @Override
        public long getItemId(int position) {
            // Item ID not needed here:
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < mFeed.getPosts().size())
                return VIEWTYPE_POST;
            else
                return VIEWTYPE_POST;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            switch (getItemViewType(position)) {
                case VIEWTYPE_POST:
                    if (convertView == null) {
                        convertView = (LinearLayout) mInflater.inflate(R.layout.product_review_list_item, null);
                        PostViewHolder holder = new PostViewHolder();
                        holder.ratingView = (RatingBar) convertView.findViewById(R.id.product_review_setRating);
                        holder.textView = (TextView) convertView.findViewById(R.id.product_review_list_item_title);
                        holder.textContainer = (LinearLayout) convertView
                            .findViewById(R.id.product_review_list_item_textcontainer);
                      
                        convertView.setTag(holder);
                    }
                    //Load he image and price too here..                 
                    RGLists item = getItem(position);
                    mProduct_priceUrl = item.getReviewPriceUrl() ;
                    aq.id(R.id.product_image).image(item.getImageUrl(), false, true);
                    if (item.getBestPrice() != null){
                     mProductPrice.setText(item.getBestPrice());
                     mProductPrice.setVisibility(1);
                    }
                    PostViewHolder holder = (PostViewHolder) convertView.getTag();
                    holder.textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mFontSizeTitle);
                    holder.ratingView.setRating(Float.parseFloat(item.getReviewRating()));
                    holder.textView.setText(item.getReviewName());
                    holder.textContainer.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                               openPostInApp(getItem(position), mDisplay_name, ProductReviewActivity.this);
                        }
                    });
                    break;
                default:
                    break;
            }

            return convertView;
        }
    }
    

    public static void openURLInBrowser(String url, Activity a) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        a.startActivity(browserIntent);
    }
    
    public String getConfigID() {
        return mConfig_id;
    }
    
    public static void openPostInApp(RGLists post, String overrideHtmlProvider, Activity a) {
        Intent i = new Intent(a, ProductSummaryActivity_.class);
        i.putExtra(ProductSummaryActivity.PRODUCT_TITLE, overrideHtmlProvider);
        i.putExtra(ProductSummaryActivity.PRODUCT_IMAGE, post.getImageUrl());
        i.putExtra(ProductSummaryActivity.PRODUCT_RATING, post.getReviewRating());
        i.putExtra(ProductSummaryActivity.REVIEW_NAME, post.getReviewName());
        i.putExtra(ProductSummaryActivity.REVIEW_SUMMARY, post.getReviewSummary());
        i.putExtra(ProductSummaryActivity.FULL_REVIEW_URL, post.getReviewUrl());
        a.startActivity(i);
    }

    static class PostViewHolder {
        RatingBar ratingView;
        TextView textView;
        LinearLayout textContainer;
        
     }

}