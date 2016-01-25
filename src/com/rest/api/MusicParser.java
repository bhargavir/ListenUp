package com.rest.api;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
//import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * This is a JSON parser file which reads JSON and renders HashMap as per requirements
 * @author Dinakaran
 *
 */
public class MusicParser {
	
	/**
	 * Map of plays.json file
	 * key is Username and value is list of tracks
	 */
	private Map<String, List<String>> playTracksMap = new HashMap<String, List<String>>();
	
	/**
	 * Map of tracks.json file
	 * Key is tracks and value is a hashmap which contains rating, plays, id and title
	 */
	private Map<String, Map<String, Object>> trackDeatils = new HashMap<String, Map<String, Object>>();
	
	/**
	 * Map of friends.json file
	 * Key is username and value is the list of friends
	 */
	private Map<String, List<String>> friendsMap = new HashMap<String, List<String>>();

	/**
	 * Method to parse friends.json file
	 * @return Hashmap of friends.json
	 */
	public Map<String, List<String>> parseFriendsJSON(){
		
		JSONParser parser = new JSONParser();
		 
        try {
 
            Object obj = parser.parse(new FileReader(
                    "C:\\listenup\\listenup\\data\\friends.json"));
 
            JSONObject jsonObject = (JSONObject) obj;
            Iterator<String> iterator = jsonObject.keySet().iterator();
            while(iterator.hasNext()){
            	String userId = iterator.next();
            	List<String> friendsList = (List<String>)jsonObject.get(userId);
            	friendsMap.put(userId, friendsList);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return friendsMap;
	}
	
	/**
	 * Method to parse tracks.json file
	 * @return map of tracks.json
	 */
	public Map<String, Map<String, Object>> parseTracksJSON(){
		JSONParser parser = new JSONParser();
		 
        try {
 
            Object obj = parser.parse(new FileReader(
                    "C:\\listenup\\listenup\\data\\tracks.json"));
            JSONObject jsonObject = (JSONObject) obj;
            Iterator<String> iterator = jsonObject.keySet().iterator();
            while(iterator.hasNext()){
            	String trackId = iterator.next();
            	Map<String, Object> trackMap = (HashMap<String, Object>)jsonObject.get(trackId);
            	trackDeatils.put(trackId, trackMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trackDeatils;
	}
	
	/**
	 * Method to parse plays.json
	 * @return Map of plays.json
	 */
	public Map<String, List<String>> parsePlaysJSON(){
		JSONParser parser = new JSONParser();
		 
        try {
 
            Object obj = parser.parse(new FileReader(
                    "C:\\listenup\\listenup\\data\\plays.json"));
 
            JSONObject jsonObject = (JSONObject) obj;
            Iterator<String> iterator = jsonObject.keySet().iterator();
            while(iterator.hasNext()){
            	String userId = iterator.next();
            	List<String> trackIds = (List)jsonObject.get(userId);
            	playTracksMap.put(userId, trackIds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playTracksMap;
	}
	
}
