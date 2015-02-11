package com.iswcorp.socialsleuth.socialnetworks;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterInfo {
	private String username;
	private String id;
	private String location;
	
	public void setUsername(String username) {
		this.username = username;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getUsername() {
		return username;
	}
	public String getId() {
		return id;
	}
	public String getLocation() {
		return location;
	}
	//example twitter search - http://twitter4j.org/en/code-examples.html
	public void searchForTweets(String username) throws TwitterException {
		Twitter twitter = TwitterFactory.getSingleton();
	    Query query = new Query("source:twitter4j "+username);
	    QueryResult result = twitter.search(query);
	    for (Status status : result.getTweets()) {
	        System.out.println("@" + status.getUser().getScreenName() + ":" + status.getText());
	    }
	}
}
