package com.atat.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import com.atat.story.AllStoriesActivity;
import com.atat.story.R;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AllCommentAdapter extends BaseAdapter {
    
    private DialogFragment dialog;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    //public ImageLoader imageLoader; 
    
    public AllCommentAdapter(DialogFragment a, ArrayList<HashMap<String, String>> d) {
        dialog = a;
        data=d;
        inflater = a.getActivity().getLayoutInflater();
        
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
            vi = inflater.inflate(R.layout.list_row_comment, null);
        TextView comment_id = (TextView)vi.findViewById(R.id.comment_id); // comment id        
        TextView username = (TextView)vi.findViewById(R.id.user_comment); // username commented
        TextView comment_content = (TextView)vi.findViewById(R.id.comment_content); // comment content
        TextView date_comment = (TextView)vi.findViewById(R.id.date_comment); // date comment
        //ImageView thumb_image=(ImageView)vi.findViewById(R.id.avatar); // avatar 
        
        HashMap<String, String> song = new HashMap<String, String>();
        song = data.get(position);
        
        // Setting all values in listview
        comment_id.setText(song.get("comment_id"));
       
        username.setText(song.get("user_comment"));
        
        comment_content.setText(song.get("comment_content"));
        date_comment.setText(song.get("date_comment"));
        //imageLoader.DisplayImage(song.get(AllStoriesActivity.KEY_THUMB_URL), thumb_image);
        return vi;
    }
}