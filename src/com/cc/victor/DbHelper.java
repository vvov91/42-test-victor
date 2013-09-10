package com.cc.victor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
	
	private final static int DB_VERSION = 1;
	private final static String DB_NAME = "cc.db";
	
	private SQLiteDatabase mDb;
	private Context mContext;

	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TableInfo.CREATE_QUERY);
		
		mDb = db;
		
		Log.i(Constants.LOG_TAG, "Database created");
		
		UserInfo userInfo = new UserInfo(mContext.getString(R.string.val_name),
				mContext.getString(R.string.val_surname), 
				Long.valueOf(mContext.getString(R.string.val_date_of_birth)), 
				mContext.getString(R.string.val_bio), 
				mContext.getString(R.string.val_phone), 
				mContext.getString(R.string.val_email));
		
		addUserInfo(userInfo);
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
	
	public void addUserInfo(UserInfo data) {
		ContentValues values = new ContentValues();
		
		mDb.beginTransaction();
		try {
			values.put(TableInfo.NAME, data.getName());
			values.put(TableInfo.SURNAME, data.getSurname());
			values.put(TableInfo.DATEOFBIRTH, data.getDateOfBirth());
			values.put(TableInfo.BIO, data.getBio());
			values.put(TableInfo.PHONE, data.getPhone());
			values.put(TableInfo.EMAIL, data.getEmail());
			
			mDb.insert(TableInfo.TABLE_NAME, null, values);
			
			mDb.setTransactionSuccessful();
		} finally {			
			mDb.endTransaction();
			
			Log.i(Constants.LOG_TAG, "Added entry to database");
		}
	}
	
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
				data.setPhone(info.getString(info.getColumnIndex(TableInfo.PHONE)));
				data.setEmail(info.getString(info.getColumnIndex(TableInfo.EMAIL)));
			}
			info.close();
		} finally {
			mDb.endTransaction();
		}
		
		return data;
	}

}
