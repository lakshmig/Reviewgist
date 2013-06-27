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

public class RGProductsTask extends BaseTask<RGListsArray> {

    public static final String BROADCAST_INTENT_ID = "RGGetProducts";
    private static HashMap<String, RGProductsTask> runningInstances = new HashMap<String, RGProductsTask>();

    private String mModelID; 
    private String mBrandID;
    private RGProductsTask(String modelID,String brandID,int taskCode) {
        super(BROADCAST_INTENT_ID, taskCode);
        mModelID = modelID;
        mBrandID = brandID;
        Log.i("RGProductsTask","model: " +mModelID);
        Log.i("RGProductsTask","brand: " +mBrandID);
        
    }

    /**
     * I know, Singleton is generally a no-no, but the only other option would
     * be to store the currently running HNPostCommentsTasks in the App object,
     * which I consider far worse. If you find a better solution, please tweet
     * 
     * @return
     */
    private static RGProductsTask getInstance(String modelID,String brandID, int taskCode) {
    	String modelBrandID = "";
    	modelBrandID = modelBrandID.concat(modelID);
    	modelBrandID = modelBrandID.concat(brandID);
    	Log.i("RGProductsTask","getInstance modelbrandId: "+ modelBrandID);
        synchronized (RGProductsTask.class) {
            if (!runningInstances.containsKey(modelBrandID))
                runningInstances.put(modelBrandID, new RGProductsTask(modelID,brandID,taskCode));
        }
        return runningInstances.get(modelBrandID);
    }

    public static void startOrReattach(Activity activity, ITaskFinishedHandler<RGListsArray> finishedHandler,
        int taskCode,String modelID,String brandID) {
        RGProductsTask task = getInstance(modelID,brandID,taskCode);
        task.setOnFinishedHandler(activity, finishedHandler, RGListsArray.class);
        if (!task.isRunning())
            task.startInBackground();
    }

    public static void stopCurrent(String modelID,String brandID) {
        getInstance(modelID,brandID, 0).cancel();
    }

    public static boolean isRunning(String modelID,String brandID) {
        return getInstance(modelID,brandID,0).isRunning();
    }

    
    @Override
    public CancelableRunnable getTask() {
        return new RGProductsTaskRunnable();
    }

    class RGProductsTaskRunnable extends CancelableRunnable {

        StringDownloadCommand mFeedDownload;

        @Override
        public void run() {
            String url= "http://www.reviewgist.de/api?operation=search&model_id="+mModelID+"&brand_id="+mBrandID+"&pageitems=10&format=json";
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
