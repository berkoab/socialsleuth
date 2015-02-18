package com.iswcorp.socialsleuth.socialnetworks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

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
	private int maxLevel;
	private BufferedWriter bw;
	private String fileName = null;
	private int count;
	
	public TwitterQuery(String startingPoint, int maxLevel, String fileName, int count) {
		this.startingPoint = startingPoint;
		this.twitter = TwitterAuth.createTwitter();
		this.maxLevel = maxLevel;
		this.fileName = fileName;
		this.count = count;
	}
	
	public void getStatus(TwitterException e, String statusOf) {
		Map<String, RateLimitStatus> statusMap = null;
		try {
			statusMap = twitter.getRateLimitStatus();
			System.out.println(statusMap);
		} catch (TwitterException e1) {
			e1.printStackTrace();
		}
		RateLimitStatus status = statusMap.get(statusOf);
		System.out.println("Rate Limit exceeded for " + statusOf + ". Sleeping for " + (status.getSecondsUntilReset()+10) + " seconds....");
		try {
			Thread.sleep((status.getSecondsUntilReset()+10)*1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	public ArrayList<User> getFollowers(String screenName, int pages, int count) {
		ArrayList<User> followers = new ArrayList<User>();
		long nextCursor = -1;
		int page = 0;
		do {
			PagableResponseList<User> usersResponse = null;
			boolean a = true;
			while(a) {
				try {
					usersResponse = twitter.getFollowersList(screenName, nextCursor, count);
				} catch (TwitterException e) {
					if(e.getErrorMessage().equals("Rate limit exceeded")) {
						getStatus(e, "/followers/list");
					} else {
						e.printStackTrace();
					}
				} finally {
					a = false;
				}
			}
			if(usersResponse!=null) {
			    nextCursor = usersResponse.getNextCursor();
			    followers.addAll(usersResponse);
			}
		    page++;
		} while ((nextCursor > 0) && (page<pages));
		return followers;
		
	}

	public ArrayList<User> getFriends(String screenName, int pages, int count) {
		ArrayList<User> friends = new ArrayList<User>();
		long nextCursor = -1;
		int page = 0;
		do {
			PagableResponseList<User> usersResponse = null;
			boolean a = true;
			while(a) {
				try {
					usersResponse = twitter.getFriendsList(screenName, nextCursor, count);
				} catch (TwitterException e) {
					if(e.getErrorMessage().equals("Rate limit exceeded")) {
						getStatus(e, "/friends/list");
					} else {
						e.printStackTrace();
					}
				} finally {
					a = false;
				}
			}
			if(usersResponse!=null) {
			    nextCursor = usersResponse.getNextCursor();
			    friends.addAll(usersResponse);
			}
		    page++;
		} while ((nextCursor > 0) && (page<pages));
		return friends;
		
	}
	
	private ResponseList<Status> getStatuses(User user) {
		ResponseList<Status> statuses = null;
		boolean a = true;
		while(a) {
			try {
				statuses = this.twitter.getUserTimeline(user.getScreenName());
			} catch (TwitterException e) {
				if (e.getStatusCode() == HttpResponseCode.UNAUTHORIZED ||
		            e.getStatusCode() == HttpResponseCode.NOT_FOUND) {
					return null;
		        } else if(e.getErrorMessage().equals("Rate limit exceeded")) {
		        	getStatus(e, "/statuses/user_timeline");
		        }
		        else {
		            e.printStackTrace();
		        }
			} finally {
				a = false;
			}
		}
		return statuses;
	}
	
	private TwitterUser createTwitterUser(String screenName) {
		TwitterUser twUser = new TwitterUser();
		boolean a = true;
		while(a) {
			try {
				twUser.setUser(this.twitter.showUser(screenName));
			} catch (TwitterException e) {
				getStatus(e, "/users/show/");
			} finally {
				a = false;
			}
		}
		return twUser;
	}
	
	private TwitterUser addStatusesAndMentions(TwitterUser user, int level) {
		ResponseList<Status> result = getStatuses(user.getUser());
		if(result==null) {
			return null;
		}
		user.setLevel(level++);
		for (Status status : result) {
			if((!status.isRetweet())&&(!status.isRetweeted())) {
				String json = TwitterObjectFactory.getRawJSON(status);
				if((json!=null)&&(!json.equals("null"))) {
					System.out.println(json);
					user.getStatuses().add(json);
				}
				ArrayList<String> mentions = new ArrayList<String>();
				for(UserMentionEntity mention:status.getUserMentionEntities()) {
					mentions.add(mention.getScreenName());
				}
				user.setMentions(mentions);
			}
	    }
		return user;
	}
	
	private void processUser(TwitterUser user, int maxLevel, int level, int pages, int count) {
		this.addStatusesAndMentions(user, level);
		try {
			for(String status:user.getStatuses()) {	
				bw.write(status+"\n");
			}
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(user.getLevel()<maxLevel) {
			user.setFollowers(this.getFollowers(user.getUser().getScreenName(), pages, count));
			user.setFriends(this.getFollowers(user.getUser().getScreenName(), pages, count));
			for(User follower:user.getFollowers()) {
				processUser(new TwitterUser(follower), maxLevel, user.getLevel(), pages, count);
			}
			for(User friend:user.getFriends()) {
				processUser(new TwitterUser(friend), maxLevel, user.getLevel(), pages, count);
			}
			for(String mention:user.getMentions()) {
				processUser(this.createTwitterUser(mention), maxLevel, user.getLevel(), pages, count);
			}
		}
	}
	
	private BufferedWriter openWriter(String fileName) {
		File file = null;
		try {
			file =new File(fileName);
	    	if(!file.exists()){
				file.createNewFile();
	    	}
			FileWriter fw = new FileWriter(file);
	    	this.bw = new BufferedWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bw;
	}
	
	private void closeWriter() {
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		this.openWriter(this.fileName);
		this.processUser(this.createTwitterUser(this.startingPoint), this.maxLevel, 0, 1, this.count);
		this.closeWriter();
	}
	
	public void printTweets() {
		for(String tweet:tweets) {
			System.out.println(tweet);
		}
	}
	
	public static void main(String[] args) throws TwitterException, InterruptedException {
		String startPoint = args[0];
		int levels = Integer.valueOf(args[1]);
		String fileName = args[2];
		int count = Integer.valueOf(args[3]);
		TwitterQuery tw = new TwitterQuery(startPoint, levels, fileName, count);
		tw.run();
	}

}
