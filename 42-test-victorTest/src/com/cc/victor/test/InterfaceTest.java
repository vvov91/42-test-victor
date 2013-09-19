package com.cc.victor.test;

import java.io.File;

import android.app.Activity;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cc.victor.DbHelper;
import com.cc.victor.MainActivity;
import com.cc.victor.UserInfo;

public class InterfaceTest extends ActivityInstrumentationTestCase2<MainActivity> {
	
	private Activity mActivity;
	private TextView mNameText;
	private TextView mSurnameText;
	private ImageView mPhoto;
	
	private DbHelper mDb;
	
	public InterfaceTest() { 
		super(MainActivity.class); 
	}
		
	@Override
	protected void setUp() throws Exception {
		super.setUp();		
		
		mActivity = getActivity();
		mNameText = (TextView) mActivity.findViewById(com.cc.victor.R.id.name_value);
		mSurnameText = (TextView) mActivity.findViewById(com.cc.victor.R.id.surname_value);
		mPhoto = (ImageView) mActivity.findViewById(com.cc.victor.R.id.user_photo);

		mDb = new DbHelper(getActivity());
	}	
	
	public void testFieldsCreated() {
		assertNotNull(mActivity);
		assertNotNull(mNameText);
		assertNotNull(mSurnameText);
	}
	
	public void testPhotoIsLoaded() {
		assertNotNull(mPhoto.getDrawable());
	}
	
	public void testNameIsNotEmpty() {
		assertNotSame("", mNameText.getText().toString());
	}
	
	public void testSurnameIsNotEmpty() {
		assertNotSame("", mSurnameText.getText().toString());
	}
	
	public void testDataIsFromDatabase() {
		mDb.open();
		UserInfo expected = mDb.getUserInfo();
		mDb.close();
		
		assertEquals(expected.getName(), mNameText.getText().toString());
		assertEquals(expected.getSurname(), mSurnameText.getText().toString());
	}
	
	public void testPhotoFileExists() {
		File photoFile = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + 
				getActivity().getPackageName() + "/photo.jpg");
		
		assertTrue(photoFile.exists());
	}
	
	public void testNewDataSavesToDatabase() {
		final String name_expected = "test_name";
		final String surname_expected = "test_surname";
		final String bio_expected = "test bio text";
		
		TouchUtils.clickView(this, ((View) mActivity.findViewById(com.cc.victor.R.id.edit)));
		final EditText nameEdit = (EditText) mActivity.findViewById(com.cc.victor.R.id.name_edit);
		final EditText surnameEdit = (EditText) mActivity.findViewById(com.cc.victor.R.id.surname_edit);
		final EditText bioEdit = (EditText) mActivity.findViewById(com.cc.victor.R.id.bio_edit);
		mActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				nameEdit.setText(name_expected);
				surnameEdit.setText(surname_expected);
				bioEdit.setText(bio_expected);
			}
			
		});
		TouchUtils.clickView(this, ((View) mActivity.findViewById(com.cc.victor.R.id.edit)));
		
		mDb.open();
		UserInfo info = mDb.getUserInfo();
		mDb.close();

		assertEquals(name_expected, info.getName());
		assertEquals(surname_expected, info.getSurname());
		assertEquals(bio_expected, info.getBio());
	}

}
