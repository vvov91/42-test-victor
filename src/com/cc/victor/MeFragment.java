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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.internal.view.menu.MenuWrapper;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * Fragment with user data
 * 
 * @author Victor Vovchenko <vitek91@gmail.com>
 *
 */
public class MeFragment extends SherlockFragment {
	
	private View mView;						// current view
	private String mPhotoPath;				// path to photo file
	
	private boolean mEditing;
	
	private EditText mNameEdit;
	private EditText mSurnameEdit;
	private EditText mBioEdit;
	private EditText mLinkEdit;
	private EditText mEmailEdit;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (savedInstanceState != null && savedInstanceState.getBoolean("editing")) {
			mEditing = true;
			
			mView = inflater.inflate(R.layout.edit, container, false);
			
			mNameEdit = (EditText) mView.findViewById(R.id.name_edit);
			mSurnameEdit = (EditText) mView.findViewById(R.id.surname_edit);
			mBioEdit = (EditText) mView.findViewById(R.id.bio_edit);
			mLinkEdit = (EditText) mView.findViewById(R.id.link_edit);
			mEmailEdit = (EditText) mView.findViewById(R.id.email_edit);

			mNameEdit.setText(savedInstanceState.getString("name"));
			mSurnameEdit.setText(savedInstanceState.getString("surname"));
			mBioEdit.setText(savedInstanceState.getString("bio"));
			mLinkEdit.setText(savedInstanceState.getString("link"));
			mEmailEdit.setText(savedInstanceState.getString("email"));
		} else {
			mEditing = false;
			
			mView = inflater.inflate(R.layout.me, container, false);
			
			mPhotoPath = Environment.getExternalStorageDirectory() + "/Android/data/" + 
					getActivity().getPackageName();		

			// photo reload button
			Button loadPhotoButton = (Button) mView.findViewById(R.id.load_photo_button);
			loadPhotoButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					startPhotoDownload();
				}
				
			});
			
			ImageView photo = (ImageView) mView.findViewById(R.id.user_photo);
			TextView name = (TextView) mView.findViewById(R.id.name_value);		
			TextView surname = (TextView) mView.findViewById(R.id.surname_value);
			TextView dateOfBirth = (TextView) mView.findViewById(R.id.date_of_birth_value);
			TextView bio = (TextView) mView.findViewById(R.id.bio_value);
			TextView link = (TextView) mView.findViewById(R.id.link_value);
			TextView email = (TextView) mView.findViewById(R.id.email_value);
			
			// getting user data from database
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
			// check if photo file exists
			if (!photoFile.exists()) {
				// if not
				photoFile = new File(mPhotoPath);
				// make dirs in path to file
				photoFile.mkdirs();
				
				startPhotoDownload();
			} else {
				// if photo already downloaded - display it
				photo.setImageDrawable(Drawable.createFromPath(mPhotoPath + "/photo.jpg"));
			}
		}

		setHasOptionsMenu(true);

		return mView;
	}	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    
	    if (mEditing) {
	    	outState.putBoolean("editing", mEditing);
	    	outState.putString("name", mNameEdit.getText().toString());
	    	outState.putString("surname", mSurnameEdit.getText().toString());
	    	outState.putString("bio", mBioEdit.getText().toString());
	    	outState.putString("link", mLinkEdit.getText().toString());
	    	outState.putString("email", mEmailEdit.getText().toString());
	    } else {
	    	outState.putBoolean("editing", mEditing);	    	
	    }
	}
	
	@Override
    public void onPrepareOptionsMenu(Menu menu) {
		if (mEditing)
			menu.findItem(R.id.edit).setTitle(R.string.save_data);
		
		super.onPrepareOptionsMenu(menu);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.edit:
			switchToEditView();
			
			mEditing = true;
			
			item.setTitle(R.string.save_data);
			
			Toast.makeText(getActivity(), "edit", Toast.LENGTH_SHORT).show();
			break;
		}
		
		return super.onOptionsItemSelected(item);		
	}
	
	private void switchToEditView() {
		ViewGroup parent = (ViewGroup) mView.getParent();
		parent.removeView(mView);
		mView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.edit, parent, false);
		parent.addView(mView);

		mNameEdit = (EditText) mView.findViewById(R.id.name_edit);
		mSurnameEdit = (EditText) mView.findViewById(R.id.surname_edit);
		mBioEdit = (EditText) mView.findViewById(R.id.bio_edit);
		mLinkEdit = (EditText) mView.findViewById(R.id.link_edit);
		mEmailEdit = (EditText) mView.findViewById(R.id.email_edit);
		
		DbHelper db = new DbHelper(getActivity());
		db.open();		
		UserInfo info = db.getUserInfo();		
		db.close();
		
		mNameEdit.setText(info.getName());
		mSurnameEdit.setText(info.getSurname());
		mBioEdit.setText(info.getBio());
		mLinkEdit.setText(info.getLink());
		mEmailEdit.setText(info.getEmail());
	}
	
	/**
	 * Gets user id from app settings storage and downloads user photo
	 */
	private void startPhotoDownload() {
		// get user Id from app settings storage
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String userId = prefs.getString("user_id", "");
		
		// if Id is present
		if (!userId.equals(""))
			// load user photo
			new DownloadPhoto().execute(userId);
	}
	
	/**
	 * AsyncTask for user photo downloading
	 */
	private class DownloadPhoto extends AsyncTask<String, Integer, Boolean> {
		
		private ProgressBar mPhotoProgressBar;
		private ImageView mPhoto;
		private Button mLoadPhotoButton;
		
		@Override
		protected void onPreExecute() {	
			// initalize elements
			mPhoto = (ImageView) mView.findViewById(R.id.user_photo);
			mPhoto.setVisibility(View.GONE);
			
			mLoadPhotoButton = (Button) mView.findViewById(R.id.load_photo_button);
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

		            // publish download progress in percents
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
			// display download progress with progressbar
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
