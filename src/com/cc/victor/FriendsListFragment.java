package com.cc.victor;

import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
	private ProgressDialog mProgressDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.friends_list, container, false);

		setHasOptionsMenu(true);

		return view;
	}
	
	@Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
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
    						Log.d(Constants.LOG_TAG, "users size: " + users.size());
    						Log.d(Constants.LOG_TAG, "users1: " + users.get(1).getId());
    						for (GraphUser user : users) {
    							Log.d(Constants.LOG_TAG, "user: " + user.getName() + " " + user.getLastName());
    						}
    						Toast.makeText(getActivity(), "friend: " + users.get(1).getFirstName() + " " + users.get(1).getLastName(), Toast.LENGTH_LONG).show();
    					}
    				}
    				
    			}).executeAsync();
    		}
        }
    }

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.edit).setVisible(false);
		menu.findItem(R.id.logout).setVisible(true);

		super.onPrepareOptionsMenu(menu);
	}
	
}
