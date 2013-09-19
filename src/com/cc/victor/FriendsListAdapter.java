package com.cc.victor;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.facebook.model.GraphUser;

/**
 * Adapter for friends list listview
 * 
 * @author Victor Vovchenko <vitek91@gmail.com>
 * 
 */
public class FriendsListAdapter extends ArrayAdapter<GraphUser> {
	
	private LayoutInflater mInflater;
	private ArrayList<GraphUser> mUsers = new ArrayList<GraphUser>();

	public FriendsListAdapter(Context context, ArrayList<GraphUser> users) {
		super(context, R.layout.friends_list_item, users);
		mUsers = users;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mUsers.size();
	}

	@Override
	public GraphUser getItem(int position) {
		return mUsers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		ViewHolder viewHolder;

		if (v == null) {
			v = mInflater.inflate(R.layout.friends_list_item, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) v.findViewById(R.id.friend_name);
			viewHolder.photo = (ImageView) v.findViewById(R.id.friend_photo);
			v.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) v.getTag();
		}

		GraphUser user = this.getItem(position);

		viewHolder.name.setText(user.getName());

		return v;
	}

	static class ViewHolder {
		TextView name;
		ImageView photo;
	}
	
}
