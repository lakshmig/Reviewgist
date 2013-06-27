package com.reviewgist.rg.parser;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.reviewgist.rg.model.RGListsArray;
import com.reviewgist.rg.model.RGLists;
import com.reviewgist.rg.util.Const;

public class RGListParser  {
	   // JSON Node names
	private static final String TAG_RESPONSE = "response";
	private static final String TAG_MODELS = "models";
	private static final String TAG_ID = "model_id";
	private static final String TAG_NAME = "display_name";
	private static final String TAG_BRANDS= "brands";
	private static final String TAG_BRAND_ID = "brand_id";
	private static final String TAG_NRESULTS= "num_results";
	private static final String TAG_PRODUCTS= "products";
	private static final String TAG_PRODUCTS_NAME = "name";
	private static final String TAG_PRODUCTS_CONFIGId= "config_id";
	private static final String TAG_PRODUCTS_BRANDNAME = "brandname";
	private static final String TAG_PRODUCTS_PLINE = "productline";
	private static final String TAG_PRODUCTS_PNUM = "productnum";
	private static final String TAG_PRODUCTS_IMAGEURL = "image_url";
	private static final String TAG_PRODUCTS_BPRICE = "best_price";
	
	// for Product Reviews
	private static final String TAG_REVIEWS= "reviews";
	private static final String TAG_RPRICE_URL= "prices_url";
	private static final String TAG_RBEST_PRICE = "best_price";
	private static final String TAG_RIMAGE_URL = "image_url";
	private static final String TAG_RNAME= "name";
	private static final String TAG_RREVIEW_URL = "url";
	private static final String TAG_RSUMMARY = "summary";
	private static final String TAG_RRATING = "star_rating";
	private static final String TAG_RDATE = "date";	
	private String nextPageURL = null;
	
	private int mTaskcode;

	public RGListParser(int taskCode) {
	       mTaskcode = taskCode;
	    }
    public RGListsArray parseDocument(String jsonString, String model_id, String brand_id) throws Exception {
        if (jsonString == null)
            return new RGListsArray();
        // contacts JSONArray
    	JSONArray contacts = null;
    	JSONObject responseObj = null;
    	
        ArrayList<RGLists> posts = new ArrayList<RGLists>();
       
        String title = null;
    
        String postID = null;

 		JSONObject json = null; 
     // try parse the string to a JSON object
     		try {
     			json = new JSONObject(jsonString);
     		} catch (JSONException e) {
     			//Log.e("JSON Parser", "Error parsing data " + e.toString());
     		}

 		
		try{
			responseObj = json.getJSONObject(TAG_RESPONSE);
			
		}catch (JSONException e) {
			e.printStackTrace();
		}
		
 		if(mTaskcode == Const.TASKCODE_LOAD_FEED){
		try{
			// Getting Array of models
			contacts = responseObj.getJSONArray(TAG_MODELS);
			for(int i = 0; i < contacts.length(); i++){
				JSONObject c = contacts.getJSONObject(i);
				
				// Storing each json item in variable
				postID = c.getString(TAG_ID);
				title = c.getString(TAG_NAME);
			    posts.add(new RGLists(title,postID));
			}
			
		}catch(JSONException e) {
			e.printStackTrace();
		}
 		}else if (mTaskcode == Const.TASKCODE_LOAD_BRANDS){
 			try{
 				// Getting Array of models
 				contacts = responseObj.getJSONArray(TAG_BRANDS);
 				for(int i = 0; i < contacts.length(); i++){
 					JSONObject c = contacts.getJSONObject(i);
 					
 					// Storing each json item in variable
 					postID = c.getString(TAG_BRAND_ID);
 					title = c.getString(TAG_NAME);
 				    posts.add(new RGLists(title,postID));
 				}
 				
 			}catch(JSONException e) {
 				e.printStackTrace();
 			}
 		}else if (mTaskcode == Const.TASKCODE_LOAD_PRODUCTS || mTaskcode == Const.TASKCODE_LOAD_MORO_PRODUCTS){
 			String brandname = null;
 	        String productline = null;
 	        String productnum = null;
 	        String image_url = null;
 	        String best_price = null;
 	        String pNum_Results = null;
 	        int  num_results=0;
 	        
 	        try{
 				//JSONObject num_results = responseObj.getJSONObject(TAG_NRESULTS);
 				pNum_Results = responseObj.getString(TAG_NRESULTS);
 	        }catch(JSONException e) {
 				e.printStackTrace();
 			}
 	         
 			try{
 				// Getting Array of models
 				contacts = responseObj.getJSONArray(TAG_PRODUCTS);
 				for(int i = 0; i < contacts.length(); i++){
 					JSONObject c = contacts.getJSONObject(i);
 					
 					// Storing each json item in variable
 					postID = c.getString(TAG_PRODUCTS_CONFIGId);
 					title = c.getString(TAG_PRODUCTS_NAME);
 					brandname = c.getString(TAG_PRODUCTS_BRANDNAME);
 					productline = c.getString(TAG_PRODUCTS_PLINE);
 					productnum = c.getString(TAG_PRODUCTS_PNUM);
 					image_url = c.getString(TAG_PRODUCTS_IMAGEURL);
 					best_price = c.getString(TAG_PRODUCTS_BPRICE);
 				    posts.add(new RGLists(title,postID,pNum_Results,brandname,productline,productnum,image_url,best_price,model_id,brand_id));
 				}
 				
 			}catch(JSONException e) {
 				e.printStackTrace();
 			}
 		}else if (mTaskcode == Const.TASKCODE_LOAD_PRODUCT_REVIEWS){
 			String prices_url = null;
 			String best_price = null;
 	        String image_url = null;
 	        String review_name = null;
 	        String review_url = null;
 	        String review_summary = null;
 	        String review_rating = null;
 	        String review_date = null;
 	        
 	        try{
 				prices_url = responseObj.getString(TAG_RPRICE_URL);
 	        }catch(JSONException e) {
 				e.printStackTrace();
 			}
 	        
 	       try{
				best_price = responseObj.getString(TAG_RBEST_PRICE);
	        }catch(JSONException e) {
				e.printStackTrace();
			}
 	       
 	      try{
 	    	 image_url = responseObj.getString(TAG_RIMAGE_URL);
	        }catch(JSONException e) {
				e.printStackTrace();
			}
 	         
 			try{
 				// Getting Array of models
 				contacts = responseObj.getJSONArray(TAG_REVIEWS);
 				for(int i = 0; i < contacts.length(); i++){
 					JSONObject c = contacts.getJSONObject(i);
 					
 					// Storing each json item in variable
 					review_name = c.getString(TAG_RNAME);
 					review_url = c.getString(TAG_RREVIEW_URL);
 					review_summary = c.getString(TAG_RSUMMARY);
 					if(c.getString(TAG_RRATING).equalsIgnoreCase("null")){
 						review_rating = "0.0";
 					}
 					else
 					review_rating = c.getString(TAG_RRATING);
 					
 					review_date = c.getString(TAG_RDATE);
 				    posts.add(new RGLists(Const.TASKCODE_LOAD_PRODUCT_REVIEWS,prices_url,best_price,image_url,review_name,review_url,review_summary,review_rating,review_date));
 				}
 				
 			}catch(JSONException e) {
 				e.printStackTrace();
 			}
 		}
        return new RGListsArray(posts, nextPageURL);
    }
}
