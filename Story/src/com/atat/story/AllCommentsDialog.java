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

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint("ValidFragment")
public class AllCommentsDialog extends DialogFragment {
	private View inflator;
	//private User user;
	// Progress Dialog
	private ProgressDialog pDialog;
	// url to get all chapters list
	// private static String url_login_user =
	// "http://192.168.22.1/story/android_connect/login.php";
//private static String url_login_user = "http://tuandang.esy.es/story/android_connect/login.php";
	private  String url_chapter_comments = "http://192.168.22.1/story/slim/v1/comment/chapter";
	private static String url_post_comments = "http://192.168.22.1/story/slim/v1/comment";
	// Creating JSON Parser object
	JSONParser jParser ;
	// JSON Node names
	private static final String TAG_ERROR = "error";
	private boolean error;
	private String message="";
	private String comment_content;
	private ArrayList<HashMap<String, String>> commentList = new ArrayList<HashMap<String, String>>();
	AllCommentAdapter adapter;
	ListView lv_comments;
	JSONArray comments = null;
	String chap_id;

	public AllCommentsDialog(String chap_id) {
		this.chap_id=chap_id;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		jParser = new JSONParser(getActivity().getApplicationContext());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		inflator = inflater.inflate(R.layout.dialog_comments, null);

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout

		builder.setView(inflator);
		return builder.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		
		final EditText edt_Username = (EditText) inflator
				.findViewById(R.id.comment);

		final Button bt_comment = (Button) inflator
				.findViewById(R.id.bt_comment);
		bt_comment.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(edt_Username.getText().length()>10 && edt_Username.getText().length()<200){
					comment_content = edt_Username.getText().toString();
					new PostComment().execute();
					
				}else {
					Toast.makeText(getActivity().getApplicationContext(), "Comment phai toi thieu 10 ki tu va toi da 200 ki tu", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		UserFunctions userFunctions = new UserFunctions();
		if(userFunctions.isUerLogout(getActivity().getApplicationContext())) bt_comment.setEnabled(false);
		
		lv_comments = (ListView)inflator.findViewById(R.id.list_comment);
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
			url_chapter_comments="http://192.168.22.1/story/slim/v1/comment/chapter";
			url_chapter_comments+="/"+chap_id;
			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(url_chapter_comments,
					"GET", null);

			// Check your log cat for JSON reponse
			//Log.d("json: ", json.toString());
			try {
				// Checking for SUCCESS TAG
				error = json.getBoolean(TAG_ERROR);
				if (error == false) {
					// stories found
					// Getting Array of Stories
					comments = json.getJSONArray("comments");
					commentList.clear();
					// looping through All Stories
					for (int i = 0; i < comments.length(); i++) {
						JSONObject c = comments.getJSONObject(i);

						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						map.put("comment_id", c.getString("comment_id"));
						map.put("user_comment", c.getString("name"));
						map.put("comment_content",
								c.getString("comment_content"));
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
			adapter = new AllCommentAdapter(AllCommentsDialog.this, commentList);
			lv_comments.setAdapter(adapter);
		}

	}
	
	
	class PostComment extends AsyncTask<String, String, String> {
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
			params.add(new BasicNameValuePair("cid", chap_id));		
			
			params.add(new BasicNameValuePair("comment", comment_content));
			
			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(url_post_comments,
					"POST", params);

			// Check your log cat for JSON reponse
			//Log.d("json: ", json.toString());
			try {
				// Checking for SUCCESS TAG
				error = json.getBoolean(TAG_ERROR);
				message = json.getString("message");
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
			if (error==false) {
				new LoadAllComments().execute();
			} else {
				Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			}

		}

	}
}
