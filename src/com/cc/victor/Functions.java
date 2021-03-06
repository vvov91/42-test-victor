package com.cc.victor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Basic functions class
 * 
 * @author Victor Vovchenko <vitek91@gmail.com>
 *
 */
public class Functions {

	/**
	 * Checks if network connection is available and connected
	 * 
	 * @return true if network connected, false otherwise
	 */
	public static boolean isNetworkConnected(Context context) {
		boolean result = false;
		
		try {
	        ConnectivityManager cm =
	        		(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	        
	        if (netInfo != null && netInfo.isConnected()) {
	            result = true;
	        } else {
	            netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	            if (netInfo != null && netInfo.isConnected())
	            	result = true;
	        }
	    } catch(Exception e) {
	        e.printStackTrace();  
	        
	        return false;
	    }
		
	    return result;
	}
	
}
