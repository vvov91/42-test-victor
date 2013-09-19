package com.cc.victor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.facebook.Request;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

/**
 * Fragment with Facebook friends list
 * 
 * @author Victor Vovchenko <vitek91@gmail.com>
 * 
 */
public class FriendsListFragment extends SherlockFragment {
	
	private ArrayList<Friend> mFriends = new ArrayList<Friend>();
	
	private FriendsListAdapter mAdapter;				// list view adapter
	
	private ProgressDialog mProgressDialog;				// data processing dialog
	private ListView mListView;							// list view with friends data
	private TextView mNoFriendsText;					// no friends text
	private Button mReloadFriendsButton;				// and data

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.friends_list, container, false);

		setHasOptionsMenu(true);
		
		if (savedInstanceState != null) {
			// restore friends list
			mFriends = savedInstanceState.getParcelableArrayList("friends");
		}
		
		mAdapter = new FriendsListAdapter(getActivity(), mFriends);
		mListView = (ListView) view.findViewById(R.id.friends_listview);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				try {
					// try open friend page in native Facebook app
					getActivity().getPackageManager().getPackageInfo("com.facebook.katana", 0);
				    startActivity(new Intent(Intent.ACTION_VIEW,
				    		Uri.parse(new StringBuilder().append("fb://profile/")
				    				.append(mAdapter.getItem(position).getId()).toString())));
				} catch (Exception e) {
					// if app is not installed - go web 
					startActivity(new Intent(Intent.ACTION_VIEW,
							Uri.parse(mAdapter.getItem(position).getLink())));
				}
			}
			
		});
		// choose priority dialog on long click
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position,
					long arg3) {
				new AlertDialog.Builder(getActivity())
				.setTitle(R.string.user_priority)
				.setSingleChoiceItems(new String[] { "0",  "1" },
						// set choosed elem from item
						mFriends.get(position).getPriority(),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// change priority
								mFriends.get(position).setPriority(which);

								// sort friends list
								Collections.sort(mFriends, new SortByPriority());
								mAdapter.notifyDataSetChanged();
								
								dialog.dismiss();								
							}
							
				}).show();
				
				return false;
			}
			
		});
		mNoFriendsText = (TextView) view.findViewById(R.id.no_friends_text);
		mReloadFriendsButton = (Button) view.findViewById(R.id.reload_friends_button);
		mReloadFriendsButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getFriendsList();
			}
			
		});

		return view;
	}
	
	@Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        
        // if "Friends" tab is displayed
        if (visible) {
        	// and if friends list is not loaded
        	if (mFriends.size() == 0)        		
        		getFriendsList();
        }
    }

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.edit).setVisible(false);
		menu.findItem(R.id.logout).setVisible(true);

		super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    
	    // if already loaded friends list
	    if (mFriends.size() != 0) {
	    	// save items
	    	outState.putParcelableArrayList("friends", mFriends);
	    }
	    
	}
	
	/**
	 * Downloads Facebook friends list
	 */
	private void getFriendsList() {	
		Log.i(Constants.LOG_TAG, "Trying to recieve friends list");
		
		// if there is no connection
		if (!Functions.isNetworkConnected(getActivity())) {
			// show dialog
			new AlertDialog.Builder(getActivity())
			.setTitle(R.string.connection)
			.setMessage(R.string.connection_is_out)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setNeutralButton(R.string.connection_settings, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// open wi-fi settings
					startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
				}
				
			})
			.setOnCancelListener(null).show();
			
			mNoFriendsText.setVisibility(View.VISIBLE);
			mReloadFriendsButton.setVisibility(View.VISIBLE);
			
			return;
		}
		
		// start new session (or reopen existing)
		Session session = new Session(getActivity());
		session.openForRead(new Session.OpenRequest(this));
		
		if (session.isOpened()) {
			mNoFriendsText.setVisibility(View.GONE);
			mReloadFriendsButton.setVisibility(View.GONE);
			
			// show "loading" dialog
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setMessage(getString(R.string.loading_friends_list));
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
			
			Request.newMyFriendsRequest(session, new GraphUserListCallback() {
				
				@Override
				public void onCompleted(List<GraphUser> users, Response response) {
					mProgressDialog.dismiss();
					
					if (response.getError() == null) {
		        		if (users.size() != 0) {		        			
		        			mNoFriendsText.setVisibility(View.GONE);
		        			mReloadFriendsButton.setVisibility(View.GONE);
		        			
		        			// save friends list
		        			mAdapter.clear();
							for (GraphUser friend : users) {
								mFriends.add(new Friend(friend.getId(), friend.getName(),
										friend.getLink(), 0));	
							}
							// notify adapter that friends list have changed
			            	mAdapter.notifyDataSetChanged();		 			            	

			        		Log.i(Constants.LOG_TAG,
			        				new StringBuilder().append("Got friend entries (")
			        				.append(users.size()).append(")").toString());
		        		}		        				
					} else {	
						// error while recieving friends list
						new AlertDialog.Builder(getActivity())
		    			.setTitle(R.string.get_data)
		    			.setMessage(R.string.get_friends_error)
		    			.setIcon(android.R.drawable.ic_dialog_alert)
		    			.setPositiveButton(R.string.try_again,
		    					new DialogInterface.OnClickListener() {
						
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									
									// try to start again
									getFriendsList();
								}
								
							})
						.setCancelable(false)
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								
								mNoFriendsText.setVisibility(View.VISIBLE);
								mReloadFriendsButton.setVisibility(View.VISIBLE);
							}
							
						}).show();
					}
				}
				
			}).executeAsync();
		}
	}
	
}
