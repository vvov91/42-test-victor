package com.cc.victor;

import java.io.File;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

/**
 * Adapter for friends list listview
 * 
 * @author Victor Vovchenko <vitek91@gmail.com>
 * 
 */
public class FriendsListAdapter extends ArrayAdapter<Friend> {
	
	private LayoutInflater mInflater;
	private ArrayList<Friend> mUsers = new ArrayList<Friend>();
	
	private ImageLoaderConfiguration mImageLoaderConfig;
	
	public FriendsListAdapter(Context context, ArrayList<Friend> users) {
		super(context, R.layout.friends_list_item, users);
		mUsers = users;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		File cacheDir = StorageUtils.getCacheDirectory(context);
		
		// turn on caching
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
        	.cacheInMemory(true)
        	.cacheOnDisc(true)
        	.showStubImage(R.drawable.no_photo)
        	.build();
		
		// configuring Universal Image Loader
		mImageLoaderConfig = new ImageLoaderConfiguration.Builder(context)
		        .threadPoolSize(5)
		        .threadPriority(Thread.NORM_PRIORITY - 1)
		        .tasksProcessingOrder(QueueProcessingType.FIFO)
		        .denyCacheImageMultipleSizesInMemory()
		        .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
		        .discCache(new UnlimitedDiscCache(cacheDir))
		        .imageDownloader(new BaseImageDownloader(context))
		        .imageDecoder(new BaseImageDecoder(true))
		        .defaultDisplayImageOptions(defaultOptions)
		        .build();		
		
		// initializing Universal Image Loader
		if (!ImageLoader.getInstance().isInited())
			ImageLoader.getInstance().init(mImageLoaderConfig);
	}

	@Override
	public int getCount() {
		return mUsers.size();
	}

	@Override
	public Friend getItem(int position) {
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

		Friend user = this.getItem(position);
		
		viewHolder.name.setText(user.getName());
		ImageLoader.getInstance().displayImage(
				"http://graph.facebook.com/" + user.getId() + "/picture", viewHolder.photo);

		return v;
	}

	static class ViewHolder {
		TextView name;
		ImageView photo;
	}
	
}
