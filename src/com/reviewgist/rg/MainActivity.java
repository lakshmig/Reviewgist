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
import com.reviewgist.rg.task.RGProductsTaskLoadMore;
import com.reviewgist.rg.task.RGListsArrayTaskMainFeed;
import com.reviewgist.rg.task.ITaskFinishedHandler;
import com.reviewgist.rg.util.FileUtil;
import com.reviewgist.rg.util.FontHelper;
import com.reviewgist.rg.util.Run;

@EActivity(R.layout.main)
public class MainActivity extends Activity implements ITaskFinishedHandler<RGListsArray> {

    @ViewById(R.id.main_list)
    ListView mPostsList;

    @ViewById(R.id.main_empty_view)
    TextView mEmptyListPlaceholder;

    @ViewById(R.id.actionbar_title)
    TextView mActionbarTitle;

    @ViewById(R.id.actionbar_refresh)
    ImageView mActionbarRefresh;

    @SystemService
    LayoutInflater mInflater;

    RGListsArray mFeed;
    PostsAdapter mPostsListAdapter;
    HashSet<RGLists> mUpvotedPosts;

    int mFontSizeTitle;
    int mFontSizeDetails;

    private static final int TASKCODE_LOAD_FEED = 10;
    private static final int TASKCODE_LOAD_MORE_POSTS = 20;
    private static final int TASKCODE_VOTE = 100;

    @AfterViews
    public void init() {
        mFeed = new RGListsArray(new ArrayList<RGLists>(), null);
        mPostsListAdapter = new PostsAdapter();
        //mUpvotedPosts = new HashSet<RGLists>();
        mPostsList.setAdapter(mPostsListAdapter);
        mPostsList.setEmptyView(mEmptyListPlaceholder);
        mActionbarRefresh.setImageDrawable(getResources().getDrawable(R.drawable.refresh));
        mActionbarTitle.setTypeface(FontHelper.getComfortaa(this, true));
        mEmptyListPlaceholder.setTypeface(FontHelper.getComfortaa(this, true));

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
        if (RGListsArrayTaskMainFeed.isRunning(getApplicationContext()))
            RGListsArrayTaskMainFeed.stopCurrent(getApplicationContext());
        else
            startFeedLoading();
    }

    
    @Override
    public void onTaskFinished(int taskCode, TaskResultCode code, RGListsArray result, Object tag) {
        if (taskCode == TASKCODE_LOAD_FEED) {
            if (code.equals(TaskResultCode.Success) && mPostsListAdapter != null)
                showFeed(result);

            ViewRotator.stopRotating(mActionbarRefresh);
            if (code.equals(TaskResultCode.Success)) {
                ImageViewFader.startFadeOverToImage(mActionbarRefresh, R.drawable.refresh_ok, 100, this);
                Run.delayed(new Runnable() {
                    public void run() {
                        ImageViewFader.startFadeOverToImage(mActionbarRefresh, R.drawable.refresh, 300,
                            MainActivity.this);
                    }
                }, 2000);
            }

        } else if (taskCode == TASKCODE_LOAD_MORE_POSTS) {
            mFeed.appendLoadMoreFeed(result);
            mPostsListAdapter.notifyDataSetChanged();
        }

    }

    private void showFeed(RGListsArray feed) {
        mFeed = feed;
        mPostsListAdapter.notifyDataSetChanged();
    }

    private void loadIntermediateFeedFromStore() {
        new GetLastHNFeedTask().execute((String)null);
        long start = System.currentTimeMillis();
        
        //Log.i("", "Loading intermediate feed took ms:" + (System.currentTimeMillis() - start));
    }
    
    class GetLastHNFeedTask extends FileUtil.GetLastHNFeedTask {
        protected void onPostExecute(RGListsArray result) {
            if (result == null) {
                // TODO: display "Loading..." instead
            } else
                showFeed(result);
        }
    }

    private void startFeedLoading() {
        RGListsArrayTaskMainFeed.startOrReattach(this, this, TASKCODE_LOAD_FEED,null);
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
                return posts ;//+ (mFeed.isLoadedMore() ? 0 : 1);
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
                               openPostInApp(getItem(position), null, MainActivity.this);
                        }
                    });
                    break;
                default:
                    break;
            }

            return convertView;
        }
    }



    public static void openPostInApp(RGLists post, String overrideHtmlProvider, Activity a) {
        Intent i = new Intent(a, BrandsActivity_.class);
        i.putExtra(BrandsActivity.MODEL_ID, post.getPostID());
        i.putExtra(BrandsActivity.MODEL_DNAME, post.getTitle());
        a.startActivity(i);
    }

    static class PostViewHolder {
        TextView titleView;
        LinearLayout textContainer;
        /*TextView urlView;
        TextView pointsView;
        TextView commentsCountView;
        Button commentsButton;*/
    }

}