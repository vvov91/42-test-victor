package com.cc.victor;

import android.os.Environment;

/**
 * Basic constants class
 * 
 * @author Victor Vovchenko <vitek91@gmail.com>
 *
 */
public class Constants {

	public final static String LOG_TAG = "com.cc.victor";			// logging tag
	public final static String USER_PHOTO_FILE_PATH = 
			new StringBuilder().append(Environment.getExternalStorageDirectory())
			.append("/Android/data/com.cc.victor").toString();

}
