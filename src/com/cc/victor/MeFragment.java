package com.cc.victor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
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
	
	private boolean mEditing;				// flag of editing state
	
	private EditText mNameEdit;
	private EditText mSurnameEdit;
	private TextView mDateOfBirth;
	private EditText mBioEdit;
	private EditText mLinkEdit;
	private EditText mEmailEdit;
	
	private final SimpleDateFormat mSdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			final Bundle savedInstanceState) {
		mSdf.setTimeZone(TimeZone.getDefault());
		
		// if got saved instances and in editing state
		if (savedInstanceState != null && savedInstanceState.getBoolean("editing")) {
			mEditing = true;
			
			// display editing layout
			mView = inflater.inflate(R.layout.edit, container, false);
			
			mNameEdit = (EditText) mView.findViewById(R.id.name_edit);
			mSurnameEdit = (EditText) mView.findViewById(R.id.surname_edit);
			mDateOfBirth = (TextView) mView.findViewById(R.id.date_of_birth_value);
			mDateOfBirth.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					startActivityForResult(new Intent(getActivity(), DatePickerActivity.class)
						.putExtra("date", savedInstanceState.getString("dateofbirth")), 0);
				}
				
			});
			mBioEdit = (EditText) mView.findViewById(R.id.bio_edit);
			mLinkEdit = (EditText) mView.findViewById(R.id.link_edit);
			mEmailEdit = (EditText) mView.findViewById(R.id.email_edit);

			// set values from previous state
			mNameEdit.setText(savedInstanceState.getString("name"));
			mSurnameEdit.setText(savedInstanceState.getString("surname"));
			mDateOfBirth.setText(savedInstanceState.getString("dateofbirth"));
			mBioEdit.setText(savedInstanceState.getString("bio"));
			mLinkEdit.setText(savedInstanceState.getString("link"));
			mEmailEdit.setText(savedInstanceState.getString("email"));
		} else {
			// if in normal state
			mEditing = false;
			
			// display data layout
			mView = inflater.inflate(R.layout.me, container, false);
			
			initStandartView();
		}

		setHasOptionsMenu(true);

		return mView;
	}	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    
	    if (mEditing) {
		    // save input values
	    	outState.putBoolean("editing", mEditing);
	    	outState.putString("name", mNameEdit.getText().toString());
	    	outState.putString("surname", mSurnameEdit.getText().toString());
	    	outState.putString("dateofbirth", mDateOfBirth.getText().toString());
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
			if (mEditing) {			
				// check if data is correct
				if (isDataCorrect()) {
					mEditing = false;
					
					// save
					saveData();
					
					// replace layout with new data
					switchToStandartView();
					
					item.setTitle(R.string.edit_data);

					Toast.makeText(getActivity(), 
							R.string.data_saved, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getActivity(), 
							R.string.data_input_error, Toast.LENGTH_LONG).show();
				}
			} else {				
				mEditing = true;
				
				// replace current layout with editing layout
				switchToEditView();
				
				item.setTitle(R.string.save_data);
			}
			break;
		}
		
		return super.onOptionsItemSelected(item);		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			// recieve and display new date
			mDateOfBirth.setText(mSdf.format(new Date(data.getLongExtra("date", 0))));
		}
	}
	
	/**
	 * Checks if input data is correct
	 * 
	 * @return true if everything ok, false otherwise
	 */
	private boolean isDataCorrect() {
		boolean result = true;
		
		// if first name is empty
		if (mNameEdit.getText().toString().equals("")) {
			result = false;
			
			mNameEdit.setBackgroundColor(getResources().getColor(R.color.edit_text_error));
		}
		
		// if last name is empty
		if (mSurnameEdit.getText().toString().equals("")) {
			result = false;
			
			mSurnameEdit.setBackgroundColor(getResources().getColor(R.color.edit_text_error));			
		}
		
		// if Facebook link is not correct
		if (!URLUtil.isValidUrl(mLinkEdit.getText().toString())) {
			result = false;
		
			mLinkEdit.setBackgroundColor(getResources().getColor(R.color.edit_text_error));	
		}
		
		// if email is not correct
		if (!mEmailEdit.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
			result = false;
		
			mEmailEdit.setBackgroundColor(getResources().getColor(R.color.edit_text_error));	
		}
		
		return result;
	}
	
	/**
	 * Replaces current layout with editing layout
	 */
	private void switchToEditView() {
		ViewGroup parent = (ViewGroup) mView.getParent();
		parent.removeView(mView);
		mView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.edit, parent, false);
		parent.addView(mView);
				
		// get user data
		DbHelper db = new DbHelper(getActivity());
		db.open();		
		final UserInfo info = db.getUserInfo();		
		db.close();

		mNameEdit = (EditText) mView.findViewById(R.id.name_edit);
		mSurnameEdit = (EditText) mView.findViewById(R.id.surname_edit);
		mDateOfBirth = (TextView) mView.findViewById(R.id.date_of_birth_value);
		mDateOfBirth.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getActivity(), DatePickerActivity.class)
					.putExtra("date", mSdf.format(new Date(info.getDateOfBirth()))), 0);
			}
			
		});
		mBioEdit = (EditText) mView.findViewById(R.id.bio_edit);
		mLinkEdit = (EditText) mView.findViewById(R.id.link_edit);
		mEmailEdit = (EditText) mView.findViewById(R.id.email_edit);		
		
		// put user data in editing fields
		mNameEdit.setText(info.getName());
		mSurnameEdit.setText(info.getSurname());
		mDateOfBirth.setText(mSdf.format(new Date(info.getDateOfBirth())));
		if (info.getBio().length() == 0)
			mBioEdit.setText(getString(R.string.not_specified));
		else 
			mBioEdit.setText(info.getBio());			
		mLinkEdit.setText(info.getLink());		
		if (info.getEmail().length() == 0)
			mEmailEdit.setText(getString(R.string.not_specified));
		else 
			mEmailEdit.setText(info.getEmail());	
	}
	
	/**
	 * Replaces editing layout with current layout
	 */
	private void switchToStandartView() {
		ViewGroup parent = (ViewGroup) mView.getParent();
		parent.removeView(mView);
		mView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.me, parent, false);
		parent.addView(mView);

		initStandartView();
	}
	
	/**
	 * Initializes normal view
	 */
	private void initStandartView() {
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
		dateOfBirth.setText(mSdf.format(new Date(info.getDateOfBirth())));
		if (info.getBio().length() == 0)
			bio.setText(getString(R.string.not_specified));
		else 
			bio.setText(info.getBio());			
		link.setText(info.getLink());		
		if (info.getEmail().length() == 0)
			email.setText(getString(R.string.not_specified));
		else 
			email.setText(info.getEmail());	
		
		File photoFile = new File(Constants.USER_PHOTO_FILE_PATH + "/photo.jpg");
		// check if photo file exists
		if (!photoFile.exists()) {
			// if not
			photoFile = new File(Constants.USER_PHOTO_FILE_PATH);
			// make dirs in path to file
			photoFile.mkdirs();
			
			startPhotoDownload();
		} else {
			// if photo already downloaded - display it
			photo.setImageDrawable(Drawable.createFromPath(Constants.USER_PHOTO_FILE_PATH
					+ "/photo.jpg"));
		}
	}
	
	/**
	 * Collects input data from editing fields and pastes it into database
	 */
	private void saveData() {
		UserInfo info = new UserInfo();
		info.setName(mNameEdit.getText().toString().trim());
		info.setSurname(mSurnameEdit.getText().toString().trim());
		try {
			info.setDateOfBirth(mSdf.parse(mDateOfBirth.getText().toString()).getTime());
		} catch (ParseException e) { }
		info.setBio(mBioEdit.getText().toString().trim());
		info.setLink(mLinkEdit.getText().toString().trim());
		info.setEmail(mEmailEdit.getText().toString().trim());
		
		DbHelper db = new DbHelper(getActivity());
		db.open();		
		db.updateUserInfo(info);
		db.close();
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
		        OutputStream output = new FileOutputStream(Constants.USER_PHOTO_FILE_PATH 
		        		+ "/photo.jpg");

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
				mPhoto.setImageDrawable(Drawable.createFromPath(Constants.USER_PHOTO_FILE_PATH
						+ "/photo.jpg"));
			} else {
				mLoadPhotoButton.setVisibility(View.VISIBLE);
			}
		}
		
	}

}
