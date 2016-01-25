package com.rest.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is the API file which renders the JSON output based on the URL
 * @author Dinakaran
 *
 */
@Path("/users")
public class Users {

	private static final String USER_NAME = "username";

	private static final String PLAYS = "plays";

	private static final String FRIENDS = "friends";

	private static final String TRACKS = "tracks";

	private static final String URI = "uri";

	private static final String USERS = "users";
	
	private static final String SUB_URI = "subresource_uris";
	
	private static final String TRENDING = "trending";
	
	private static final String RATING= "rating";

	private Map<String, List<String>> playsMap;

	private Map<String, Map<String, Object>> trackDetails;

	private Map<String, List<String>> friendsMap;

	/**
	 * Build HasMap of JSON objects
	 */
	private void buildJSONMaps() {
		MusicParser mParser = new MusicParser();
		playsMap = mParser.parsePlaysJSON();
		trackDetails = mParser.parseTracksJSON();
		friendsMap = mParser.parseFriendsJSON();
	}

	@GET
	@Produces("application/json")
	public Response getUsers() throws JSONException {
		buildJSONMaps();
		JSONObject mainObj = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		Iterator<String> playsIterator = playsMap.keySet().iterator();
		while (playsIterator.hasNext()) {
			JSONObject jsonObject = new JSONObject();
			String userName = playsIterator.next();
			String url = "/users/" + userName;
			jsonObject.put(USER_NAME, userName);
			jsonObject.put(PLAYS, playsMap.get(userName).size());
			jsonObject.put(FRIENDS, friendsMap.get(userName).size());
			jsonObject.put(URI, url);
			jsonArray.put(jsonObject);
		}
		mainObj.put("Users", jsonArray);
		mainObj.put(URI, "/users");
		String result = mainObj.toString();
		return Response.status(200).entity(result).build();
	}
	
	/**
	 * FInd the total number of unique tracks
	 * @param tracksList list of tracks
	 * @return
	 */
	private int findTrackCount(List<String> tracksList){
		Set<String> trackSet = new HashSet<String>();
		int count =0;
		for(String track:  tracksList){
			if(!trackSet.contains(track)){
				trackSet.add(track);
				count++;
			}
		}
		return count;
	}

	@Path("{userName}")
	@GET
	@Produces("application/json")
	public Response getUser(@PathParam("userName") String userName) throws JSONException {
		buildJSONMaps();
		JSONObject jsonObject = new JSONObject();
		String url = "/users/" + userName;
		jsonObject.put(USER_NAME, userName);
		jsonObject.put(TRACKS, playsMap.get(userName).size());
		jsonObject.put(FRIENDS, friendsMap.get(userName).size());
		jsonObject.put(TRACKS, findTrackCount(playsMap.get(userName)));
		JSONObject subJsonObject = new JSONObject();
		subJsonObject.put(TRENDING, "/users/"+userName+"/trending");
		jsonObject.put(SUB_URI, subJsonObject);
		String result = jsonObject.toString();
		return Response.status(200).entity(result).build();
	}
	
	@Path("{userName}/trending")
	@GET
	@Produces("application/json")
	public Response getTrending(@PathParam("userName") String userName) throws JSONException {
		buildJSONMaps();
		JSONObject jsonObject = new JSONObject();
		String url = "/users/" + userName+"/trending";
		//Build the trending song list
		List<String> trendingSongs =buildTrendingSongs(userName);
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(trendingSongs);
		jsonObject.put(TRACKS, jsonArray);
		jsonObject.put(URI, url);
		String result = jsonObject.toString();
		return Response.status(200).entity(result).build();
	}
	
	private List<String> buildTrendingSongs(String userName) {
		List<String> trendingSongs = new ArrayList<String>();
		Map<String, Integer> overalltrend = getTrendingSongs(userName);
		Iterator<String> iterator = overalltrend.keySet().iterator();
		while(iterator.hasNext()){
			String track = iterator.next();
			trendingSongs.add(track);
		}
		return trendingSongs;
	}
	
	private Map<String, Integer> getTrendingSongs(String userName){
		List<String> friends = friendsMap.get(userName);
		friends.add(userName);
		List<String> tracks = new ArrayList<String>();
		//Friends track list all the tracks by the friends and how many times it was played
		Map<String, Map<String, Integer>> friendsTrack = new HashMap<String, Map<String, Integer>>();
		List<String> uniqueTracks = new ArrayList<String>();
		populateUserTrackCount(friends, friendsTrack, uniqueTracks);
		return findTrendingSongs(uniqueTracks, friendsTrack, friends.size());
	}
	
	/**
	 * This method builds 2 data structures.
	 * 1.)A HashMap which stores which user has what songs played and how many times they are played.
	 * 2.)A list of unique tracks.(A track list and no duplicates of songs).
	 * @param friends
	 * @param friendsTrack
	 * @param uniqueTracks
	 */
	private void populateUserTrackCount(List<String> friends,
			Map<String, Map<String, Integer>> friendsTrack, List<String> uniqueTracks) {
		for (String friend : friends) {
			List<String> tracks = playsMap.get(friend);
			Map<String, Integer> trackCount = new HashMap<String, Integer>();
			for (String track : tracks) {
				if (trackCount.containsKey(track)) {
					int count = trackCount.get(track);
					count++;
					trackCount.put(track, count);
				}else{
					uniqueTracks.add(track);
					trackCount.put(track, 1);
				}
			}
			friendsTrack.put(friend, trackCount);
		}
	}
	
	/**
	 * 
	 * @param uniqueTracks list of unique tracks from the friends group
	 * @param friendsTrack Map which shows each user and what are the songs played by them and the count of it
	 * @param totalFriends
	 * @return a HashMap which is the final tending songs with tracks as key and count as value
	 */
	private Map<String, Integer> findTrendingSongs(List<String> uniqueTracks,
			Map<String, Map<String, Integer>> friendsTrack, int totalFriends) {
		//Key is Tracks and the value total time played by the friends
		Map<String, Integer> totalScore = new HashMap<String, Integer>();
		//Key is tracks and the value is total number of friends who played this track
		Map<String, Integer> friendsCount = new HashMap<String, Integer>();
		for (String track : uniqueTracks) {
			Iterator<String> iterator = friendsTrack.keySet().iterator();
			while (iterator.hasNext()) {
				String userName = iterator.next();
				Map<String, Integer> trackMap = friendsTrack.get(userName);
				if(trackMap.get(track) != null){
					if (totalScore.containsKey(track)) {
						
						int totalCount = trackMap.get(track);
						totalCount += totalScore.get(track);
						totalScore.put(track, totalCount);
					}else{
						totalScore.put(track, 1);
					}
					if(friendsCount.containsKey(track)){
						int friendCount = friendsCount.get(track);
						friendCount++;
						friendsCount.put(track, friendCount);
					}else{
						friendsCount.put(track, 1);
					}
				}
			}

		}
		return calculateScoreForTracks(totalScore, friendsCount, totalFriends);
	}
	
	private Map<String, Integer> calculateScoreForTracks(Map<String, Integer> totalScore, 
			Map<String, Integer> friendsCount, int totalFriends){
		Map<String, Integer> overalltrend = new HashMap<String, Integer>();
		Iterator<String> iterator = totalScore.keySet().iterator();
		while(iterator.hasNext()){
			String track = iterator.next();
			int totalCount = totalScore.get(track);
			int friendCount = friendsCount.get(track);
			//Now get the rating for each track
			Map<String, Object> trackMap = trackDetails.get(track);
			int rating = ((Long)(trackMap.get(RATING))).intValue();
			int overallScore = (totalCount*(friendCount/totalFriends));
			//Now calculate score based on ratings
			overallScore = (overallScore * (rating/10));
			overalltrend.put(track, (int)overallScore);
		}
		int maxScore = findMaxScore(overalltrend);
		return eliminateSmallerValues(overalltrend, maxScore);
		
	}
	
	private Map<String, Integer> eliminateSmallerValues(Map<String, Integer> overalltrend, int maxScore){
		int thresholdValue = maxScore*(20/100);
		Iterator<String> iterator = overalltrend.keySet().iterator();
		while(iterator.hasNext()){
			String track = iterator.next();
			if(overalltrend.get(track)< thresholdValue){
				overalltrend.remove(track);
			}
		}
		return overalltrend;
	}
	
	private int findMaxScore(Map<String, Integer> overalltrend){
		int maxScore = 0;
		Iterator<String> iterator = overalltrend.keySet().iterator();
		while(iterator.hasNext()){
			String track = iterator.next();
			if(overalltrend.get(track)>maxScore){
				maxScore = overalltrend.get(track);
			}
		}
		return maxScore;
	}
	

	
}
