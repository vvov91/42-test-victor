package com.cc.victor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import com.actionbarsherlock.view.Window;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;
import android.widget.DatePicker.OnDateChangedListener;

/**
 * Date picking dialog activity
 * 
 * @author Victor Vovchenko <vitek91@gmail.com>
 * 
 */
public class DatePickerActivity extends Activity {
	
	private String mDate = ""; 
	
	private final SimpleDateFormat mSdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature((int) Window.FEATURE_NO_TITLE);	
		setContentView(R.layout.activity_datepicker);			

		mSdf.setTimeZone(TimeZone.getDefault());
		
		Calendar today = Calendar.getInstance();
		try {
			// parsing date from user data
			today.setTimeInMillis(mSdf.parse(getIntent().getStringExtra("date")).getTime());
		} catch (ParseException e) { }
		
		DatePicker datepicker = (DatePicker) findViewById(R.id.datepicker);
		datepicker.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), new OnDateChangedListener() {
					
					@Override
					public void onDateChanged(DatePicker view, int year, int monthOfYear,
							int dayOfMonth) {
						mDate = new StringBuilder().append(dayOfMonth).append("-")
								.append(monthOfYear + 1).append("-").append(year).toString();
					}
					
				});
	}
	
	public void okButtonClick(View v) {		
		long date = 0;
		try {
			// if nothing changed
			if (mDate.equals("")) {
				// set date from user data
				mDate = getIntent().getStringExtra("date");
			}
			
			date = mSdf.parse(mDate).getTime();
		} catch (ParseException e) { }
		
		// if selected date is more than now
		if (date > System.currentTimeMillis()) {
			Toast.makeText(getApplicationContext(),
					R.string.dateofbitrh_error, Toast.LENGTH_LONG).show();

			// do nothing
			return;
		}		
		
		setResult(RESULT_OK, new Intent().putExtra("date", date));
		
		finish();
	}
}
