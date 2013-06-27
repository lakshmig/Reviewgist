package com.reviewgist.rg.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Handles HTML response of a {@link HttpClient}.
 * @author manuelmaly
 */
public class HTMLResponseHandler implements ResponseHandler<String> {
	
	InputStream is = null;
	JSONObject jObj = null;
	String json = null;

    private IAPICommand<String> mCommand;
    
    public HTMLResponseHandler(IAPICommand<String> command, DefaultHttpClient client) {
        mCommand = command;
    }
    
    @Override
    public String handleResponse(HttpResponse response)
            throws ClientProtocolException, IOException {
    		is =  response.getEntity().getContent();
    		
    		try {
    			BufferedReader reader = new BufferedReader(new InputStreamReader(
    					is, "iso-8859-1"), 8);
    			StringBuilder sb = new StringBuilder();
    			String line = null;
    			while ((line = reader.readLine()) != null) {
    				sb.append(line + "\n");
    			}
    			is.close();
    			json = sb.toString();
    		} catch (Exception e) {
    			//Log.e("Buffer Error", "Error converting result " + e.toString());
    		}
    	// try parse the string to a JSON object
    		try {
    			jObj = new JSONObject(json);
    		} catch (JSONException e) {
    			//Log.e("JSON Parser", "Error parsing data " + e.toString());
    		}
    	
    	final StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        mCommand.responseHandlingFinished(json, statusCode);
        return null;
    }

}
