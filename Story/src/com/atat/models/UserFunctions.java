package com.atat.models;

import android.content.Context;

public class UserFunctions {

	/**
	 * Function get Login status
	 * */
	public boolean isUserLoggedIn(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);

		int count = db.getRowCount();
		if (count > 0) {
			// user logged in
			return true;
		}
		return false;
	}

	/**
	 * Function to logout user Reset Database
	 * */
	public boolean logoutUser(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		db.resetTables();
		return true;
	}

	public String getUserName(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		return db.getUserDetails().get("username");
	}

	public String getApiKey(Context context) {

		DatabaseHandler db = new DatabaseHandler(context);
		return db.getUserDetails().get("api_key");
	}

	public boolean isAdmin(Context context) {
		DatabaseHandler db = new DatabaseHandler(context);
		if (db.getUserDetails().get("type") != null)
			if (db.getUserDetails().get("type").equals("1"))
				return true;
		return false;
	}

	public boolean isUerLogout(Context context){
		DatabaseHandler db = new DatabaseHandler(context);
		if (db.getRowCount()>0) return false;
		return true;
	}
}

