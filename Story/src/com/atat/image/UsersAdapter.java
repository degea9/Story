package com.atat.image;

import java.util.ArrayList;
import java.util.HashMap;

import com.atat.story.ManageUserActivity;
import com.atat.story.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.TextView;

public class UsersAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;

	public UsersAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
		activity = a;
		data = d;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
			vi = inflater.inflate(R.layout.list_row_user, null);
		TextView uid = (TextView) vi.findViewById(R.id.user_id); // u id

		TextView name = (TextView) vi.findViewById(R.id.user_name); // name
		TextView type = (TextView) vi.findViewById(R.id.user_type); // type

		HashMap<String, String> user = new HashMap<String, String>();
		user = data.get(position);

		// Setting all values in listview
		uid.setText(user.get(ManageUserActivity.KEY_ID));

		name.setText(user.get(ManageUserActivity.KEY_NAME));
		if (user.get(ManageUserActivity.KEY_TYPE).equals("0"))
			type.setText("ACTIVE");
		else type.setText("BLOCKED");

		return vi;
	}
}