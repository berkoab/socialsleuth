package com.iswcorp.socialsleuth.socialnetworks;

import java.util.Map;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.PagableResponseList;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterListener;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public abstract class TwitterAuth {
	public static final String CONSUMER_KEY = "RR6qXq23mPi3BNxVsN8gwzkvm";
	public static final String CONSUMER_SECRET = "MbuEcZaKKOo8MmAvlvZ2ngmk9qSaXqPY1yRRZ1Mj1orWgJZgJq";
	public static final String ACCESS_TOKEN = "407790264-5khSqNcRNRU19oqNy0bzzADLFuWY46W3J7EQuhpb";
	public static final String ACCESS_SECRET = "LlVrRGhlGAgajvQxyKigP5d4dV7GaqCPDYE9nHX6EWJhD";
	
	private static ConfigurationBuilder getCB() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
			.setOAuthConsumerKey(CONSUMER_KEY)
			.setOAuthConsumerSecret(CONSUMER_SECRET)
			.setOAuthAccessToken(ACCESS_TOKEN)
			.setOAuthAccessTokenSecret(ACCESS_SECRET);
		cb.setJSONStoreEnabled(true);
		
		return cb;
	}
	
	public static Twitter createTwitter() {
		Twitter twitter = null;
		try {
			twitter = new TwitterFactory(getCB().build()).getInstance(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return twitter;
	}
	
	public static AsyncTwitter createAsynchTwitter() {
		TwitterListener listener = new TwitterAdapter() {
	        @Override 
	        public void updatedStatus(Status status) {
	          System.out.println("Successfully updated the status to [" +
	                   status.getText() + "].");
	        }

			@Override
			public void gotFollowersList(PagableResponseList<User> users) {
				// TODO Auto-generated method stub
				super.gotFollowersList(users);
			}

			@Override
			public void gotFriendsList(PagableResponseList<User> users) {
				// TODO Auto-generated method stub
				super.gotFriendsList(users);
			}

			@Override
			public void gotRateLimitStatus(
					Map<String, RateLimitStatus> rateLimitStatus) {
				// TODO Auto-generated method stub
				super.gotRateLimitStatus(rateLimitStatus);
			}
	        
	        
	    };
	    // The factory instance is re-useable and thread safe.
//	    AsyncTwitterFactory factory = new AsyncTwitterFactory();
//	    AsyncTwitter asyncTwitter = factory.getInstance();
	    AsyncTwitter asyncTwitter = new AsyncTwitterFactory(getCB().build()).getInstance(); 
	    asyncTwitter.addListener(listener);
		return asyncTwitter;
	}
}
