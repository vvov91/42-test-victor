package com.cc.victor;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Friend class, parcelable
 * 
 * @author Victor Vovchenko <vitek91@gmail.com>
 *
 */
public class Friend implements Parcelable {

	private String mId;
	private String mName;
	private String mLink;
	
	public Friend(String mId, String mName, String mLink) {
		this.mId = mId;
		this.mName = mName;
		this.mLink = mLink;
	}
	
	public Friend(Parcel in) {
		String[] data = new String[3];
		in.readStringArray(data);
		
		mId = data[0];
		mName = data[1];
		mLink = data[2];
	}
	
	public Friend() {
		this.mId = "";
		this.mName = "";
		this.mLink = "";
	}
	
	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}
	
	public String getLink() {
		return mLink;
	}

	public void setLink(String mLink) {
		this.mLink = mLink;
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(new String[] { mId, mName, mLink });
	}
	
	public static final Parcelable.Creator<Friend> CREATOR = new Parcelable.Creator<Friend>() {

		@Override
		public Friend createFromParcel(Parcel source) {
			return new Friend(source);
		}

		@Override
		public Friend[] newArray(int size) {
			return new Friend[size];
		}
		
	};
	
}
