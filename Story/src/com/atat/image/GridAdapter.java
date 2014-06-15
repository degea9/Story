package com.atat.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import com.atat.story.AllCategoriesActivity;
import com.atat.story.AllStoriesActivity;
import com.atat.story.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;

	public GridAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
		activity = a;
		data = d;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(activity.getApplicationContext());
		//imageLoader.clearCache();
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.row_grid, null);
		TextView cat_id = (TextView) vi.findViewById(R.id.cat_id); // cat id
		TextView name = (TextView) vi.findViewById(R.id.cat_name); // name

		ImageView thumb_image = (ImageView) vi.findViewById(R.id.cat_image); // thumb
																				// image

		HashMap<String, String> song = new HashMap<String, String>();
		song = data.get(position);

		// Setting all values in listview
		cat_id.setText(song.get(AllCategoriesActivity.KEY_ID));
		name.setText(song.get(AllCategoriesActivity.KEY_TITLE));
		imageLoader.DisplayImage(song.get(AllCategoriesActivity.KEY_THUMB_URL),
				thumb_image);
		return vi;
	}
}