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
import com.reviewgist.rg.server.StringDownloadCommand;
import com.reviewgist.rg.server.IAPICommand.RequestType;
import com.reviewgist.rg.task.RGProductsTask.RGProductsTaskRunnable;
import com.reviewgist.rg.util.Const;
import com.reviewgist.rg.util.ExceptionUtil;
import com.reviewgist.rg.util.FileUtil;
import com.reviewgist.rg.util.Run;

public class RGProductsTaskLoadMore extends BaseTask<RGListsArray> {

    private RGListsArray mFeedToAttachResultsTo;
    private String mModelID; 
    private String mBrandID;
    public static String mCount;

    private static HashMap<String, RGProductsTaskLoadMore> runningInstances = new HashMap<String, RGProductsTaskLoadMore>();
    public static final String BROADCAST_INTENT_ID = "HNFeedLoadMore";

    private static RGProductsTaskLoadMore getInstance(int taskCode,String modelID,String brandID, String count) {
    	String modelBrandID = "";
    	modelBrandID = modelBrandID.concat(modelID);
    	modelBrandID = modelBrandID.concat(brandID);
    	Log.i("RGProductsTask","getInstance modelbrandId: "+ modelBrandID);
        synchronized (RGProductsTaskLoadMore.class) {
            if (!runningInstances.containsKey(modelBrandID))
                runningInstances.put(modelBrandID, new RGProductsTaskLoadMore(taskCode,modelID,brandID,count));
        }
        return runningInstances.get(modelBrandID);
    }

    private RGProductsTaskLoadMore(int taskCode,String model_id,String brand_id,String count) {
        super(BROADCAST_INTENT_ID, taskCode);
        mModelID = model_id;
        mBrandID = brand_id;
    }

    public static void start(Activity activity, ITaskFinishedHandler<RGListsArray> finishedHandler,
        RGListsArray feedToAttachResultsTo, int taskCode,String model_id,String brand_id,String count) {
    	mCount = count;
        RGProductsTaskLoadMore task = getInstance(taskCode,model_id,brand_id,count);
        task.setOnFinishedHandler(activity, finishedHandler, RGListsArray.class);
        task.setFeedToAttachResultsTo(feedToAttachResultsTo);
        if (task.isRunning())
            task.cancel();
        task.startInBackground();
    }

    
    public static void stopCurrent(Context applicationContext, int taskCode, String modelID,String brandID,String count) {
        getInstance(taskCode,modelID,brandID,count).cancel();
    }

    public static boolean isRunning(Context applicationContext, int taskCode,String modelID,String brandID, String count) {
        return getInstance(taskCode,modelID,brandID,count).isRunning();
    }

    public void setFeedToAttachResultsTo(RGListsArray feedToAttachResultsTo) {
        mFeedToAttachResultsTo = feedToAttachResultsTo;
    }
    
    @Override
    public CancelableRunnable getTask() {
        return new RGProductsTaskLoadMoreRunnable();
    }
    
    class RGProductsTaskLoadMoreRunnable extends CancelableRunnable {

        StringDownloadCommand mFeedDownload;

        @Override
        public void run() {
            String url= "http://www.reviewgist.de/api?operation=search&model_id="+mModelID+"&brand_id="+mBrandID+"&pageitems="+mCount+"&format=json";//mFeedToAttachResultsTo.getNextPageURL();
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
                	mResult = feedParser.parseDocument(mFeedDownload.getResponseContent(),mModelID,mBrandID);
                    Run.inBackground(new Runnable() {
                        public void run() {
                        	FileUtil.setLastRGProducts(mResult,mModelID,mBrandID);
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
