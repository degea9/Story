package com.atat.story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.atat.json.JSONParser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;

import android.content.Context;
import android.content.DialogInterface;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import android.util.Log;

import android.widget.Toast;

@SuppressLint("ValidFragment")
public class ManageDialog extends DialogFragment {

	// Progress Dialog
	private ProgressDialog pDialog;
	// url to get all chapters list
	private static String url_admin_actionBlock = "http://192.168.22.1/story/slim/v1//admin/user/block/id";
	private static String url_admin_actionActive = "http://192.168.22.1/story/slim/v1//admin/user/active/id";
	// private static String url_login_user =
	// "http://tuandang.esy.es/story/android_connect/login.php";
	// Creating JSON Parser object
	JSONParser jParser;
	// JSON Node names
	private static final String TAG_ERROR = "error";
	private String error;

	JSONArray user_details;
	ArrayList<HashMap<String, String>> userList ; 
	ManageUserActivity manageUser;
	private String uid;
	private int position;

	public ManageDialog(String uid,ManageUserActivity manageUser,ArrayList<HashMap<String, String>> userList,int position) {
		this.manageUser = manageUser;
		this.userList = userList;
		this.uid = uid;
		this.position=position;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		jParser = new JSONParser(getActivity().getApplicationContext());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.manage_user).setItems(R.array.manage_actions,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							Log.d("block", "block");
							new ManageAction(getActivity()
									.getApplicationContext()).execute("BLOCK");
							break;
						case 2:
							ManageDialog.this.getDialog().cancel();
							FragmentManager fm = getActivity()
									.getSupportFragmentManager();
							ManageUserCommentsDialog manageUserComments = new ManageUserCommentsDialog(
									uid);
							manageUserComments.show(fm, "manage_user_comment");
							break;

						case 1:
							Log.d("active", "active");
							new ManageAction(getActivity()
									.getApplicationContext()).execute("ACTIVE");
							break;
						default:
							break;

						}

					}
				});
		return builder.create();
	}

	class ManageAction extends AsyncTask<String, String, String> {
		private Context context;

		public ManageAction(Context context) {
			this.context = context;
		}

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Loading . Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting All chapters from url
		 * */
		protected String doInBackground(String... action) {
			String status;
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("uid", uid));
			JSONObject json;
			if (action[0].equals("BLOCK")) {
				// getting JSON string from URL
				json = jParser.makeHttpRequest(url_admin_actionBlock, "PUT",
						params);
				status = "BLOCK";
			} else{
				json = jParser.makeHttpRequest(url_admin_actionActive, "PUT",
						params);
				status = "ACTIVE";
			}
			// Check your log cat for JSON reponse
			Log.d("json: ", json.toString());
			try {
				// Checking for SUCCESS TAG
				error = json.getString(TAG_ERROR);

			} catch (JSONException e) {
				e.printStackTrace();
			}

			return status;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String status) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			if (error.equals("false")) {
				if (status.equals("BLOCK")){
					Toast.makeText(context, "Blocked", Toast.LENGTH_SHORT)
							.show();
					userList.get(position).put("type", "2");
				}
				else{
					Toast.makeText(context, "Actived", Toast.LENGTH_SHORT)
							.show();
					userList.get(position).put("type", "0");
				}
				
				manageUser.changeAdapter(userList);

			} else {
				if (status.equals("BLOCK"))
					Toast.makeText(context,
							"failed to block. Please try again!",
							Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(context,
							"failed to active. Please try again!",
							Toast.LENGTH_SHORT).show();
			}
		}

	}
}
