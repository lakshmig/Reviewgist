package com.reviewgist.rg.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.AsyncQueryHandler;
import android.os.AsyncTask;
import android.util.Log;

import com.reviewgist.rg.App;
import com.reviewgist.rg.model.RGListsArray;

public class FileUtil {

    private static final String LAST_HNFEED_FILENAME = "lastHNFeed";
    private static final String LAST_HNPOSTCOMMENTS_FILENAME_PREFIX = "lastHNPostComments";
    private static final String LAST_RGBRANDS_FILENAME = "lastRGBrands";
    private static final String LAST_RGPRDUCTS_FILENAME = "lastRGProducts";
    private static final String LAST_RGPRDUCTREVIEW_FILENAME = "lastRGProductReview";
    private static final String TAG = "FileUtil";

    public abstract static class GetLastHNFeedTask extends AsyncTask<String, Void, RGListsArray> {
        @Override
        protected RGListsArray doInBackground(String... params) {
            return getLastHNFeed();
        }

    }
    
    /*
     * Returns null if no last feed was found or could not be parsed.
     */
    private static RGListsArray getLastHNFeed() {
        ObjectInputStream obj = null;
        try {
            obj = new ObjectInputStream(new FileInputStream(getLastHNFeedFilePath()));
            Object rawHNFeed = obj.readObject();
            if (rawHNFeed instanceof RGListsArray)
                return (RGListsArray) rawHNFeed;
        } catch (Exception e) {
            //Log.e(TAG, "Could not get last getLastHNFeed from file :(", e);
        } finally {
            if (obj != null) {
                try {
                    obj.close();
                } catch (IOException e) {
                    //Log.e(TAG, "Couldn't close last getLastHNFeed file :(", e);
                }
            }
        }
        return null;
    }

    public static void setLastHNFeed(final RGListsArray hnFeed) {
        Run.inBackground(new Runnable() {
            public void run() {
                ObjectOutputStream os = null;
                try {
                    os = new ObjectOutputStream(new FileOutputStream(getLastHNFeedFilePath()));
                    os.writeObject(hnFeed);
                } catch (Exception e) {
                    //Log.e(TAG, "Could not save last setLastHNFeed to file :(", e);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            //Log.e(TAG, "Couldn't close last setLastHNFeed file :(", e);
                        }
                    }
                }
            }
        });
    }
    
    private static String getLastHNFeedFilePath() {
        File dataDir = App.getInstance().getFilesDir();
        return dataDir.getAbsolutePath() + File.pathSeparator + LAST_HNFEED_FILENAME;
    }
   
    public abstract static class GetLastRGBrandsListTask extends AsyncTask<String, Void, RGListsArray> {
        @Override
        protected RGListsArray doInBackground(String...postIDs) {
        	 if (postIDs != null && postIDs.length > 0)
                 return getLastRGBrands(postIDs[0]);
             return null;
        }
    }
    
    /*
     * Returns null if no last brands was not found or could not be parsed.
     */
    private static RGListsArray getLastRGBrands(String model_id) {
        ObjectInputStream obj = null;
        try {
            obj = new ObjectInputStream(new FileInputStream(getLastRGBrandsFilePath(model_id)));
            Object rawHNFeed = obj.readObject();
            if (rawHNFeed instanceof RGListsArray)
                return (RGListsArray) rawHNFeed;
        } catch (Exception e) {
            //Log.e(TAG, "Could not get last getLastRGBrands from file :(", e);
        } finally {
            if (obj != null) {
                try {
                    obj.close();
                } catch (IOException e) {
                    //Log.e(TAG, "Couldn't close last getLastRGBrands file :(", e);
                }
            }
        }
        return null;
    }
   
    private static String getLastRGBrandsFilePath(String postID) {
        File dataDir = App.getInstance().getFilesDir();
        return dataDir.getAbsolutePath() + File.pathSeparator + LAST_RGBRANDS_FILENAME+ "_" + postID;
    }

    public static void setLastRGBrands(final RGListsArray hnFeed, final String model_id) {
        Run.inBackground(new Runnable() {
            public void run() {
                ObjectOutputStream os = null;
                try {
                    os = new ObjectOutputStream(new FileOutputStream(getLastRGBrandsFilePath(model_id)));
                    os.writeObject(hnFeed);
                } catch (Exception e) {
                    //Log.e(TAG, "Could not save last setLastRGBrands to file :(", e);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            //Log.e(TAG, "Couldn't close last setLastRGBrands file :(", e);
                        }
                    }
                }
            }
        });
    }

    public abstract static class GetLastRGProductsListTask extends AsyncTask<String, Void, RGListsArray> {
        @Override
        protected RGListsArray doInBackground(String...postIDs) {
        	 if (postIDs != null && postIDs.length > 0)
                 return getLastRGProducts(postIDs[0],postIDs[1]);
             return null;
        }
    }
    
    /*
     * Returns null if no last brands was not found or could not be parsed.
     */
    private static RGListsArray getLastRGProducts(String model_id,String brand_id) {
    	Log.i("getLastRGProducts","model_id: "+ model_id);
    	Log.i("getLastRGProducts","brand_id: "+ brand_id);
        ObjectInputStream obj = null;
        try {
            obj = new ObjectInputStream(new FileInputStream(getLastRGProductsFilePath(model_id,brand_id)));
            Object rawHNFeed = obj.readObject();
            if (rawHNFeed instanceof RGListsArray)
                return (RGListsArray) rawHNFeed;
        } catch (Exception e) {
            //Log.e(TAG, "Could not get last getLastRGProducts from file :(", e);
        } finally {
            if (obj != null) {
                try {
                    obj.close();
                } catch (IOException e) {
                    //Log.e(TAG, "Couldn't close last getLastRGProducts file :(", e);
                }
            }
        }
        return null;
    }
   
    private static String getLastRGProductsFilePath(String modelID,String brandID) {
        File dataDir = App.getInstance().getFilesDir();
        return dataDir.getAbsolutePath() + File.pathSeparator + LAST_RGPRDUCTS_FILENAME+ "_" + modelID + "_" + brandID;
    }

    public static void setLastRGProducts(final RGListsArray hnFeed, final String model_id,final String brand_id) {
        Run.inBackground(new Runnable() {
            public void run() {
                ObjectOutputStream os = null;
                try {
                    os = new ObjectOutputStream(new FileOutputStream(getLastRGProductsFilePath(model_id,brand_id)));
                    os.writeObject(hnFeed);
                    //Log.i(TAG, "Stored data successfully");
                } catch (Exception e) {
                    //Log.e(TAG, "Could not save last setLastRGProducts to file :(", e);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            //Log.e(TAG, "Couldn't close last NH feed file :(", e);
                        }
                    }
                }
            }
        });
    }
    
    public abstract static class GetLastRGProductReviewsTask extends AsyncTask<String, Void, RGListsArray> {
        @Override
        protected RGListsArray doInBackground(String...postIDs) {
        	 if (postIDs != null && postIDs.length > 0)
                 return getLastRGProductReviews(postIDs[0]);
             return null;
        }
    }
    
    /*
     * Returns null if no last brands was not found or could not be parsed.
     */
    private static RGListsArray getLastRGProductReviews(String config_id) {
        ObjectInputStream obj = null;
        try {
            obj = new ObjectInputStream(new FileInputStream(getLastRGProductReviewsFilePath(config_id)));
            Object rawHNFeed = obj.readObject();
            if (rawHNFeed instanceof RGListsArray)
                return (RGListsArray) rawHNFeed;
        } catch (Exception e) {
            //Log.e(TAG, "Could not get last getLastRGProductReviews from file :(", e);
        } finally {
            if (obj != null) {
                try {
                    obj.close();
                } catch (IOException e) {
                    //Log.e(TAG, "Couldn't close last getLastRGProductReviews feed file :(", e);
                }
            }
        }
        return null;
    }
   
    private static String getLastRGProductReviewsFilePath(String configID) {
        File dataDir = App.getInstance().getFilesDir();
        return dataDir.getAbsolutePath() + File.pathSeparator + LAST_RGPRDUCTREVIEW_FILENAME+ "_" + configID;
    }

    public static void setLastRGProductReviews(final RGListsArray hnFeed, final String config_id) {
        Run.inBackground(new Runnable() {
            public void run() {
                ObjectOutputStream os = null;
                try {
                    os = new ObjectOutputStream(new FileOutputStream(getLastRGProductReviewsFilePath(config_id)));
                    os.writeObject(hnFeed);
                } catch (Exception e) {
                    //Log.e(TAG, "Could not save last setLastRGProductReviews to file :(", e);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            //Log.e(TAG, "Couldn't close last setLastRGProductReviews file :(", e);
                        }
                    }
                }
            }
        });
    }

   
  /*  public abstract static class GetLastHNPostCommentsTask extends AsyncTask<String, Void, HNPostComments> {
        @Override
        protected HNPostComments doInBackground(String... postIDs) {
            if (postIDs != null && postIDs.length > 0)
                return getLastHNPostComments(postIDs[0]);
            return null;
        }
    }

    /*
     * Returns null if no last comments file was found or could not be parsed.
     
    private static HNPostComments getLastHNPostComments(String postID) {
        ObjectInputStream obj = null;
        try {
            obj = new ObjectInputStream(new FileInputStream(getLastHNPostCommentsPath(postID)));
            Object rawHNComments = obj.readObject();
            if (rawHNComments instanceof HNPostComments)
                return (HNPostComments) rawHNComments;
        } catch (Exception e) {
            Log.e(TAG, "Could not get last HNPostComments from file :(", e);
        } finally {
            if (obj != null) {
                try {
                    obj.close();
                } catch (IOException e) {
                    Log.e(TAG, "Couldn't close last NH comments file :(", e);
                }
            }
        }
        return null;
    }

    public static void setLastHNPostComments(final HNPostComments comments, final String postID) {
        Run.inBackground(new Runnable() {
            public void run() {
                ObjectOutputStream os = null;
                try {
                    os = new ObjectOutputStream(new FileOutputStream(getLastHNPostCommentsPath(postID)));
                    os.writeObject(comments);
                } catch (Exception e) {
                    Log.e(TAG, "Could not save last HNPostComments to file :(", e);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Couldn't close last NH comments file :(", e);
                        }
                    }
                }
            }
        });
    }

    private static String getLastHNPostCommentsPath(String postID) {
        File dataDir = App.getInstance().getFilesDir();
        return dataDir.getAbsolutePath() + "/" + LAST_HNPOSTCOMMENTS_FILENAME_PREFIX + "_" + postID;
    }
   */
}
