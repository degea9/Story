package com.atat.story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.atat.image.GridAdapter;
import com.atat.image.LazyAdapter;
import com.atat.json.JSONParser;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

public class AllCategoriesActivity extends Activity implements OnItemClickListener {
	private ProgressDialog pDialog;
	public static final String KEY_ID = "cat_id";
	public static final String KEY_TITLE = "name";
	public static final String KEY_THUMB_URL = "image";
	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_CATEGORIES = "categories";
	private static String url_all_categories = "http://192.168.22.1/story/android_connect/get_all_category.php";
	private static String url_thumb_image_category = "http://192.168.22.1/story/data/images/category/72x72/";
	//private static String url_all_categories = "http://tuandang.esy.es/story/android_connect/get_all_category.php";
	//private static String url_thumb_image_category = "http://tuandang.esy.es/story/data/images/category/72x72/";
	private static ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	// Creating JSON Parser object
	JSONParser jParser ;
	private static boolean isExecute = false;
	// chapters JSONArray
	JSONArray products = null;
	private GridView grid_category;
	private GridAdapter grid_adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_all_categories);
		jParser = new JSONParser(getApplicationContext());
		grid_category = (GridView) findViewById(R.id.gridView1);
		grid_category.setOnItemClickListener(this);
		if(isExecute==false) new LoadAllStories().execute();
		else{
			grid_adapter = new GridAdapter(AllCategoriesActivity.this, songsList);
			grid_category.setAdapter(grid_adapter);
			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.all_categories, menu);
		return true;
	}

	class LoadAllStories extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AllCategoriesActivity.this);
			pDialog.setMessage("Loading stories. Please wait...");
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
			JSONObject json = jParser.makeHttpRequest(url_all_categories,
					"GET", params);

			// Check your log cat for JSON reponse
			 //Log.d("All categories: ", json.toString());

			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// stories found
					// Getting Array of Stories
					products = json.getJSONArray(TAG_CATEGORIES);

					// looping through All Stories
					for (int i = 0; i < products.length(); i++) {
						JSONObject c = products.getJSONObject(i);

						// Storing each json item in variable
						// String id = c.getString(TAG_CID);
						// String name = c.getString(TAG_NAME);

						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						map.put(KEY_ID, c.getString(KEY_ID));
						map.put(KEY_TITLE, c.getString(KEY_TITLE));
						map.put(KEY_THUMB_URL,
								url_thumb_image_category
										+ c.getString(KEY_THUMB_URL));

						// adding HashList to ArrayList
						songsList.add(map);
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
					 * Updating parsed JSON data into ListView *
					 */
					isExecute=true;
					// Getting adapter by passing xml data ArrayList
					grid_adapter = new GridAdapter(AllCategoriesActivity.this, songsList);
					grid_category.setAdapter(grid_adapter);
				}
			});

		}

	}

	@Override
	public void onItemClick(final AdapterView<?> arg0, final View view, final int position, final long id) {
		String cat_id = ((TextView)view.findViewById(R.id.cat_id)).getText().toString();
		Intent intent = new Intent(AllCategoriesActivity.this,AllStoriesActivity.class);
		intent.putExtra("cat_id", cat_id);
		startActivity(intent);
		
	}

}
