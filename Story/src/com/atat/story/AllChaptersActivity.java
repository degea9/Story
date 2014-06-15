package com.atat.story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView;
import android.widget.TextView;


import com.atat.json.JSONParser;
import com.atat.models.UserFunctions;

public class AllChaptersActivity extends ListActivity {
	private String story_id;
	private static boolean isStoryVoted=false;
	// Progress Dialog
	private ProgressDialog pDialog;

	// Creating JSON Parser object
	JSONParser jParser;

	ArrayList<HashMap<String, String>> chaptersList;

	// url to get all chapters list
	private  String url_all_chapters = "http://192.168.22.1/story/slim/v1/chapters/story";
	//private static String url_all_chapters = "http://tuandang.esy.es/story/android_connect/get_all_chapters.php";

	// JSON Node names
	private static final String TAG_ERROR = "error";
	private static final String TAG_CHAPTERS = "chapters";
	private static final String TAG_CID = "cid";
	private static final String TAG_NAME = "name";

	// chapters JSONArray
	JSONArray products = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.all_chapters);
		jParser = new JSONParser(getApplicationContext());
		story_id=getIntent().getStringExtra(AllStoriesActivity.KEY_ID);
		// Hashmap for ListView
		chaptersList = new ArrayList<HashMap<String, String>>();

		// Loading chapters in Background Thread
		new LoadAllChapters().execute();

		// Get listview
		ListView lv = getListView();

		// on seleting single chapter
		// launching read chapter Screen
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// getting values from selected ListItem
				String cid = ((TextView) view.findViewById(R.id.cid)).getText()
						.toString();

				// Starting new intent
				Intent in = new Intent(getApplicationContext(),
						ReadChapterActivity.class);
				// sending pid to next activity
				in.putExtra(TAG_CID, cid);
				in.putExtra("story_id", story_id);
				// starting new activity and expecting some response back
				startActivityForResult(in, 100);
			}
		});

	}

	// Response from read chapter Activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// if result code 100
		if (resultCode == 100) {
			// if result code 100 is received 
			// means user edited/deleted product
			// reload this screen again
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		}

	}

	/**
	 * Background Async Task to Load all product by making HTTP Request
	 * */
	class LoadAllChapters extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AllChaptersActivity.this);
			pDialog.setMessage("Loading chapters. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting All chapters from url
		 * */
		protected String doInBackground(String... args) {
			// Building Parameters
			//List<NameValuePair> params = new ArrayList<NameValuePair>();
			//params.add(new BasicNameValuePair(AllStoriesActivity.KEY_ID, story_id));
			UserFunctions userFunction = new UserFunctions();
			//params.add(new BasicNameValuePair("username", userFunction.getUserName(getApplicationContext())));
			// getting JSON string from URL
			url_all_chapters+="/"+story_id;
			JSONObject json = jParser.makeHttpRequest(url_all_chapters, "GET", null);
			
			// Check your log cat for JSON reponse
			//Log.d("All Chapters: ", json.toString());

			try {
				// Checking for SUCCESS TAG
				String error = json.getString(TAG_ERROR);
				if(userFunction.isUerLogout(getApplicationContext())==false)isStoryVoted= json.getBoolean("voted");
				//if(voted_story.equals("true")) isStoryVoted=true;
				//else isStoryVoted=false;
				if (error.equals("false")) {
					// products found
					// Getting Array of Products
					products = json.getJSONArray(TAG_CHAPTERS);

					// looping through All Products
					for (int i = 0; i < products.length(); i++) {
						JSONObject c = products.getJSONObject(i);

						// Storing each json item in variable
						String id = c.getString(TAG_CID);
						String name = c.getString(TAG_NAME);

						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						map.put(TAG_CID, id);
						map.put(TAG_NAME, name);

						// adding HashList to ArrayList
						chaptersList.add(map);
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
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed JSON data into ListView
					 * */
					ListAdapter adapter = new SimpleAdapter(
							AllChaptersActivity.this, chaptersList,
							R.layout.list_item_chapter, new String[] { TAG_CID,
									TAG_NAME},
							new int[] { R.id.cid, R.id.name });
					// updating listview
					setListAdapter(adapter);
				}
			});

		}

	}
	
	
	public static boolean isUserVotedStory(){
		return isStoryVoted;
	}
	
	
	public static void setUserVoteStory(boolean isVote){
		
		isStoryVoted = isVote;
	}
}