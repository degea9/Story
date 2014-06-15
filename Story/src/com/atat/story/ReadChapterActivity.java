package com.atat.story;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

import android.app.Dialog;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.atat.json.JSONParser;

import com.atat.models.UserFunctions;

@SuppressLint("NewApi")
public class ReadChapterActivity extends FragmentActivity implements
		OnDismissListener {
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
	private NavDrawerListAdapter adapter;

	// Progress Dialog
	private ProgressDialog pDialog;
	private TextView tvChapContent;
	private String chap_content;
	private String chap_id;
	private MenuItem menu_item_vote;
	// Creating JSON Parser object
	JSONParser jParser;

	// url to get all chapters list
	private String url_read_chapters = "http://192.168.22.1/story/slim/v1/chapter";
	// private static String url_read_chapters =
	// "http://tuandang.esy.es/story/android_connect/read_chapter.php";
	// url vote story
	private static String url_vote_story = "http://192.168.22.1/story/slim/v1/vote";
	private String url_unvote_story = "http://192.168.22.1/story/slim/v1/vote/story";
	// private static String url_vote_story =
	// "http://tuandang.esy.es/story/android_connect/vote_story.php";
	// private static String url_unvote_story =
	// "http://tuandang.esy.es/story/android_connect/unvote_story.php";
	// JSON Node names
	private static final String TAG_ERROR = "error";
	private static final String TAG_CONTENT = "content";
	// chapters JSONArray
	JSONArray products = null;
	private boolean error;
	private String message = "";

	private SeekBar font_size;
	private Dialog setting_dialog;
	private String story_id;
	private String username;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_chapter);
		jParser = new JSONParser(getApplicationContext());
		Intent intent = getIntent();
		chap_id = intent.getStringExtra("cid");
		story_id = intent.getStringExtra("story_id");
		tvChapContent = (TextView) findViewById(R.id.tvChapContent);
		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

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

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

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
				// getActionBar().setTitle(mTitle);
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
		new ReadChapter().execute();
		// Log.d("con create", "duoc tao");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.read_chapter, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.action_comment:
			show_dialog("comment");
			return true;
		case R.id.action_vote:
			menu_item_vote = item;
			UserFunctions userFunction = new UserFunctions();
			if (!userFunction.isUserLoggedIn(getApplicationContext()))
				show_dialog("vote");
			else {
				username = userFunction.getUserName(getApplicationContext());
				if (!AllChaptersActivity.isUserVotedStory())
					new VoteStory().execute("vote");
				else
					new VoteStory().execute("unvote");
			}
			return true;
		case R.id.action_settings:
			setting_dialog = new Dialog(ReadChapterActivity.this);
			setting_dialog.setContentView(R.layout.dialog_settings);
			setting_dialog.setTitle("Settings");
			font_size = (SeekBar) setting_dialog
					.findViewById(R.id.sb_settting_font);
			font_size.setProgress(0);
			font_size.setMax(15);
			setting_dialog.setOnDismissListener(this);
			setting_dialog.show();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	protected void onPause() {

		super.onPause();
		if (setting_dialog != null) {
			setting_dialog.dismiss();
		}
	}

	class ReadChapter extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ReadChapterActivity.this);
			pDialog.setMessage("Loading chapter. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting All chapters from url
		 * */
		protected String doInBackground(String... args) {
			// Building Parameters
			url_read_chapters += "/" + chap_id;
			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(url_read_chapters, "GET",
					null);
			// Check your log cat for JSON reponse
			try {
				// Checking for SUCCESS TAG
				boolean error = json.getBoolean(TAG_ERROR);

				if (error == false) {
					// chapters found

					// Storing each json item in variable
					chap_content = json.getString(TAG_CONTENT);
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
			tvChapContent.setText(chap_content);
			tvChapContent.setTextSize(20);
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		tvChapContent.setTextSize(font_size.getProgress() + 15);
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

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem menu_vote = menu.findItem(R.id.action_vote);
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		menu_vote.setVisible(!drawerOpen);
		if (AllChaptersActivity.isUserVotedStory())
			menu_vote.setIcon(R.drawable.ic_action_voted);
		return super.onPrepareOptionsMenu(menu);
	}

	private void displayView(int position) {
		Intent intent;
		switch (position) {
		case 0:
			break;
		case 1:
			intent = new Intent(ReadChapterActivity.this,
					AllStoriesActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		case 2:
			intent = new Intent(ReadChapterActivity.this,
					AllCategoriesActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		case 3:
			UserFunctions userFunction = new UserFunctions();
			userFunction.logoutUser(getApplicationContext());
			intent = new Intent(ReadChapterActivity.this,
					AllStoriesActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		default:
			break;
		}

		mDrawerList.setItemChecked(position, true);
		mDrawerList.setSelection(position);
		// setTitle(navMenuTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
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

	public void show_dialog(String c) {
		FragmentManager fm = getSupportFragmentManager();
		if (c.equals("vote")) {
			LoginDialog loginDialog = new LoginDialog();
			loginDialog.show(fm, "login_dialog");
		} else if (c.equals("comment")) {
			AllCommentsDialog commentDialog = new AllCommentsDialog(chap_id);
			commentDialog.show(fm, "comment_dialog");
		}
	}

	class VoteStory extends AsyncTask<String, String, String> {
		/**
		 * vote
		 * */
		protected String doInBackground(String... action) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair("story_id", story_id));
			JSONObject json;
			if (action[0].equals("vote")) {
				// getting JSON string from URL
				json = jParser.makeHttpRequest(url_vote_story, "POST", params);
				Log.d("vote", json.toString());
				try {
					error = json.getBoolean(TAG_ERROR);
					message = json.getString("message");
				} catch (JSONException e) {

					e.printStackTrace();
				}
				return "vote";
			} else if (action[0].equals("unvote")) {
				// getting JSON string from URL
				url_unvote_story = "http://192.168.22.1/story/slim/v1/vote/story";
				url_unvote_story += "/" + story_id;
				json = jParser
						.makeHttpRequest(url_unvote_story, "DELETE", null);
				Log.d("unvote", json.toString());
				try {
					error = json.getBoolean(TAG_ERROR);
					message = json.getString("message");
				} catch (JSONException e) {

					e.printStackTrace();
				}
				return "unvote";
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String action) {
			if (action.equals("vote")) {
				if (error == false) {
					AllChaptersActivity.setUserVoteStory(true);
					menu_item_vote.setIcon(R.drawable.ic_action_voted);
					Toast.makeText(ReadChapterActivity.this,
							"Thank you for your vote", Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(ReadChapterActivity.this, message,
							Toast.LENGTH_SHORT).show();
				}
			} else if (action.equals("unvote")) {
				if (error == false) {
					AllChaptersActivity.setUserVoteStory(false);

					menu_item_vote.setIcon(R.drawable.ic_action_vote);
					Toast.makeText(ReadChapterActivity.this,
							"Your vote have been removed", Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(ReadChapterActivity.this, message,
							Toast.LENGTH_SHORT).show();
				}
			}

		}
	}

}
