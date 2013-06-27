package com.reviewgist.rg.model;

import java.io.Serializable;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.reviewgist.rg.util.Const;

public class RGLists implements Serializable {
    
    private static final long serialVersionUID = -6764758363164898276L;
    private String mDisplayName = null;
    private String mModelID = null;
    private String mPModel_id = null;
    private String mPBrand_id = null;
    
    private String mProductNumResults = null; 
    private String mBrandName = null; 
    private String mPLine = null; 
    private String mPnum = null; 
    private String mImageUrl = null; 
    private String mBestPrice = null; 
    
    private String mReviewUrl = null; 
    private String mReviewPriceUrl = null; 
    private String mReviewName = null; 
    private String mReviewSummary = null; 
    private String mReviewRating = null; 
    private String mReviewDate = null; 
    
    
    public RGLists( String display_name, String modelID) {
        super();
        mDisplayName = display_name;
        mModelID = modelID;
    }
   // for product lists
    public RGLists( String name, String config_id,String num_results,String brandname,String productline,String productnum,String image_url,String best_price,String pmodel_id, String pbrand_id) {
        super();
        mDisplayName = name;
        mModelID = config_id;
        mProductNumResults = num_results;
        mBrandName = brandname;
        mPLine = productline;
        mPnum = productnum;
        mImageUrl = image_url;
        mBestPrice = best_price;
        mPModel_id =  pmodel_id;
        mPBrand_id = pbrand_id;
    }
    
    public RGLists(int taskCode,String prices_url,String best_price,String image_url,String review_name,String review_url,String review_summary,String review_rating,String review_date){
    	mReviewPriceUrl = prices_url;
    	mBestPrice = best_price;
    	mImageUrl = image_url;
    	mReviewName = review_name;
    	mReviewUrl = review_url;
    	mReviewSummary = review_summary;
    	mReviewRating = review_rating;
    	mReviewDate  = review_date; 	
    }
    
    public String getReviewPriceUrl() {
        return mReviewPriceUrl;
    }
    
    public String getReviewName() {
        return mReviewName;
    }
    public String getReviewUrl() {
        return mReviewUrl;
    }
    public String getReviewSummary() {
        return mReviewSummary;
    }
    public String getReviewDate() {
        return mReviewDate;
    }
    public String getReviewRating() {
    		return mReviewRating;
    }
    
    public String getTitle() {
    	return mDisplayName;
    }

    public String getDisplayName() {
    	String displayName = mDisplayName;
    	String NULL = "null";
    	if(mPLine != null){
    		if(!(mPLine.equalsIgnoreCase(NULL))){
	    		displayName = mPLine;
	    		if(!(mPnum.equalsIgnoreCase(NULL))){
	    			displayName = displayName.concat(" ");
	    			displayName = displayName.concat(mPnum);
	    		}
    		}
    	}else if(mPnum != null && !(mPnum.equalsIgnoreCase(NULL))){
			displayName = mPnum;
		}
    	return displayName;
    }
    public String getPostID() {
        return mModelID;
    }
    
    public String getProductNumResults() {
        return mProductNumResults;
    }
    
    public String getBrandName() {
        return mBrandName;
    }
    public String getPLine() {
        return mPLine;
    }
    public String getPnum() {
        return mPnum;
    }
    public String getImageUrl(){
        return mImageUrl;
    }
    public String  getBestPrice()  {
    	if(mBestPrice.length() >0)
    		return mBestPrice;
    	else
    		return null;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mModelID == null) ? 0 : mModelID.hashCode());
        return result;
    }

    
    
}
