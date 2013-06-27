package com.reviewgist.rg.task;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.reviewgist.rg.App;
import com.reviewgist.rg.model.RGListsArray;
import com.reviewgist.rg.parser.RGListParser;
import com.reviewgist.rg.reuse.CancelableRunnable;
import com.reviewgist.rg.server.IAPICommand;
import com.reviewgist.rg.server.IAPICommand.RequestType;
import com.reviewgist.rg.server.StringDownloadCommand;
import com.reviewgist.rg.util.Const;
import com.reviewgist.rg.util.ExceptionUtil;
import com.reviewgist.rg.util.FileUtil;
import com.reviewgist.rg.util.Run;

public class RGPReviewsTask extends BaseTask<RGListsArray> {

    public static final String BROADCAST_INTENT_ID = "RGGetReviews";
    private static HashMap<String, RGPReviewsTask> runningInstances = new HashMap<String, RGPReviewsTask>();

    private String mConfigID; // for which post shall comments be loaded?

    private RGPReviewsTask(String configID,int taskCode) {
        super(BROADCAST_INTENT_ID, taskCode);
        mConfigID = configID;
    }

    /**
     * I know, Singleton is generally a no-no, but the only other option would
     * be to store the currently running HNPostCommentsTasks in the App object,
     * which I consider far worse. If you find a better solution, please tweet
     * 
     * @return
     */
    private static RGPReviewsTask getInstance(String postID, int taskCode) {
        synchronized (RGPReviewsTask.class) {
            if (!runningInstances.containsKey(postID))
                runningInstances.put(postID, new RGPReviewsTask(postID, taskCode));
        }
        return runningInstances.get(postID);
    }

    public static void startOrReattach(Activity activity, ITaskFinishedHandler<RGListsArray> finishedHandler,
        int taskCode,String configID) {
        RGPReviewsTask task = getInstance(configID, taskCode);
        task.setOnFinishedHandler(activity, finishedHandler, RGListsArray.class);
        if (!task.isRunning())
            task.startInBackground();
    }

    public static void stopCurrent(String configID) {
        getInstance(configID, 0).cancel();
    }

    public static boolean isRunning(String configID) {
        return getInstance(configID, 0).isRunning();
    }

    
    @Override
    public CancelableRunnable getTask() {
        return new RGPReviewsTaskRunnable();
    }

    class RGPReviewsTaskRunnable extends CancelableRunnable {

        StringDownloadCommand mFeedDownload;

        @Override
        public void run() {
            String url= "http://www.reviewgist.de/api?operation=productsummary&config_id="+mConfigID+"&format=json";
            mFeedDownload = new StringDownloadCommand(url, new HashMap<String, String>(), RequestType.GET, false, null,
                    App.getInstance(),null );
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
                        	FileUtil.setLastRGProductReviews(mResult,mConfigID);
                    	}
                    });
                } catch (Exception e) {
                    ExceptionUtil.sendToGoogleAnalytics(e, Const.GAN_ACTION_PARSING);
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
