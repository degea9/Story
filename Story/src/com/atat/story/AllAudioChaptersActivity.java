package com.atat.story;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;


import com.atat.json.JSONParser;

public class AllAudioChaptersActivity extends ListActivity {

	// Progress Dialog
	private ProgressDialog pDialog;

	// Creating JSON Parser object
	JSONParser jParser ;

	ArrayList<HashMap<String, String>> chaptersList;

	// url to get all chapters list
	private static String url_all_chapters = "http://192.168.22.1/story/android_connect/get_all_audio_chapters.php";
	//private static String url_all_chapters = "http://tuandang.esy.es/story/android_connect/get_all_audio_chapters.php";

	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_CHAPTERS = "chapters";
	private static final String TAG_CID = "cid";
	private static final String TAG_NAME = "name";
	private static final String TAG_PATH = "path";
	private static final MediaPlayer mp = new MediaPlayer();

	// chapters JSONArray
	JSONArray products = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.all_chapters);
		jParser = new JSONParser(getApplicationContext());
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
			public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
				// getting values from selected ListItem
				//String cid = ((TextView) view.findViewById(R.id.cid)).getText()
						//.toString();

				// Starting new intent
				//Intent in = new Intent(getApplicationContext(),
						//ReadChapterActivity.class);
				// sending pid to next activity
				//in.putExtra(TAG_CID, cid);
				String path = "http://192.168.22.1/story/data/story/text/68/"+chaptersList.get(position).get(TAG_PATH);
				mp.reset();
				try {
					mp.setDataSource(path);
					mp.prepare();
					mp.start();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				// starting new activity and expecting some response back
				//startActivityForResult(in, 100);
				
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
			pDialog = new ProgressDialog(AllAudioChaptersActivity.this);
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
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(url_all_chapters, "GET", params);
			
			// Check your log cat for JSON reponse
			//Log.d("All Chapters: ", json.toString());

			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// products found
					// Getting Array of Products
					products = json.getJSONArray(TAG_CHAPTERS);

					// looping through All Products
					for (int i = 0; i < products.length(); i++) {
						JSONObject c = products.getJSONObject(i);

						// Storing each json item in variable
						String id = c.getString(TAG_CID);
						String name = c.getString(TAG_NAME);
						String path = c.getString(TAG_PATH);

						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						map.put(TAG_CID, id);
						map.put(TAG_NAME, name);
						map.put(TAG_PATH, path);

						// adding HashList to ArrayList
						chaptersList.add(map);
					}
				} else {
					// no products found
					// Launch Add New product Activity
					//Intent i = new Intent(getApplicationContext(),
							//NewProductActivity.class);
					// Closing all previous activities
					//i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					//startActivity(i);
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
							AllAudioChaptersActivity.this, chaptersList,
							R.layout.list_item_chapter, new String[] { TAG_CID,
									TAG_NAME},
							new int[] { R.id.cid, R.id.name });
					// updating listview
					setListAdapter(adapter);
				}
			});

		}

	}
}