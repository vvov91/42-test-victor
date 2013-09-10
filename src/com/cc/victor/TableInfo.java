package com.cc.victor;

public class TableInfo {
	
	public final static String TABLE_NAME = "Info";

	public final static String NAME = "name";
	public final static String SURNAME = "surname";
	public final static String DATEOFBIRTH = "date_of_birth";
	public final static String BIO = "bio";
	public final static String PHONE = "phone";
	public final static String EMAIL = "email";
	
	public final static String CREATE_QUERY = new StringBuilder()
		.append("CREATE TABLE ").append(TABLE_NAME).append(" (_id INTEGER PRIMARY KEY, ")
		.append(NAME).append(" TEXT NOT NULL, ")
		.append(SURNAME).append(" TEXT NOT NULL, ")
		.append(DATEOFBIRTH).append(" TEXT, ")
		.append(BIO).append(" TEXT, ")
		.append(PHONE).append(" TEXT, ")
		.append(EMAIL).append(" TEXT);").toString();

}