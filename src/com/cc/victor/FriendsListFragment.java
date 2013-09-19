package com.cc.victor;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.friends_list, container, false);

		setHasOptionsMenu(true);
		
		mAdapter = new FriendsListAdapter(getActivity(), mFriends);
		mListView = (ListView) view.findViewById(R.id.friends_listview);
		mListView.setAdapter(mAdapter);

		return view;
	}
	
	@Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        
        if (visible) {
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
		mSession = new Session(getActivity());
		mSession.openForRead(new Session.OpenRequest(this));
		
		if (mSession.isOpened()) {
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setMessage(getString(R.string.loading_friends_list));
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
			
			Request.newMyFriendsRequest(mSession, new GraphUserListCallback() {
				
				@Override
				public void onCompleted(List<GraphUser> users, Response response) {
					mProgressDialog.dismiss();
					
					if (response.getError() == null) {
		        		mAdapter.clear();
						for (GraphUser friend : users) {
							mFriends.add(friend);	
						}
		            	mAdapter.notifyDataSetChanged();	
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
								}
								
							})
						.setCancelable(false)
						.show();
					}
				}
				
			}).executeAsync();
		}
	}
	
}
