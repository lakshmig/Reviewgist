package com.reviewgist.rg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

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
import com.reviewgist.rg.task.RGProductsTask;
import com.reviewgist.rg.task.ITaskFinishedHandler;
import com.reviewgist.rg.task.RGProductsTaskLoadMore;
import com.reviewgist.rg.util.FileUtil;
import com.reviewgist.rg.util.FontHelper;
import com.reviewgist.rg.util.Run;

@EActivity(R.layout.products_activity)
public class PListsActivity extends Activity implements ITaskFinishedHandler<RGListsArray> {

    @ViewById(R.id.product_list)
    ListView mPostsList;

    @ViewById(R.id.main_empty_view)
    TextView mEmptyListPlaceholder;

    @ViewById(R.id.actionbar_title)
    TextView mActionbarTitle;
    
    @ViewById(R.id.actionbar_back)
    ImageView mActionbarBack;

    @ViewById(R.id.actionbar_refresh)
    ImageView mActionbarRefresh;

    @SystemService
    LayoutInflater mInflater;

    RGListsArray mFeed;
    PostsAdapter mPostsListAdapter;

    int mFontSizeTitle;
    int mFontSizeDetails;

    private static final int TASKCODE_LOAD_PRODUCTS = 50;
    private static final int TASKCODE_LOAD_MORE_PRODUCTS = 60;
    public static final String MODEL_ID = "MODEL";
    public static final String BRAND_ID = "BRAND";
    public static final String BRAND_NAME = "BRAND_NAME";
    String mModel_id;
    String mBrand_id;
    String mBrand_name;
    private static  int more = 10;
    private int mProductNumResults = -1; 

    @AfterViews
    public void init() {
    	mModel_id =  (String) getIntent().getSerializableExtra(MODEL_ID);
    	mBrand_id =  (String) getIntent().getSerializableExtra(BRAND_ID);
    	mBrand_name = (String) getIntent().getSerializableExtra(BRAND_NAME);
        mFeed = new RGListsArray(new ArrayList<RGLists>(), null);
        mPostsListAdapter = new PostsAdapter();
        mPostsList.setAdapter(mPostsListAdapter);
        mPostsList.setEmptyView(mEmptyListPlaceholder);
        mActionbarRefresh.setImageDrawable(getResources().getDrawable(R.drawable.refresh));
        mActionbarTitle.setTypeface(FontHelper.getComfortaa(this, true));
        mActionbarTitle.setText(mBrand_name);
        mEmptyListPlaceholder.setTypeface(FontHelper.getComfortaa(this, true));
        
        mActionbarBack.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        Log.i("PListsActivity","model: " +mModel_id);
        Log.i("PListsActivity","brand: " +mBrand_id);
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

    @Click(R.id.actionbar_refresh)
    void refreshClicked() {
        if (RGProductsTask.isRunning(mModel_id,mBrand_id))
        	RGProductsTask.stopCurrent(mModel_id,mBrand_id);
        else
            startFeedLoading();
    }

    

    @Override
    public void onTaskFinished(int taskCode, TaskResultCode code, RGListsArray result, Object tag) {
        if (taskCode == TASKCODE_LOAD_PRODUCTS) {
            if (code.equals(TaskResultCode.Success) && mPostsListAdapter != null){
            	 RGLists listItem = result.getPosts().get(0);
            	 String numresult = listItem.getProductNumResults();
            	 String NULL = "null";
            	 Log.i("PListsActivity","numresult: " + numresult);
            	 if( numresult != null ){
            		 if(!(numresult.equalsIgnoreCase(NULL)))
            		 mProductNumResults = Integer.parseInt(numresult);
            	 }
                showFeed(result);
            }
            ViewRotator.stopRotating(mActionbarRefresh);
            if (code.equals(TaskResultCode.Success)) {
                ImageViewFader.startFadeOverToImage(mActionbarRefresh, R.drawable.refresh_ok, 100, this);
                Run.delayed(new Runnable() {
                    public void run() {
                        ImageViewFader.startFadeOverToImage(mActionbarRefresh, R.drawable.refresh, 300,
                            PListsActivity.this);
                    }
                }, 2000);
            }

        } else if (taskCode == TASKCODE_LOAD_MORE_PRODUCTS) {
        	//mFeed = null;
            //mFeed.appendLoadMoreFeed(result);
            //mPostsListAdapter.notifyDataSetChanged();
        	 showFeed(result);
        }

    }

    private void showFeed(RGListsArray feed) {
        mFeed = feed;
        mPostsListAdapter.notifyDataSetChanged();
    }

    private void loadIntermediateFeedFromStore() {
        new GetLastRGProductsListTask().execute(mModel_id,mBrand_id);
        long start = System.currentTimeMillis();
        
        //Log.i("", "Loading intermediate feed took ms:" + (System.currentTimeMillis() - start));
    }
    
    class GetLastRGProductsListTask extends FileUtil.GetLastRGProductsListTask {
        protected void onPostExecute(RGListsArray result) {
            if (result == null) {
                // TODO: display "Loading..." instead
            } else
                showFeed(result);
        }
    }

    private void startFeedLoading() {
        RGProductsTask.startOrReattach(this, this, TASKCODE_LOAD_PRODUCTS,mModel_id,mBrand_id);
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
        private static final int VIEWTYPE_LOADMORE = 1;

        @Override
        public int getCount() {
        	int count = 0;
            int posts = mFeed.getPosts().size();
            if (posts == 0)
                count = 0;
            else{
            	if(mProductNumResults > 0 && more <= mProductNumResults)
            		count  = posts + 1;
            	else 
            		count = posts;
               // return posts + (mFeed.isLoadedMore() ? 0 : 1);
            }
            return count;
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
               return VIEWTYPE_LOADMORE;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
        	 RGLists item = getItem(position);
            switch (getItemViewType(position)) {
                case VIEWTYPE_POST:
                    if (convertView == null) {
                        convertView = (LinearLayout) mInflater.inflate(R.layout.product_list_item, null);
                        PostViewHolder holder = new PostViewHolder();
                        holder.titleView = (TextView) convertView.findViewById(R.id.product_list_item_title);
                        holder.priceView = (TextView) convertView.findViewById(R.id.product_list_item_price);
                        holder.textContainer = (LinearLayout) convertView
                            .findViewById(R.id.product_list_item_textcontainer);
                      
                        convertView.setTag(holder);
                    }
                    PostViewHolder holder = (PostViewHolder) convertView.getTag();
                    holder.titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mFontSizeTitle);
                    if(mBrand_name.equals("Top Rated") == true)
                    {
                    	holder.titleView.setText(item.getBrandName()+" "+item.getDisplayName());
                    }else{
                        holder.titleView.setText(item.getDisplayName());
                    }
                    holder.priceView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mFontSizeDetails);
                    
                    if (item.getBestPrice() != null){
                        holder.priceView.setText(item.getBestPrice() + "");
                        holder.priceView.setVisibility(1);
                    }else
                        holder.priceView.setText("-");
                    holder.textContainer.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                               openPostInApp(getItem(position), null, PListsActivity.this);
                        }
                    });
                    holder.textContainer.setOnLongClickListener(new OnLongClickListener() {
                        public boolean onLongClick(View v) {
                            final ArrayList<CharSequence> items = new ArrayList<CharSequence>(Arrays.asList(
                                getString(R.string.pref_htmlprovider_original_url),
                                getString(R.string.pref_htmlprovider_viewtext),
                                getString(R.string.pref_htmlprovider_google), getString(R.string.external_browser)));

                            AlertDialog.Builder builder = new AlertDialog.Builder(PListsActivity.this);
                            return true;
                        }
                    });
                    break;

                case VIEWTYPE_LOADMORE:
                	// I don't use the preloaded convertView here because it's
                    // only one cell
                    convertView = (FrameLayout) mInflater.inflate(R.layout.product_list_item_loadmore, null);
                    final TextView textView = (TextView) convertView.findViewById(R.id.main_list_item_loadmore_text);
                    textView.setTypeface(FontHelper.getComfortaa(PListsActivity.this, true));
                    final ImageView imageView = (ImageView) convertView
                        .findViewById(R.id.main_list_item_loadmore_loadingimage);
                    if (RGProductsTaskLoadMore.isRunning(PListsActivity.this, TASKCODE_LOAD_MORE_PRODUCTS,mModel_id,mBrand_id,String.valueOf(more))) {
                        textView.setVisibility(View.INVISIBLE);
                        imageView.setVisibility(View.VISIBLE);
                        convertView.setClickable(false);
                    }

                    final View convertViewFinal = convertView;
                    convertView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            textView.setVisibility(View.INVISIBLE);
                            imageView.setVisibility(View.VISIBLE);
							convertViewFinal.setClickable(false);
							if(mProductNumResults > 0 && more <= mProductNumResults){
								more = more + 10;
								RGProductsTaskLoadMore.start(PListsActivity.this,PListsActivity.this, mFeed,
										TASKCODE_LOAD_MORE_PRODUCTS,mModel_id,mBrand_id,String.valueOf(more));
							}
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
  
    public static void openPostInApp(RGLists post, String overrideHtmlProvider, Activity a) {
    	String display_name = null;
        Intent i = new Intent(a, ProductReviewActivity_.class);
        i.putExtra(ProductReviewActivity.CONFIG_ID, post.getPostID());
        i.putExtra(ProductReviewActivity.DISPLAY_NAME, ( post.getBrandName() +" "+ post.getDisplayName()));
        a.startActivity(i);
    }

    static class PostViewHolder {
        TextView titleView;
        TextView priceView;
        LinearLayout textContainer;
        
    }

}