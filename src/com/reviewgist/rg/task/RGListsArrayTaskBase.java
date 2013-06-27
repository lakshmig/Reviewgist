package com.reviewgist.rg.task;

import java.util.HashMap;

import org.jsoup.Jsoup;

import android.util.Log;

import com.reviewgist.rg.App;
import com.reviewgist.rg.model.RGListsArray;
import com.reviewgist.rg.parser.RGListParser;
import com.reviewgist.rg.reuse.CancelableRunnable;
import com.reviewgist.rg.server.IAPICommand;
import com.reviewgist.rg.server.StringDownloadCommand;
import com.reviewgist.rg.server.IAPICommand.RequestType;
import com.reviewgist.rg.util.Const;
import com.reviewgist.rg.util.ExceptionUtil;
import com.reviewgist.rg.util.FileUtil;
import com.reviewgist.rg.util.Run;

public abstract class RGListsArrayTaskBase extends BaseTask<RGListsArray> {

    public RGListsArrayTaskBase(String notificationBroadcastIntentID, int taskCode,String id) {
        super(notificationBroadcastIntentID, taskCode);
    }

    @Override
    public CancelableRunnable getTask() {
        return new HNFeedTaskRunnable();
    }
    
    protected abstract String getFeedURL(int taskcode,String Id);

    class HNFeedTaskRunnable extends CancelableRunnable {

        StringDownloadCommand mFeedDownload;

        @Override
        public void run() {
            mFeedDownload = new StringDownloadCommand(getFeedURL(mTaskCode,"0"), new HashMap<String, String>(), RequestType.GET, false, null,
                App.getInstance(),null);
            
            mFeedDownload.run();

            if (mCancelled)
                mErrorCode = IAPICommand.ERROR_CANCELLED_BY_USER;
            else
                mErrorCode = mFeedDownload.getErrorCode();

            if (!mCancelled && mErrorCode == IAPICommand.ERROR_NONE) {
                RGListParser feedParser = new RGListParser(mTaskCode);
                try {
                    mResult = feedParser.parseDocument(mFeedDownload.getResponseContent(),null,null);
                    Run.inBackground(new Runnable() {
                        public void run() {
                        	if(mTaskCode == Const.TASKCODE_LOAD_FEED)
                        		FileUtil.setLastHNFeed(mResult);
                       	}
                    });
                } catch (Exception e) {
                    mResult = null;
                    ExceptionUtil.sendToGoogleAnalytics(e, Const.GAN_ACTION_PARSING);
                    //Log.e("RGListsArrayTask", "RGListsArray Parser Error :(", e);
                }
            }

            if (mResult == null)
                mResult = new RGListsArray();
        }

        @Override
        public void onCancelled() {
            mFeedDownload.cancel();
        }

    }

}
