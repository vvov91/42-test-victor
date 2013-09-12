package com.cc.victor;

import java.util.ArrayList;


import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.Session;
import com.facebook.SessionState;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;

public class MainActivity extends SherlockFragmentActivity {

	private TabHost mTabHost;
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	
	private Session mSession;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private String mAuthToken = "";
	
	private DbHelper mDb;
	
	private SharedPreferences mPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mAuthToken = mPrefs.getString("auth_token", "");
		
		if (mAuthToken.equals("")) {
			facebookLogin();
			
			mDb = new DbHelper(this);
			mDb.open();
			boolean isDbEmpty = mDb.isDbEmpty();
			mDb.close();
			
			if (isDbEmpty) {
				facebookGetUserInfo();
			}
		} else {
			loadTabs(savedInstanceState);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mTabHost != null) 
			outState.putString("tab", mTabHost.getCurrentTabTag());
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
		case R.id.logout: 
			if (mSession == null) {
				mSession = new Session(this);
			}
			mSession.closeAndClearTokenInformation();
			saveToken("");
			
			finish();
			
			break;
		}
		
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void loadTabs(Bundle savedInstanceState) {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);

		mTabsAdapter.addTab(mTabHost.newTabSpec(getString(R.string.me))
				.setIndicator(getString(R.string.me)), MeFragment.class, null);
		mTabsAdapter.addTab(mTabHost.newTabSpec(getString(R.string.about))
				.setIndicator(getString(R.string.about)), AboutFragment.class, null);

		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}
	}
	
	private void facebookLogin() {
		mSession = new Session(this);
		mSession.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
	}
	
	private void facebookGetUserInfo() {
		/*Toast.makeText(getApplicationContext(), "Login token: "
                + session.getAccessToken(), Toast.LENGTH_SHORT).show();
		
		if (Session.getActiveSession().isOpened()) {
			Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback() {

				@Override
				public void onCompleted(GraphUser user,
						Response response) {
					Log.d(Constants.LOG_TAG, "got callback");
					
					if (user != null) {
						Toast.makeText(getApplicationContext(), "Hello "
                                + user.getName() + "!", Toast.LENGTH_SHORT).show();
						Toast.makeText(getApplicationContext(), "Login token: "
                                + session.getAccessToken(), Toast.LENGTH_SHORT).show();
                    }
				} 
				
			});
		}*/
	}
	
	private void saveToken(String token) {
		Editor prefsEditor = mPrefs.edit();
		prefsEditor.putString("auth_token", token);
		prefsEditor.commit();
		
		mAuthToken = token;
	}
	
	private class SessionStatusCallback implements Session.StatusCallback {
		
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	    	if (state == SessionState.OPENED && mAuthToken.equals("")) {
	    		saveToken(session.getAccessToken());
	    		
	    		loadTabs(null);
	    	}
	    	
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

	public static class TabsAdapter extends FragmentPagerAdapter implements
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
