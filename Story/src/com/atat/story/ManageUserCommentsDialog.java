package com.atat.story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.atat.image.AllCommentAdapter;

import com.atat.json.JSONParser;
import com.atat.models.UserFunctions;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint("ValidFragment")
public class ManageUserCommentsDialog extends DialogFragment {
	private View inflator;
	// private User user;
	// Progress Dialog
	private ProgressDialog pDialog;
	// url to get all chapters list
	// private static String url_login_user =
	// "http://192.168.22.1/story/android_connect/login.php";
	// private static String url_login_user =
	// "http://tuandang.esy.es/story/android_connect/login.php";
	private static String url_user_comments = "http://192.168.22.1/story/slim/v1/admin/comment/user/";
	// Creating JSON Parser object
	JSONParser jParser ;
	// JSON Node names
	private static final String TAG_ERROR = "error";
	private String error;
	
	private ArrayList<HashMap<String, String>> commentList = new ArrayList<HashMap<String, String>>();
	AllCommentAdapter adapter;
	ListView lv_comments;
	JSONArray comments = null;
	String uid;

	public ManageUserCommentsDialog(String uid) {
		this.uid = uid;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		jParser = new JSONParser(getActivity().getApplicationContext());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		inflator = inflater.inflate(R.layout.admin_manage_comment, null);
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout

		builder.setView(inflator);
		return builder.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		final Button bt_comment = (Button) inflator
				.findViewById(R.id.bt_delete_comment);
		bt_comment.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new DeleteComment().execute();
			}
		});

		lv_comments = (ListView) inflator.findViewById(R.id.list_comment);
		new LoadAllComments().execute();
	}

	class LoadAllComments extends AsyncTask<String, String, String> {
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
		 * getting All comments from url
		 * */
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("uid", uid));

			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(url_user_comments + uid,
					"GET2", params);

			// Check your log cat for JSON reponse
			Log.d("json: ", json.toString());
			try {
				// Checking for SUCCESS TAG
				error = json.getString(TAG_ERROR);
				if (error.equals("false")) {
					// stories found
					// Getting Array of Stories
					comments = json.getJSONArray("comments");

					// looping through All Stories
					for (int i = 0; i < comments.length(); i++) {
						JSONObject c = comments.getJSONObject(i);

						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						map.put("comment_id", c.getString("id"));
						map.put("user_comment", json.getString("name"));
						map.put("comment_content", c.getString("comment"));
						map.put("date_comment", c.getString("date_comment"));

						// adding HashList to ArrayList
						commentList.add(map);
					}
				} else {
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			adapter = new AllCommentAdapter(ManageUserCommentsDialog.this,
					commentList);
			lv_comments.setAdapter(adapter);
		}

	}

	class DeleteComment extends AsyncTask<String, String, String> {
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
		 * getting All comments from url
		 * */
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("uid", uid));

			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(url_user_comments+uid,
					"DELETE", params);

			// Check your log cat for JSON reponse
			//Log.d("json: ", json.toString());
			try {
				// Checking for SUCCESS TAG
				error = json.getString(TAG_ERROR);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
			if(error.equals("false")){
				ManageUserCommentsDialog.this.dismiss();
				Toast.makeText(getActivity().getApplicationContext(), "Task deleted succesfully", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getActivity().getApplicationContext(), "failed to delete. Please try again!", Toast.LENGTH_SHORT).show();
			}
		}

	}
}
