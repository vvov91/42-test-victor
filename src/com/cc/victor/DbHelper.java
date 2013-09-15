package com.cc.victor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database helper
 * 
 * @author Victor Vovchenko <vitek91@gmail.com>
 *
 */
public class DbHelper extends SQLiteOpenHelper {
	
	private final static int DB_VERSION = 2;					// DB version
	private final static String DB_NAME = "cc.db";				// DB file
	
	private SQLiteDatabase mDb;

	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TableInfo.CREATE_QUERY);
		
		mDb = db;
		
		Log.i(Constants.LOG_TAG, "Database created");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// upgrading database
		// dropping table
		db.execSQL(TableInfo.DROP_QUERY);
		// creating it again
		db.execSQL(TableInfo.CREATE_QUERY);
		
		Log.i(Constants.LOG_TAG,
				new StringBuilder().append("Database updated from ")
				.append(oldVersion).append(" to ").append(newVersion).toString());
	}
	
	public void open() throws SQLException {
		try {
			mDb = this.getWritableDatabase();
		} catch (SQLiteException ex) {
			Log.e(Constants.LOG_TAG, "Failed to open database in read/write mode. Working in read mode");
			
			mDb = this.getReadableDatabase();
		}
	}
	
	@Override
	public void close() {
		mDb.close();
	}
	
	/**
	 * Inserts {@link UserInfo} object into database
	 * 
	 * @param data {@link UserInfo} object to insert
	 */
	public void addUserInfo(UserInfo data) {
		ContentValues values = new ContentValues();
		
		mDb.beginTransaction();
		try {
			values.put(TableInfo.NAME, data.getName());
			values.put(TableInfo.SURNAME, data.getSurname());
			values.put(TableInfo.DATEOFBIRTH, data.getDateOfBirth());
			values.put(TableInfo.BIO, data.getBio());
			values.put(TableInfo.LINK, data.getLink());
			values.put(TableInfo.EMAIL, data.getEmail());
			
			mDb.insert(TableInfo.TABLE_NAME, null, values);
			
			mDb.setTransactionSuccessful();
		} finally {			
			mDb.endTransaction();
			
			Log.i(Constants.LOG_TAG, "Added entry to database");
		}
	}
	
	/**
	 * Returns {@link UserInfo} object from database
	 * 
	 * @return {@link UserInfo} object with user data
	 */
	public UserInfo getUserInfo() {
		UserInfo data = new UserInfo();
		
		mDb.beginTransaction();
		try {
			Cursor info = mDb.query(TableInfo.TABLE_NAME, null, null, null, null, null, null, "1");
			if (info.getCount() != 0) {
				info.moveToFirst();

				data.setName(info.getString(info.getColumnIndex(TableInfo.NAME)));
				data.setSurname(info.getString(info.getColumnIndex(TableInfo.SURNAME)));
				data.setDateOfBirth(info.getLong(info.getColumnIndex(TableInfo.DATEOFBIRTH)));
				data.setBio(info.getString(info.getColumnIndex(TableInfo.BIO)));
				data.setLink(info.getString(info.getColumnIndex(TableInfo.LINK)));
				data.setEmail(info.getString(info.getColumnIndex(TableInfo.EMAIL)));
			}
			info.close();
		} finally {
			mDb.endTransaction();
		}
		
		return data;
	}
	
	/**
	 * Updates user data with {@link UserInfo}
	 * 
	 * @param info {@link UserInfo} object with user data
	 */
	public void updateUserInfo(UserInfo info) {
		ContentValues values = new ContentValues();
		values.put(TableInfo.NAME, info.getName());
		values.put(TableInfo.SURNAME, info.getSurname());
		values.put(TableInfo.DATEOFBIRTH, info.getDateOfBirth());
		values.put(TableInfo.BIO, info.getBio());
		values.put(TableInfo.LINK, info.getLink());
		values.put(TableInfo.EMAIL, info.getEmail());

		mDb.update(TableInfo.TABLE_NAME, values, null, null);
		
		Log.i(Constants.LOG_TAG, "User data updated");
	}
	
	/**
	 * Checks if database has no entries
	 * 
	 * @return true if database is empty, false otherwise
	 */
	public boolean isDbEmpty() {
		boolean dbIsEmpty = true;
		
		mDb.beginTransaction();
		try {
			Cursor info = mDb.query(TableInfo.TABLE_NAME, new String[] { TableInfo.NAME },
					null, null, null, null, null, "1");
			if (info.getCount() != 0)
				dbIsEmpty = false;
			info.close();
			
			mDb.setTransactionSuccessful();					
		} finally {
			mDb.endTransaction();
		}
		
		return dbIsEmpty;
	}
	
	/**
	 * Deletes all database content
	 */
	public void clearDatabase() {
		mDb.delete(TableInfo.TABLE_NAME, null, null);
		
		Log.i(Constants.LOG_TAG, "Database cleared");
	}

}
