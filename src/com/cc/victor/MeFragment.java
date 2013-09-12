package com.cc.victor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class MeFragment extends SherlockFragment {
	
	private View mView;
	private String mPhotoPath;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.me, container, false);
				
		mPhotoPath = Environment.getExternalStorageDirectory() + "/Android/data/" + 
				getActivity().getPackageName();
		

		Button loadPhotoButton = (Button) mView.findViewById(R.id.load_photo_button);
		loadPhotoButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
				String userId = prefs.getString("user_id", "");
				if (!userId.equals(""))
					new DownloadPhoto().execute(userId);
			}
			
		});
		
		ImageView photo = (ImageView) mView.findViewById(R.id.user_photo);
		TextView name = (TextView) mView.findViewById(R.id.name_value);		
		TextView surname = (TextView) mView.findViewById(R.id.surname_value);
		TextView dateOfBirth = (TextView) mView.findViewById(R.id.date_of_birth_value);
		TextView bio = (TextView) mView.findViewById(R.id.bio_value);
		TextView link = (TextView) mView.findViewById(R.id.link_value);
		TextView email = (TextView) mView.findViewById(R.id.email_value);
		
		DbHelper db = new DbHelper(getActivity());
		db.open();		
		UserInfo info = db.getUserInfo();		
		db.close();
		
		name.setText(info.getName());
		surname.setText(info.getSurname());
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
		sdf.setTimeZone(TimeZone.getDefault());
		dateOfBirth.setText(sdf.format(new Date(info.getDateOfBirth())));
		bio.setText(info.getBio());
		link.setText(info.getLink());
		email.setText(info.getEmail());
		
		File photoFile = new File(mPhotoPath + "/photo.jpg");
		if (!photoFile.exists()) {
			photoFile = new File(mPhotoPath);
			photoFile.mkdirs();
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			String userId = prefs.getString("user_id", "");
			if (!userId.equals(""))
				new DownloadPhoto().execute(userId);
		} else {
			photo.setImageDrawable(Drawable.createFromPath(mPhotoPath + "/photo.jpg"));
		}

		return mView;
	}
	
	private class DownloadPhoto extends AsyncTask<String, Integer, Boolean> {
		
		private ProgressBar mPhotoProgressBar;
		private ImageView mPhoto;
		private Button mLoadPhotoButton;
		
		@Override
		protected void onPreExecute() {	
			mPhoto = (ImageView) mView.findViewById(R.id.user_photo);
			mLoadPhotoButton = (Button) mView.findViewById(R.id.load_photo_button);
			mPhoto.setVisibility(View.GONE);
			mLoadPhotoButton.setVisibility(View.GONE);
			
			mPhotoProgressBar = (ProgressBar) mView.findViewById(R.id.photo_progressbar);
			mPhotoProgressBar.setVisibility(View.VISIBLE);
			mPhotoProgressBar.setMax(100);
			mPhotoProgressBar.setProgress(0);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				URL url = new URL("http://graph.facebook.com/" + params[0] + "/picture");

				URLConnection conection = url.openConnection();
		        conection.connect();
		        int lenghtOfFile = conection.getContentLength();
		        InputStream input = new BufferedInputStream(url.openStream(), 8192);
		        OutputStream output = new FileOutputStream(mPhotoPath + "/photo.jpg");

		        byte data[] = new byte[1024];
		        long total = 0;
		        int count;
		        while ((count = input.read(data)) != -1) {
		            total += count;

		            publishProgress((int) (total * 100) / lenghtOfFile);

		            output.write(data, 0, count);
		        }
		        output.flush();
		        output.close();
		        input.close();
			} catch (IOException e) {
				return false;
			}
			
			return true;
		}

		private void publishProgress(Integer progress) {
			mPhotoProgressBar.setProgress(progress);
		}
		
		@Override
		protected void onPostExecute(Boolean photoDownloaded) {
			super.onPostExecute(photoDownloaded);
			
			mPhotoProgressBar.setVisibility(View.GONE);		
						
			if (photoDownloaded) {
				mPhoto.setVisibility(View.VISIBLE);
				mPhoto.setImageDrawable(Drawable.createFromPath(mPhotoPath + "/photo.jpg"));
			} else {
				mLoadPhotoButton.setVisibility(View.VISIBLE);
			}
		}
		
	}

}
