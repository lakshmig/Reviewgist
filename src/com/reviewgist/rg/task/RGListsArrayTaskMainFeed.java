package com.reviewgist.rg.task;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.reviewgist.rg.model.RGListsArray;

public class RGListsArrayTaskMainFeed extends RGListsArrayTaskBase {
	
	  private static final int TASKCODE_LOAD_FEED = 10;
	  private static final int TASKCODE_LOAD_MORE_POSTS = 20;
	  private static final int TASKCODE_LOAD_BRANDS = 30;
	  private static final int TASKCODE_LOAD_MORE_BRANDS = 40;
    
    private static RGListsArrayTaskMainFeed instance;
    public static final String BROADCAST_INTENT_ID = "HNFeedMain";
    
    private static RGListsArrayTaskMainFeed getInstance(int taskCode,String Id) {
        synchronized (RGListsArrayTaskBase.class) {
            if (instance == null)
                instance = new RGListsArrayTaskMainFeed(taskCode,Id);
        }
        return instance;
    }
    
    private RGListsArrayTaskMainFeed(int taskCode,String Id) {
        super(BROADCAST_INTENT_ID, taskCode,Id);
    }
    
    @Override
    protected String getFeedURL(int taskcode,String Id) {
    	String url = null;
    	if(taskcode == TASKCODE_LOAD_FEED )
    		url = "http://www.reviewgist.de/api?operation=listmodels&format=json"; 
    	return url; 
    }
    
    public static void startOrReattach(Activity activity, ITaskFinishedHandler<RGListsArray> finishedHandler, int taskCode,String Id) {
        RGListsArrayTaskMainFeed task = getInstance(taskCode,Id);
        task.setOnFinishedHandler(activity, finishedHandler, RGListsArray.class);
        if (!task.isRunning())
            task.startInBackground();
    }

    public static void stopCurrent(Context applicationContext) {
        getInstance(0,null).cancel();
    }

    public static boolean isRunning(Context applicationContext) {
        return getInstance(0,null).isRunning();
    }

}
