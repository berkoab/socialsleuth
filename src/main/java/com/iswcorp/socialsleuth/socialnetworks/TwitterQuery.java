package com.iswcorp.socialsleuth.socialnetworks;

import java.util.ArrayList;

import twitter4j.HttpResponseCode;
import twitter4j.PagableResponseList;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.User;
import twitter4j.UserMentionEntity;

public class TwitterQuery {
	private Twitter twitter = null;
	private String startingPoint =  null;
	private ArrayList<String> tweets = new ArrayList<String>();
	private int mentionLevels;
	
	public TwitterQuery(String startingPoint, int mentionLevels) {
		this.startingPoint = startingPoint;
		this.twitter = TwitterAuth.createTwitter();
		this.mentionLevels = mentionLevels;
	}
	
	public ArrayList<User> getFollowers(String screenName, int pages, int count) throws TwitterException, InterruptedException {
		ArrayList<User> followers = new ArrayList<User>();
		long nextCursor = -1;
		int page = 0;
		do {
			RateLimitStatus listStatus = twitter.getRateLimitStatus().get("/followers/list");
			if(listStatus.getRemaining()==0) {
				System.out.println("Rate Limit exceeded. Sleeping for " + (listStatus.getSecondsUntilReset()+1) + " seconds.");
				Thread.sleep((listStatus.getSecondsUntilReset()+1)*1000);
			}
			
		    PagableResponseList<User> usersResponse = twitter.getFollowersList(screenName, nextCursor, count);
		    nextCursor = usersResponse.getNextCursor();
		    followers.addAll(usersResponse);
		    page++;
		} while ((nextCursor > 0) && (page<pages));
		return followers;
		
	}

	public ArrayList<User> getFriends(String screenName, int pages, int count) throws TwitterException, InterruptedException {
		ArrayList<User> friends = new ArrayList<User>();
		long nextCursor = -1;
		int page = 0;
		do {
			RateLimitStatus listStatus = twitter.getRateLimitStatus().get("/friends/list");
			if(listStatus.getRemaining()==0) {
				System.out.println("Rate Limit exceeded for /friends/list. Sleeping for " + (listStatus.getSecondsUntilReset()+1) + " seconds.");
				Thread.sleep((listStatus.getSecondsUntilReset()+1)*1000);
			}
			
		    PagableResponseList<User> usersResponse = twitter.getFriendsList(screenName, nextCursor, count);
		    nextCursor = usersResponse.getNextCursor();
		    friends.addAll(usersResponse);
		    page++;
		} while ((nextCursor > 0) && (page<pages));
		return friends;
		
	}
	
	private ResponseList<Status> getStatuses(User user) throws InterruptedException {
		RateLimitStatus listStatus = null;
		ResponseList<Status> status = null;
		try {
			listStatus = twitter.getRateLimitStatus().get("/statuses/user_timeline");
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(listStatus.getRemaining()==0) {
			System.out.println("Rate Limit exceeded for /statuses/user_timeline. Sleeping for " + (listStatus.getSecondsUntilReset()+1) + " seconds.");
			Thread.sleep((listStatus.getSecondsUntilReset()+1)*1000);
		}
		try {
			status = this.twitter.getUserTimeline(user.getScreenName());
		} catch (TwitterException e) {
			if (e.getStatusCode() == HttpResponseCode.UNAUTHORIZED ||
	            e.getStatusCode() == HttpResponseCode.NOT_FOUND) {
				return null;
	        }
	        else {
	            e.printStackTrace();
	        }
		}
		return status;
	}
	
	public void addStatuses(ArrayList<User> users, int mentionLevels) throws TwitterException, InterruptedException {
		for(User user:users) {
			ResponseList<Status> result = getStatuses(user);
			if(result==null) {
				continue;
			}
			int level = 0;
			for (Status status : result) {
				String json = TwitterObjectFactory.getRawJSON(status);
				if((json!=null)&&(!json.equals("null"))) {
					System.out.println(json);
					this.tweets.add(json);
				}
				if(level<mentionLevels) {
					ArrayList<User> mentions = new ArrayList<User>();
					for(UserMentionEntity mention:status.getUserMentionEntities()) {
						mentions.add(this.twitter.showUser(mention.getScreenName()));
					}
					addStatuses(mentions, level);
					level++;
				}
		    }
		}
	}
	
	public Twitter getTwitter() {
		return this.twitter;
	}
	
	public void run() throws TwitterException, InterruptedException {
		ArrayList<User> followers = this.getFollowers(this.startingPoint, 1, 20);
		ArrayList<User> friends = this.getFriends(this.startingPoint, 1, 20);
		addStatuses(followers, this.mentionLevels);
		addStatuses(friends, this.mentionLevels);
	}
	
	public void printTweets() {
		for(String tweet:tweets) {
			System.out.println(tweet);
		}
	}
	
	public static void main(String[] args) throws TwitterException, InterruptedException {
		TwitterQuery tw = new TwitterQuery("Ravens", 2);
		tw.run();
//		tw.printTweets();
	}

}
