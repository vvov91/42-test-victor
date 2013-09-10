package com.cc.victor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class MeFragment extends SherlockFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.me, container, false);
		
		TextView name = (TextView) view.findViewById(R.id.name_value);		
		TextView surname = (TextView) view.findViewById(R.id.surname_value);
		TextView dateOfBirth = (TextView) view.findViewById(R.id.date_of_birth_value);
		TextView bio = (TextView) view.findViewById(R.id.bio_value);
		TextView phone = (TextView) view.findViewById(R.id.phone_value);
		TextView email = (TextView) view.findViewById(R.id.email_value);
		
		DbHelper db = new DbHelper(getActivity());
		db.open();
		
		UserInfo info = db.getUserInfo();
		name.setText(info.getName());
		surname.setText(info.getSurname());
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
		sdf.setTimeZone(TimeZone.getDefault());
		dateOfBirth.setText(sdf.format(new Date(info.getDateOfBirth())));
		bio.setText(info.getBio());
		phone.setText(info.getPhone());
		email.setText(info.getEmail());
		
		db.close();

		return view;
	}

}
