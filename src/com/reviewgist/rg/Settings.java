package com.reviewgist.rg;

import com.reviewgist.rg.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
    
    public static final String PREF_FONTSIZE = "pref_fontsize";
    public static final String PREF_HTMLPROVIDER = "pref_htmlprovider";
    public static final String PREF_HTMLVIEWER = "pref_htmlviewer";
    public static final String PREF_USER = "pref_user";
    
    public static final String USER_DATA_SEPARATOR = ":";
    
    public static String getFontSize(Context c) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPref.getString(PREF_FONTSIZE, c.getString(R.string.pref_default_fontsize));
    }

    public static String getHtmlProvider(Context c) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPref.getString(PREF_HTMLPROVIDER, c.getString(R.string.pref_default_htmlprovider));
    }

    public static String getHtmlViewer(Context c) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPref.getString(PREF_HTMLVIEWER, c.getString(R.string.pref_default_htmlviewer));
    }
}
