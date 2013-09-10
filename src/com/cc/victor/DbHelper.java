package com.cc.victor;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
	
	private final static int DB_VERSION = 1;
	private final static String DB_NAME = "cc.db";
	
	private SQLiteDatabase mDb;

	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TableInfo.CREATE_QUERY);
		
		Log.i(Constants.LOG_TAG, "Database created");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
	
	public void open() throws SQLException {
		try {
			mDb = this.getWritableDatabase();
		} catch (SQLiteException ex) {
			Log.e(Constants.LOG_TAG, "Failed to open database in read/write mode. Working in read mode");
			
			mDb = this.getReadableDatabase();
		}
	}
	
	public void close() {
		mDb.close();
	}

}
