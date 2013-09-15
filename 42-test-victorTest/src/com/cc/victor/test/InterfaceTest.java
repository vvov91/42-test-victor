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
	
	private View mEditButton;
	
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
		mEditButton = (View) mActivity.findViewById(com.cc.victor.R.id.edit);

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
	
	public void testEditModeIsAvailable() {
		TouchUtils.clickView(this, mEditButton);
		
		EditText nameEdit = (EditText) mActivity.findViewById(com.cc.victor.R.id.name_edit);
		assertNotNull(nameEdit);
	}

}
