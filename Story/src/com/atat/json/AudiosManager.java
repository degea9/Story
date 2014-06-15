package com.atat.json;

import java.util.ArrayList;
import java.util.HashMap;

public class AudiosManager {
	private ArrayList<HashMap<String, String>> audiosList = new ArrayList<HashMap<String, String>>();

	public AudiosManager(){
		
	}
	
	/**
     * Function to read all mp3 files of story
     * and store the details in ArrayList
     * */
    public ArrayList<HashMap<String, String>> getPlayList(){      
 
       
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("audio chapter Title", "audio chapter tittle here");
                song.put("audio chapter Path", "path audio here");
 
                // Adding each song to SongList
                audiosList.add(song);
       
        // return songs list array
        return audiosList;
    }
}
