package com.atat.story;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Random;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.atat.json.JSONParser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import android.widget.SeekBar;

import android.widget.TextView;
import android.widget.Toast;

public class AndroidBuildingMusicPlayerActivity extends Activity implements
		OnCompletionListener, SeekBar.OnSeekBarChangeListener,
		OnPreparedListener, OnBufferingUpdateListener {

	private ImageButton btnPlay;
	private ImageButton btnForward;
	private ImageButton btnBackward;
	private ImageButton btnNext;
	private ImageButton btnPrevious;
	private ImageButton btnPlaylist;
	private ImageButton btnRepeat;
	private ImageButton btnShuffle;
	private SeekBar songProgressBar;
	private TextView songTitleLabel;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;
	// Media Player
	private MediaPlayer mp;
	// Handler to update UI timer, progress bar etc,.
	private Handler mHandler = new Handler();;

	private Utilities utils;
	private int seekForwardTime = 5000; // 5000 milliseconds
	private int seekBackwardTime = 5000; // 5000 milliseconds
	private int currentSongIndex = 0;
	private boolean isShuffle = false;
	private boolean isRepeat = false;
	private static ArrayList<HashMap<String, String>> songsList ;
	// add new
	private ProgressDialog pDialog;
	// url to get all chapters list
	private  String url_all_chapters = "http://192.168.22.1/story/slim/v1/chapters/story";
	//private static String url_all_chapters = "http://tuandang.esy.es/story/android_connect/get_all_chapters.php";
	// Creating JSON Parser object
	JSONParser jParser ;
	// JSON Node names
	private static final String TAG_ERROR = "error";
	private static final String TAG_CHAPTERS = "chapters";
	
	// chapters JSONArray
	JSONArray products = null;
	private String url_audio_data = "http://192.168.22.1/story/data/story/audio/";
	//private String url_audio_data = "http://tuandang.esy.es/story/data/story/audio/";
	private String story_id;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		 jParser = new JSONParser(getApplicationContext());
		//Log.d("oncreate", "duoc goi");
		// All player buttons
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		btnForward = (ImageButton) findViewById(R.id.btnForward);
		btnBackward = (ImageButton) findViewById(R.id.btnBackward);
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
		btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
		btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
		btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
		songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
		songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);

		// Mediaplayer
		mp = new MediaPlayer();
		story_id = getIntent().getStringExtra(AllStoriesActivity.KEY_ID);
		utils = new Utilities();

		// Listeners
		songProgressBar.setOnSeekBarChangeListener(this); // Important
		mp.setOnCompletionListener(this); // Important
		mp.setOnBufferingUpdateListener(this);
		// Getting all songs list
		new LoadAllChapters().execute();

		// By default play first song

		/**
		 * Play button click event plays a song and changes button to pause
		 * image pauses a song and changes button to play image
		 * */
		btnPlay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// check for already playing
				if (mp.isPlaying()) {
					if (mp != null) {
						mp.pause();
						// Changing button image to play button
						btnPlay.setImageResource(R.drawable.btn_play);
					}
				} else {
					// Resume song
					if (mp != null) {
						mp.start();
						// Changing button image to pause button
						btnPlay.setImageResource(R.drawable.btn_pause);
					}
				}

			}
		});

		/**
		 * Forward button click event Forwards song specified seconds
		 * */
		btnForward.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// get current song position
				int currentPosition = mp.getCurrentPosition();
				// check if seekForward time is lesser than song duration
				if (currentPosition + seekForwardTime <= mp.getDuration()) {
					// forward song
					mp.seekTo(currentPosition + seekForwardTime);
				} else {
					// forward to end position
					mp.seekTo(mp.getDuration());
				}
			}
		});

		/**
		 * Backward button click event Backward song to specified seconds
		 * */
		btnBackward.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// get current song position
				int currentPosition = mp.getCurrentPosition();
				// check if seekBackward time is greater than 0 sec
				if (currentPosition - seekBackwardTime >= 0) {
					// forward song
					mp.seekTo(currentPosition - seekBackwardTime);
				} else {
					// backward to starting position
					mp.seekTo(0);
				}

			}
		});

		/**
		 * Next button click event Plays next song by taking currentSongIndex +
		 * 1
		 * */
		btnNext.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				songCurrentDurationLabel.setText(""
						+ utils.milliSecondsToTimer(0));
				// check if next song is there or not
				mHandler.removeCallbacks(mUpdateTimeTask);

				if (currentSongIndex < (songsList.size() - 1)) {

					playSong(currentSongIndex + 1);
					currentSongIndex = currentSongIndex + 1;
				} else {
					// play first song
					playSong(0);
					currentSongIndex = 0;
				}

			}
		});

		/**
		 * Back button click event Plays previous song by currentSongIndex - 1
		 * */
		btnPrevious.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				songCurrentDurationLabel.setText(""
						+ utils.milliSecondsToTimer(0));
				mHandler.removeCallbacks(mUpdateTimeTask);

				if (currentSongIndex > 0) {
					playSong(currentSongIndex - 1);
					currentSongIndex = currentSongIndex - 1;
				} else {
					// play last song
					playSong(songsList.size() - 1);
					currentSongIndex = songsList.size() - 1;
				}

			}
		});

		/**
		 * Button Click event for Repeat button Enables repeat flag to true
		 * */
		btnRepeat.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isRepeat) {
					isRepeat = false;
					Toast.makeText(getApplicationContext(), "Repeat is OFF",
							Toast.LENGTH_SHORT).show();
					btnRepeat.setImageResource(R.drawable.btn_repeat);
				} else {
					// make repeat to true
					isRepeat = true;
					Toast.makeText(getApplicationContext(), "Repeat is ON",
							Toast.LENGTH_SHORT).show();
					// make shuffle to false
					isShuffle = false;
					btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
					btnShuffle.setImageResource(R.drawable.btn_shuffle);
				}
			}
		});

		/**
		 * Button Click event for Shuffle button Enables shuffle flag to true
		 * */
		btnShuffle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isShuffle) {
					isShuffle = false;
					Toast.makeText(getApplicationContext(), "Shuffle is OFF",
							Toast.LENGTH_SHORT).show();
					btnShuffle.setImageResource(R.drawable.btn_shuffle);
				} else {
					// make repeat to true
					isShuffle = true;
					Toast.makeText(getApplicationContext(), "Shuffle is ON",
							Toast.LENGTH_SHORT).show();
					// make shuffle to false
					isRepeat = false;
					btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
					btnRepeat.setImageResource(R.drawable.btn_repeat);
				}
			}
		});

		/**
		 * Button Click event for Play list click event Launches list activity
		 * which displays list of songs
		 * */
		btnPlaylist.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(),
						PlayListActivity.class);
				startActivityForResult(i, 100);
			}
		});

	}

	/**
	 * Receiving song index from playlist view and play the song
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 100) {
			currentSongIndex = data.getExtras().getInt("songIndex");
			// play selected song
			playSong(currentSongIndex);
		}

	}

	/**
	 * Function to play a song
	 * 
	 * @param songIndex
	 *            - index of song
	 * */
	public void playSong(int songIndex) {
		// Play song
		try {
			pDialog.show();
			mHandler.removeCallbacks(mUpdateTimeTask);
			mp.reset();
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mp.setDataSource(songsList.get(songIndex).get("songPath"));
			Log.d("path ne", songsList.get(songIndex).get("songPath"));
			mp.prepareAsync();
			mp.setOnPreparedListener(this);
			// Displaying Song title
			String songTitle = songsList.get(songIndex).get("songTitle");
			songTitleLabel.setText(songTitle);

			// Changing Button Image to pause image
			btnPlay.setImageResource(R.drawable.btn_pause);

			// set Progress bar values
			songProgressBar.setProgress(0);
			songProgressBar.setMax(100);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update timer on seekbar
	 * */
	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	/**
	 * Background Runnable thread
	 * */
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			long totalDuration = mp.getDuration();
			long currentDuration = mp.getCurrentPosition();

			// Displaying Total Duration time
			songTotalDurationLabel.setText(""
					+ utils.milliSecondsToTimer(totalDuration));
			// Displaying time completed playing
			songCurrentDurationLabel.setText(""
					+ utils.milliSecondsToTimer(currentDuration));

			// Updating progress bar
			int progress = (int) (utils.getProgressPercentage(currentDuration,
					totalDuration));
			// Log.d("Progress", ""+progress);
			songProgressBar.setProgress(progress);

			// Running this thread after 100 milliseconds
			mHandler.postDelayed(this, 100);
		}
	};

	/**
	 * 
	 * */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromTouch) {

	}

	/**
	 * When user starts moving the progress handler
	 * */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// remove message Handler from updating progress bar
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	/**
	 * When user stops moving the progress hanlder
	 * */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);

		int totalDuration = mp.getDuration();
		int currentPosition = utils.progressToTimer(seekBar.getProgress(),
				totalDuration);

		// forward or backward to certain seconds
		mp.seekTo(currentPosition);

		// update timer progress again
		updateProgressBar();
	}

	/**
	 * On Song Playing completed if repeat is ON play same song again if shuffle
	 * is ON play random song
	 * */
	@Override
	public void onCompletion(MediaPlayer arg0) {

		// check for repeat is ON or OFF
		if (isRepeat) {
			// repeat is on play same song again
			playSong(currentSongIndex);
		} else if (isShuffle) {
			// shuffle is on - play a random song
			Random rand = new Random();
			currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
			playSong(currentSongIndex);
		} else {
			// no repeat or shuffle ON - play next song
			if (currentSongIndex < (songsList.size() - 1)) {
				playSong(currentSongIndex + 1);
				currentSongIndex = currentSongIndex + 1;
			} else {
				// play first song
				playSong(0);
				currentSongIndex = 0;
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("ondestroy", "duoc goi");
		mp.release();
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler = null;
	}

	class LoadAllChapters extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			songsList= new ArrayList<HashMap<String, String>>();
			pDialog = new ProgressDialog(
					AndroidBuildingMusicPlayerActivity.this);
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
			url_all_chapters+="/"+story_id;
			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(url_all_chapters, "GET",
					null);

			// Check your log cat for JSON reponse
			// Log.d("All Chapters: ", json.toString());

			try {
				// Checking for SUCCESS TAG
				boolean error = json.getBoolean(TAG_ERROR);

				if (error==false) {
					// products found
					// Getting Array of Products
					products = json.getJSONArray(TAG_CHAPTERS);

					// looping through All Products
					for (int i = 0; i < products.length(); i++) {
						JSONObject c = products.getJSONObject(i);

						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						map.put("songTitle", c.getString("name"));
						map.put("songPath", url_audio_data+story_id+"/"+c.getString("path"));

						// adding HashList to ArrayList
						songsList.add(map);
					}
				} else {
					// no products found
					// Launch Add New product Activity
					// Intent i = new Intent(getApplicationContext(),
					// NewProductActivity.class);
					// Closing all previous activities
					// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					// startActivity(i);
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
					Log.d("tuan", songsList.get(0).get("songPath"));
					playSong(0);
				}
			});

		}

	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
		pDialog.dismiss();
		// Updating progress bar
		updateProgressBar();

	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int percent) {
		songProgressBar.setSecondaryProgress(percent);

	}
	
	public ArrayList<HashMap<String, String>> getPlayList(){
		return songsList;
	}

}