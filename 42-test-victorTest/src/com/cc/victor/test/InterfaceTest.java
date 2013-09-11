package com.cc.victor.test;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ImageView;
import android.widget.TextView;

import com.cc.victor.MainActivity;

public class InterfaceTest extends ActivityInstrumentationTestCase2<MainActivity> {
	
	private Activity mActivity;
	private TextView mNameText;
	private TextView mSurnameText;
	private ImageView mPhoto;
	
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
	}
	
	public void testFieldsCreated() {
		assertNotNull(mActivity);
		assertNotNull(mNameText);
		assertNotNull(mSurnameText);
	}
	
	public void testPhotoIsLoaded() {
		assertNotNull(mPhoto);
	}
	
	public void testNameIsCorrect() {
		String expected = getActivity().getString(com.cc.victor.R.string.val_name);
		
		assertEquals(expected, mNameText.getText().toString());
	}
	
	public void testSurnameIsCorrect() {
		String expected = getActivity().getString(com.cc.victor.R.string.val_surname);
		
		assertEquals(expected, mSurnameText.getText().toString());
	}

}
