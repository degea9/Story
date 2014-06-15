package com.atat.story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import android.widget.SearchView;

import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.ListView;
import com.atat.json.JSONParser;
import com.atat.models.UserFunctions;

import com.atat.image.LazyAdapter;

import com.atat.story.R;


@SuppressLint("NewApi")
public class AllStoriesActivity extends Activity implements OnQueryTextListener {
	// nav menu
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter navAdapter;

	private ProgressDialog pDialog;
	// All static variables

	// url to get all chapters list
	private static String url_thumb_image_story = "http://192.168.22.1/story/data/images/story/72x72/";
	private  String url_all_stories = "http://192.168.22.1/story/slim/v1/stories";

	
	public static final String KEY_ID = "sid";
	public static final String KEY_IS_AUDIO = "is_audio";
	public static final String KEY_TITLE = "name";
	public static final String KEY_AUTHOR = "author";
	public static final String KEY_CATEGORY = "category";
	public static final String KEY_THUMB_URL = "image";
	// JSON Node names
	private static final String TAG_ERROR = "error";
	private static final String TAG_STORIES = "stories";
	private String cat_id;

	private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	// chapters JSONArray
	JSONArray products = null;

	ListView list;
	LazyAdapter adapter;
	LazyAdapter newAdapter;
	// Creating JSON Parser object
	JSONParser jParser ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_all_stories);
		jParser =  new JSONParser(getApplicationContext());
		//getApplicationContext().deleteDatabase("story");
		cat_id = getIntent().getStringExtra("cat_id");
		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu_story);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// Home
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons
				.getResourceId(0, -1)));
		// Top Stories
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons
				.getResourceId(1, -1)));

		// Category
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons
				.getResourceId(2, -1)));
		// Logout
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons
				.getResourceId(3, -1)));
		UserFunctions userFunction = new UserFunctions();
		if(userFunction.isAdmin(getApplicationContext()))
		//Manage Users	
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons
						.getResourceId(4, -1)));

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		navAdapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(navAdapter);

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for
									// accessibility
				R.string.app_name // nav drawer close - description for
									// accessibility
		) {
			public void onDrawerClosed(View view) {
				 getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		if (savedInstanceState == null) {
			// on first time display view for first nav item
			displayView(0);
		}

		new LoadAllStories().execute();

		list = (ListView) findViewById(R.id.list);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// getting values from selected ListItem
				String sid = ((TextView) view.findViewById(R.id.sid)).getText()
						.toString();
				String is_audio = ((TextView) view.findViewById(R.id.is_audio))
						.getText().toString();
				Log.d("sid",sid);
				Log.d("is_audio",is_audio);
				Intent in;
				// Starting new intent
				if (Integer.parseInt(is_audio) == 0) {
					in = new Intent(getApplicationContext(),
							AllChaptersActivity.class);
				} else {
					in = new Intent(getApplicationContext(),
							AndroidBuildingMusicPlayerActivity.class);
				}
				// sending pid to next activity
				in.putExtra(KEY_ID, sid);

				// starting new activity and expecting some response back
				startActivityForResult(in, 100);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.all_stories, menu);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
				.getActionView();
		searchView.setOnQueryTextListener(this);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
        case R.id.action_settings:
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	 @Override
	    protected void onPostCreate(Bundle savedInstanceState) {
	        super.onPostCreate(savedInstanceState);
	        // Sync the toggle state after onRestoreInstanceState has occurred.
	        mDrawerToggle.syncState();
	    }
	 
	    @Override
	    public void onConfigurationChanged(Configuration newConfig) {
	        super.onConfigurationChanged(newConfig);
	        // Pass any configuration change to the drawer toggls
	        mDrawerToggle.onConfigurationChanged(newConfig);
	    }
	class LoadAllStories extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AllStoriesActivity.this);
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
			
			if (cat_id != null)
				url_all_stories+="/category/"+cat_id;
			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(url_all_stories, "GET",null);

		

			try {
				// Checking for SUCCESS TAG
				String error = json.getString(TAG_ERROR);

				if (error.equals("false")) {
					// stories found
					// Getting Array of Stories
					products = json.getJSONArray(TAG_STORIES);

					// looping through All Stories
					for (int i = 0; i < products.length(); i++) {
						JSONObject c = products.getJSONObject(i);
						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						map.put(KEY_ID, c.getString(KEY_ID));
						map.put(KEY_IS_AUDIO, c.getString(KEY_IS_AUDIO));
						map.put(KEY_TITLE, c.getString(KEY_TITLE));
						map.put(KEY_AUTHOR, c.getString(KEY_AUTHOR));
						map.put(KEY_CATEGORY, c.getString(KEY_CATEGORY));
						map.put(KEY_THUMB_URL,
								url_thumb_image_story
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

					// Getting adapter by passing xml data ArrayList
					adapter = new LazyAdapter(AllStoriesActivity.this,
							songsList);
					list.setAdapter(adapter);

				}
			});

		}

	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// Log.d("dang search ", newText);

		ArrayList<HashMap<String, String>> newStoriesList = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < songsList.size(); i++)
			if (isInMap(songsList.get(i), newText))
				newStoriesList.add(songsList.get(i));
		newAdapter = new LazyAdapter(AllStoriesActivity.this, newStoriesList);
		list.setAdapter(newAdapter);
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String newText) {

		return false;
	}

	public boolean isInMap(HashMap<String, String> baseMap, String prefix) {

		if (baseMap.get(KEY_TITLE).toLowerCase()
				.startsWith(prefix.toLowerCase()))
			return true;

		return false;
	}

	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}
	
	private void displayView(int position) {
		Intent intent;
		switch (position) {
		case 0:
			
			break;
		case 1:
			intent = new Intent(AllStoriesActivity.this,
					AllStoriesActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		case 2:
			intent = new Intent(AllStoriesActivity.this,
					AllCategoriesActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		case 3:
			UserFunctions userFunction = new UserFunctions();
			userFunction.logoutUser(getApplicationContext());
			intent = new Intent(AllStoriesActivity.this,
					AllStoriesActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		case 4:
			intent = new Intent(AllStoriesActivity.this,
					ManageUserActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		
		default:
			break;
		}

		mDrawerList.setItemChecked(position, true);
		mDrawerList.setSelection(position);		
		mDrawerLayout.closeDrawer(mDrawerList);
    }
}