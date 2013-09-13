package com.cc.victor;

/**
 * User information object
 * 
 * @author Victor Vovchenko <vitek91@gmail.com>
 *
 */
public class UserInfo {
	
	private String mName;								// user first name
	private String mSurname;							// user last name
	private long mDateOfBirth;							// date of birth in ms
	private String mBio;								// bio
	private String mLink;								// Facebook profile link
	private String mEmail;								// user's email
	
	public UserInfo() {
		mName = mSurname = mBio = mLink = mEmail = "";
		mDateOfBirth = 0L;
	}
	
	public UserInfo(String mName, String mSurname, long mDateOfBirth,
			String mBio, String mLink, String mEmail) {
		this.mName = mName;
		this.mSurname = mSurname;
		this.mDateOfBirth = mDateOfBirth;
		this.mBio = mBio;
		this.mLink = mLink;
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

	public String getLink() {
		return mLink;
	}

	public void setLink(String mLink) {
		this.mLink = mLink;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setEmail(String mEmail) {
		this.mEmail = mEmail;
	}

}
