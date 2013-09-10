package com.cc.victor;

public class UserInfo {
	
	private String mName;
	private String mSurname;
	private long mDateOfBirth;
	private String mBio;
	private String mPhone;
	private String mEmail;
	
	public UserInfo() {
		mName = mSurname = mBio = mPhone = mEmail = "";
		mDateOfBirth = 0L;
	}
	
	public UserInfo(String mName, String mSurname, long mDateOfBirth,
			String mBio, String mPhone, String mEmail) {
		this.mName = mName;
		this.mSurname = mSurname;
		this.mDateOfBirth = mDateOfBirth;
		this.mBio = mBio;
		this.mPhone = mPhone;
		this.mEmail = mEmail;
	}
	
	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public String getSurname() {
		return mSurname;
	}

	public void setSurname(String mSurname) {
		this.mSurname = mSurname;
	}

	public long getDateOfBirth() {
		return mDateOfBirth;
	}

	public void setDateOfBirth(long mDateOfBirth) {
		this.mDateOfBirth = mDateOfBirth;
	}

	public String getBio() {
		return mBio;
	}

	public void setBio(String mBio) {
		this.mBio = mBio;
	}

	public String getPhone() {
		return mPhone;
	}

	public void setPhone(String mPhone) {
		this.mPhone = mPhone;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setEmail(String mEmail) {
		this.mEmail = mEmail;
	}

}
