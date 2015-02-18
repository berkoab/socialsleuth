package com.iswcorp.socialsleuth.socialnetworks;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public abstract class TwitterAuth {
	public static final String CONSUMER_KEY = "RR6qXq23mPi3BNxVsN8gwzkvm";
	public static final String CONSUMER_SECRET = "MbuEcZaKKOo8MmAvlvZ2ngmk9qSaXqPY1yRRZ1Mj1orWgJZgJq";
	public static final String ACCESS_TOKEN = "407790264-5khSqNcRNRU19oqNy0bzzADLFuWY46W3J7EQuhpb";
	public static final String ACCESS_SECRET = "LlVrRGhlGAgajvQxyKigP5d4dV7GaqCPDYE9nHX6EWJhD";
	
	public static Twitter createTwitter() {
		Twitter twitter = null;
		try {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
				.setOAuthConsumerKey(CONSUMER_KEY)
				.setOAuthConsumerSecret(CONSUMER_SECRET)
				.setOAuthAccessToken(ACCESS_TOKEN)
				.setOAuthAccessTokenSecret(ACCESS_SECRET);
			cb.setJSONStoreEnabled(true);
			twitter = new TwitterFactory(cb.build()).getInstance(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return twitter;
	}
}
