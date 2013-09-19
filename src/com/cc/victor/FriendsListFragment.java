package com.cc.victor;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
	
	private Session mSession;
	private ArrayList<GraphUser> mFriends = new ArrayList<GraphUser>();
	
	private FriendsListAdapter mAdapter;
	
	private ProgressDialog mProgressDialog;
	private ListView mListView;
	private TextView mNoFriendsText;
	private Button mReloadFriendsButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.friends_list, container, false);

		setHasOptionsMenu(true);
		
		mAdapter = new FriendsListAdapter(getActivity(), mFriends);
		mListView = (ListView) view.findViewById(R.id.friends_listview);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				try {
					getActivity().getPackageManager().getPackageInfo("com.facebook.katana", 0);
				    startActivity(new Intent(Intent.ACTION_VIEW,
				    		Uri.parse(new StringBuilder().append("fb://profile/")
				    				.append(mAdapter.getItem(position).getId()).toString())));
				} catch (Exception e) {
					startActivity(new Intent(Intent.ACTION_VIEW,
							Uri.parse(mAdapter.getItem(position).getLink())));
				}
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
        
        if (visible) {
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
	
	private void getFriendsList() {		
		if (!Functions.isNetworkConnected(getActivity())) {
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
		
		mSession = new Session(getActivity());
		mSession.openForRead(new Session.OpenRequest(this));
		
		if (mSession.isOpened()) {
			mNoFriendsText.setVisibility(View.GONE);
			mReloadFriendsButton.setVisibility(View.GONE);
			
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setMessage(getString(R.string.loading_friends_list));
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
			
			Request.newMyFriendsRequest(mSession, new GraphUserListCallback() {
				
				@Override
				public void onCompleted(List<GraphUser> users, Response response) {
					mProgressDialog.dismiss();
					
					if (response.getError() == null) {
		        		if (users.size() != 0) {		        			
		        			mNoFriendsText.setVisibility(View.GONE);
		        			mReloadFriendsButton.setVisibility(View.GONE);
		        			
		        			mAdapter.clear();
							for (GraphUser friend : users) {
								mFriends.add(friend);	
							}
			            	mAdapter.notifyDataSetChanged();		        			
		        		}		        				
					} else {		        				        			
						new AlertDialog.Builder(getActivity())
		    			.setTitle(R.string.get_data)
		    			.setMessage(R.string.get_friends_error)
		    			.setIcon(android.R.drawable.ic_dialog_alert)
		    			.setPositiveButton(R.string.try_again,
		    					new DialogInterface.OnClickListener() {
						
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									
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
