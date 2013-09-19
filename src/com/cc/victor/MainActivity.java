package com.cc.victor;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;


import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabWidget;

/**
 * Main activty class
 * 
 * @author Victor Vovchenko <vitek91@gmail.com>
 *
 */
public class MainActivity extends SherlockFragmentActivity {

	private TabHost mTabHost;
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	private ProgressBar mProgressBar;
	
	private Session mSession;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private String mAuthToken = "";						// authorization token
		
	private SharedPreferences mPrefs;
	
	private Bundle mSavedInstanceState = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		

		if (savedInstanceState != null) {
			mSavedInstanceState = savedInstanceState;
		}

		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
		
		// getting auth token from app settings storage
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mAuthToken = mPrefs.getString("auth_token", "");
		
		// perform login
		facebookLogin();		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mTabHost != null) 
			outState.putInt("tab", mTabHost.getCurrentTab());
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	   
	    mSession.onActivityResult(this, requestCode, resultCode, data);
	}
	    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
		
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		
		// on memu "logout" click
		case R.id.logout:
			// if session is not started
			if (mSession == null) {
				// start new session
				mSession = new Session(this);
			}
			// clear token information
			mSession.closeAndClearTokenInformation();
			// save empty token to app settings storage
			saveToken("");		
			
			// clear database
			DbHelper db = new DbHelper(this);
			db.open();
			db.clearDatabase();
			db.close();
			
			// delete user photo
			File photoFile = new File(Constants.USER_PHOTO_FILE_PATH + "/photo.jpg");
			photoFile.delete();
			
			Log.i(Constants.LOG_TAG, "Session cleared, auth token deleted");
			
			// exit app
			finish();
			
			break;
		}
		
		return super.onMenuItemSelected(featureId, item);
	}
	
	/**
	 * Shows dialog with no network connection error
	 */
	private void noConnectionDialog() {
		new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.connection)
		.setMessage(R.string.connection_is_out)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setNeutralButton(R.string.connection_settings, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// close app
				finish();
				// and open wi-fi settings
				startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
			}
			
		})
		.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
			
		})
		.show();
	}
	
	/**
	 * Loads app tabs
	 */
	private void loadTabs() {
		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);	

		// tab with user data 
		mTabsAdapter.addTab(mTabHost.newTabSpec(getString(R.string.me))
				.setIndicator(getString(R.string.me)), MeFragment.class, null);		

		// tab with friends list
		mTabsAdapter.addTab(mTabHost.newTabSpec(getString(R.string.about))
				.setIndicator(getString(R.string.friends)), FriendsListFragment.class, null);

		// tab with app about info
		mTabsAdapter.addTab(mTabHost.newTabSpec(getString(R.string.about))
				.setIndicator(getString(R.string.about)), AboutFragment.class, null);
		
		if (mSavedInstanceState != null) {
			mTabHost.setCurrentTab(mSavedInstanceState.getInt("tab"));
		}
	}
	
	/**
	 * Starts new Facebook session
	 */
	private void facebookLogin() {
		mSession = new Session(this);
		mSession.openForRead(new Session.OpenRequest(this)
			.setPermissions(Arrays.asList("email", "user_birthday", "user_about_me"))
			.setCallback(statusCallback));
	}
	
	/**
	 * Performs request to fetch user data
	 */
	private void facebookGetUserInfo() {		
		if (mSession.isOpened()) {
			mProgressBar.setVisibility(View.VISIBLE);
			
			Request.newMeRequest(mSession, new Request.GraphUserCallback() {

				@Override
				public void onCompleted(GraphUser user, Response response) {					
					if (user != null) {
						mProgressBar.setVisibility(View.GONE);
						
						SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
						sdf.setTimeZone(TimeZone.getDefault());

						// convert bithday date to milliseconds
						String birthDate = user.getBirthday();
						long birthDateMs = 0L;
						try {
							birthDateMs = sdf.parse(birthDate).getTime();
						} catch (ParseException e) { }
						
						String bio = "";
						String email = "";
						
						// check if additional fields are specified
						if (user.asMap().get("bio") != null) 
							bio = user.asMap().get("bio").toString();
						
						if (user.asMap().get("email") != null)
							email = user.asMap().get("email").toString();
						
						UserInfo info = new UserInfo(user.getFirstName(),
								user.getLastName(), birthDateMs, bio, user.getLink(), email);
						
						DbHelper db = new DbHelper(MainActivity.this);
						db.open();
						
						// insert data into DB
						db.addUserInfo(info);
						
						db.close();
						
						// save user Id in app settings storage
						saveUserId(user.getId());
						
						Log.i(Constants.LOG_TAG, "New user data saved");
						
						// load interface
						loadTabs();
                    }
				} 
				
			}).executeAsync();
		}
	}
	
	/**
	 * Saves authorization token to app settings storage
	 * 
	 * @param token authorization token
	 */
	private void saveToken(String token) {
		Editor prefsEditor = mPrefs.edit();
		prefsEditor.putString("auth_token", token);
		prefsEditor.commit();
		
		mAuthToken = token;
	}
	
	/**
	 * Saves user Id to app settings storage
	 * 
	 * @param userId Facebook user Id
	 */
	private void saveUserId(String userId) {
		Editor prefsEditor = mPrefs.edit();
		prefsEditor.putString("user_id", userId);
		prefsEditor.commit();
	}
	
	/**
	 * Session callback class
	 */
	private class SessionStatusCallback implements Session.StatusCallback {
		
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	    	// session is opened 
	    	if (state == SessionState.OPENED) {
	    		// if token is empty
	    		if (mAuthToken.equals("")) {
	    			// save current token to storage
	    			saveToken(session.getAccessToken());
	    			
	    			Log.i(Constants.LOG_TAG, "Got new auth token");
	    		}
	    		
	    		DbHelper db = new DbHelper(MainActivity.this);
	    		db.open();
	    		boolean isDbEmpty = db.isDbEmpty();
	    		db.close();
	    		
	    		// if database is empty
	    		if (isDbEmpty) {
	    			// and if network is up
	    			if (Functions.isNetworkConnected(getApplicationContext()))
	    				// retrieve user data
		    			facebookGetUserInfo();
	    			else {
	    				// otherwise tell user to find connection
	    				noConnectionDialog();
	    			}
	    		} else {
	    			loadTabs();
	    		}
	    	}
	    	
	    	// authorization failure or another problem
	    	if (state == SessionState.CLOSED_LOGIN_FAILED) {
	    		new AlertDialog.Builder(MainActivity.this)
	    			.setTitle(R.string.dialog_login)
	    			.setMessage(R.string.login_failed)
	    			.setIcon(android.R.drawable.ic_dialog_alert)
	    			.setPositiveButton(R.string.try_again,
						new DialogInterface.OnClickListener() {
					
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								
								// try to start session again
								facebookLogin();
							}
							
						})
					.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();							
						}
						
					})
					.setCancelable(false)
					.show();
	    	}
	    }
	    
	}

	/**
	 * Tabs adapter class
	 */
	private static class TabsAdapter extends FragmentPagerAdapter implements
			TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final TabHost mTabHost;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		static final class TabInfo {
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(Class<?> _class, Bundle _args) {
				clss = _class;
				args = _args;
			}
		}

		static class DummyTabFactory implements TabHost.TabContentFactory {
			private final Context mContext;

			public DummyTabFactory(Context context) {
				mContext = context;
			}

			@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabsAdapter(FragmentActivity activity, TabHost tabHost,
				ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mTabHost = tabHost;
			mViewPager = pager;
			mTabHost.setOnTabChangedListener(this);
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
			tabSpec.setContent(new DummyTabFactory(mContext));

			TabInfo info = new TabInfo(clss, args);
			mTabs.add(info);
			mTabHost.addTab(tabSpec);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			return Fragment.instantiate(mContext, info.clss.getName(),
					info.args);
		}

		@Override
		public void onTabChanged(String tabId) {
			int position = mTabHost.getCurrentTab();
			mViewPager.setCurrentItem(position);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			TabWidget widget = mTabHost.getTabWidget();
			int oldFocusability = widget.getDescendantFocusability();
			widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			mTabHost.setCurrentTab(position);
			widget.setDescendantFocusability(oldFocusability);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}
		
	}

}
