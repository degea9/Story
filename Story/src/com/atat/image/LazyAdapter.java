package com.atat.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

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

public class LazyAdapter extends BaseAdapter {
    
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader; 
    
    public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
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
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_row, null);
        TextView sid = (TextView)vi.findViewById(R.id.sid); // story id 
        TextView is_audio = (TextView)vi.findViewById(R.id.is_audio); // is audio
        TextView name = (TextView)vi.findViewById(R.id.title); // name
        TextView artist = (TextView)vi.findViewById(R.id.author); // author name
        TextView author = (TextView)vi.findViewById(R.id.category); // category
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image); // thumb image
        
        HashMap<String, String> song = new HashMap<String, String>();
        song = data.get(position);
        
        // Setting all values in listview
        sid.setText(song.get(AllStoriesActivity.KEY_ID));
        is_audio.setText(song.get(AllStoriesActivity.KEY_IS_AUDIO));
        name.setText(song.get(AllStoriesActivity.KEY_TITLE));
        artist.setText(song.get(AllStoriesActivity.KEY_AUTHOR));
        author.setText(song.get(AllStoriesActivity.KEY_CATEGORY));
        imageLoader.DisplayImage(song.get(AllStoriesActivity.KEY_THUMB_URL), thumb_image);
        return vi;
    }
}