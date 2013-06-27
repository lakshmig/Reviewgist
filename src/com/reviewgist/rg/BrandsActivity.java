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
import com.reviewgist.rg.task.RGBrandsTask;
import com.reviewgist.rg.task.ITaskFinishedHandler;
import com.reviewgist.rg.util.FileUtil;
import com.reviewgist.rg.util.FontHelper;
import com.reviewgist.rg.util.Run;

@EActivity(R.layout.brands_activity)
public class BrandsActivity extends Activity implements ITaskFinishedHandler<RGListsArray> {

    @ViewById(R.id.main_list)
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

    private static final int TASKCODE_LOAD_BRANDS = 30;
    private static final int TASKCODE_LOAD_MORE_BRANDS = 40;
    public static final String MODEL_ID = "MODEL";
    public static final String MODEL_DNAME = "MODELNAME";
    String mModel_id;
    String mDisplay_name;

    @AfterViews
    public void init() {
    	mModel_id =  (String) getIntent().getSerializableExtra(MODEL_ID);
    	mDisplay_name = (String) getIntent().getSerializableExtra(MODEL_DNAME);
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
        if (RGBrandsTask.isRunning(mModel_id))
        	RGBrandsTask.stopCurrent(mModel_id);
        else
            startFeedLoading();
    }

   
    @Override
    public void onTaskFinished(int taskCode, TaskResultCode code, RGListsArray result, Object tag) {
        if (taskCode == TASKCODE_LOAD_BRANDS) {
            if (code.equals(TaskResultCode.Success) && mPostsListAdapter != null)
                showFeed(result);

            ViewRotator.stopRotating(mActionbarRefresh);
            if (code.equals(TaskResultCode.Success)) {
                ImageViewFader.startFadeOverToImage(mActionbarRefresh, R.drawable.refresh_ok, 100, this);
                Run.delayed(new Runnable() {
                    public void run() {
                        ImageViewFader.startFadeOverToImage(mActionbarRefresh, R.drawable.refresh, 300,
                            BrandsActivity.this);
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
        new GetLastRGBrandsTask().execute(mModel_id);
        long start = System.currentTimeMillis();
        
        //Log.i("", "Loading intermediate feed took ms:" + (System.currentTimeMillis() - start));
    }
    
    class GetLastRGBrandsTask extends FileUtil.GetLastRGBrandsListTask {
        protected void onPostExecute(RGListsArray result) {
            if (result == null) {
                // TODO: display "Loading..." instead
            } else
                showFeed(result);
        }
    }

    private void startFeedLoading() {
        RGBrandsTask.startOrReattach(this, this, TASKCODE_LOAD_BRANDS,mModel_id);
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
            int posts = mFeed.getPosts().size();
            if (posts == 0)
                return 0;
            else
                return posts ;
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
            switch (getItemViewType(position)) {
                case VIEWTYPE_POST:
                    if (convertView == null) {
                        convertView = (LinearLayout) mInflater.inflate(R.layout.main_list_item, null);
                        PostViewHolder holder = new PostViewHolder();
                        holder.titleView = (TextView) convertView.findViewById(R.id.main_list_item_title);
                        holder.textContainer = (LinearLayout) convertView
                            .findViewById(R.id.main_list_item_textcontainer);
                      
                        convertView.setTag(holder);
                    }

                    RGLists item = getItem(position);
                    PostViewHolder holder = (PostViewHolder) convertView.getTag();
                    holder.titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mFontSizeTitle);
                    holder.titleView.setText(item.getTitle());
                    holder.textContainer.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                               openPostInApp(getItem(position), mModel_id, BrandsActivity.this);
                        }
                    });
                    break;
                case VIEWTYPE_LOADMORE:
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
    
    public String getModelID() {
        return mModel_id;
    }
    
    public static void openPostInApp(RGLists post, String overrideHtmlProvider, Activity a) {
        Intent i = new Intent(a, PListsActivity_.class);
        i.putExtra(PListsActivity.MODEL_ID, overrideHtmlProvider);
        i.putExtra(PListsActivity.BRAND_ID, post.getPostID());
        i.putExtra(PListsActivity.BRAND_NAME, post.getTitle());
        Log.i("BrandsActivity", "model_id:"+overrideHtmlProvider);
        Log.i("BrandsActivity", "brand_id:"+post.getPostID());
        Log.i("BrandsActivity", "model_name:"+post.getTitle());
        a.startActivity(i);
    }

    static class PostViewHolder {
        TextView titleView;
        LinearLayout textContainer;
        TextView pointsView;
    }

}