package com.cc.victor.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.cc.victor.DbHelper;
import com.cc.victor.Friend;
import com.cc.victor.FriendsListAdapter;
import com.cc.victor.MainActivity;
import com.cc.victor.SortByPriority;
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
		
		TouchUtils.clickView(this, mActivity.findViewById(com.cc.victor.R.id.edit));
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
		getInstrumentation().waitForIdleSync();
		TouchUtils.clickView(this, mActivity.findViewById(com.cc.victor.R.id.edit));
		
		mDb.open();
		UserInfo info = mDb.getUserInfo();
		mDb.close();

		assertEquals(name_expected, info.getName());
		assertEquals(surname_expected, info.getSurname());
		assertEquals(bio_expected, info.getBio());
	}
	
	public void testFriendsListLoaded() {
		loadFriendsList();
		
		ListView listView = (ListView) mActivity.findViewById(com.cc.victor.R.id.friends_listview);
		assertNotSame(0, listView.getCount());
	}
	
	public void testAtLeastOneFriendPhotoIsLoaded() {
		loadFriendsList();
		
		Drawable noPhotoDrawable = 
				mActivity.getResources().getDrawable(com.cc.victor.R.drawable.no_photo);
		ImageView image = (ImageView) mActivity.findViewById(com.cc.victor.R.id.friend_photo);
		Drawable imageDrawable = image.getDrawable();
		
		assertFalse(imageDrawable.equals(noPhotoDrawable));
	}
	
	public void testUserPrioritySorting() {
		loadFriendsList();
		
		ListView listView = (ListView) mActivity.findViewById(com.cc.victor.R.id.friends_listview);
		
		if (listView.getCount() > 1) {
			Friend friend1 = (Friend) listView.getItemAtPosition(0);
			final Friend friend2 = (Friend) listView.getItemAtPosition(1);
			
			final FriendsListAdapter adapter = (FriendsListAdapter) listView.getAdapter();
			
			mActivity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					adapter.remove(friend2);
					friend2.setPriority(1);
					adapter.add(friend2);
					
					ArrayList<Friend> friends = new ArrayList<Friend>();
					for (int i = 0; i < adapter.getCount(); i++) {
						friends.add(adapter.getItem(i));
					}
					Collections.sort(friends, new SortByPriority());
					adapter.clear();
					adapter.addAll(friends);
					adapter.notifyDataSetChanged();
				}
				
			});		
			getInstrumentation().waitForIdleSync();
			
			Friend friend3 = (Friend) listView.getItemAtPosition(0);
			
			assertNotSame(friend3, friend1);
		}
	}
	
	private void loadFriendsList() {
		final TabHost tabHost = (TabHost) mActivity.findViewById(android.R.id.tabhost);
		mActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				tabHost.setCurrentTab(1);
			}
			
		});
		getInstrumentation().waitForIdleSync();
		
		// sleep thread to wait while friend list is being loading
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) { }
	}

}
