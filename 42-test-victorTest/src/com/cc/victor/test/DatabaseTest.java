package com.cc.victor.test;

import com.cc.victor.DbHelper;
import com.cc.victor.TableInfo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

public class DatabaseTest extends AndroidTestCase {

	private DbHelper mDb;

	public DatabaseTest() {
		super();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");

		mDb = new DbHelper(context);
		mDb.open();
	}

	public void tearDown() throws Exception {
		super.tearDown();
		
		mDb.close();
	}
	
	public void testDatabaseIsWritable() {
		boolean dbIsWritable = true;
		
		try {
			mDb.getWritableDatabase();
		} catch (Exception e) {
			dbIsWritable = false;
		}
		
		assertTrue(dbIsWritable);
	}
	
	public void testInfoTableIsCreated() {
		String tableNameActual = "";
		String tableNameExpected = TableInfo.TABLE_NAME;
		
		SQLiteDatabase db = mDb.getReadableDatabase();
		Cursor tables = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
		if (tables.getCount() > 0) {
			tables.moveToLast();
			
			tableNameActual = tables.getString(0);
		}
		tables.close();
		
		assertEquals(tableNameExpected, tableNameActual);
	}

}
