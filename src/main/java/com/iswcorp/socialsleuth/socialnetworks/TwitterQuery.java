package com.iswcorp.socialsleuth.socialnetworks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.Gson;

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
	private int maxLevel;
//	private BufferedWriter bw;
	private String dir = null;
	private int count;
	private Gson gson = null;
	
	public TwitterQuery(String startingPoint, int maxLevel, String dir, int count) {
		this.startingPoint = startingPoint;
		this.twitter = TwitterAuth.createTwitter();
		this.maxLevel = maxLevel;
		this.dir = dir;
		this.count = count;
		this.gson = new Gson();
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
		int secondsUntilReset = status.getSecondsUntilReset()+15;
		System.out.println("Rate Limit exceeded for " + statusOf + ". Sleeping for " + (secondsUntilReset) + " seconds....");
		while(secondsUntilReset>0) {
			try {
				System.out.println("Sleeping for " + secondsUntilReset + " seconds....");
				Thread.sleep(60*1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			secondsUntilReset = secondsUntilReset - 60;
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
					getStatus(e, "/followers/list");
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
					getStatus(e, "/friends/list");
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
	
	private void write(BufferedWriter bw, User user) {
		try {
			String json = gson.toJson(user);
			System.out.println(json);
			bw.write(json);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void processUser(TwitterUser user, int maxLevel, int level, int pages, int count, String parent) {
		
//		try {
//			for(String status:user.getStatuses()) {	
//				writer.write(status+"\n");
//			}
//			writer.flush();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		if(user.getLevel()<maxLevel) {
			user.setFollowers(this.getFollowers(user.getUser().getScreenName(), pages, count));
			user.setFriends(this.getFriends(user.getUser().getScreenName(), pages, count));
			this.addStatusesAndMentions(user, level);
			BufferedWriter statusWriter = openWriter("twitter_"+user.getUser().getId()+"_statuses.json");
			for(String status:user.getStatuses()) {	
				try {
					statusWriter.write(status);
					statusWriter.newLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				write(statusWriter, status+"\n");
			}
			closeWriter(statusWriter);
			BufferedWriter followersBw = openWriter("twitter_"+user.getUser().getId()+"_followers.json");
			for(User follower:user.getFollowers()) {
				write(followersBw, follower);
				if(!follower.getScreenName().equals(parent)) {
					processUser(new TwitterUser(follower), maxLevel, user.getLevel(), pages, count, 
							user.getUser().getScreenName());
				}
			}
			closeWriter(followersBw);
			BufferedWriter friendsBw = openWriter("twitter_"+user.getUser().getId()+"_friends.json");
			for(User friend:user.getFriends()) {
				write(friendsBw, friend);
				if(!friend.getScreenName().equals(parent)) {
					processUser(new TwitterUser(friend), maxLevel, user.getLevel(), pages, count, 
							user.getUser().getScreenName());
				}
			}
			closeWriter(friendsBw);
			BufferedWriter mentionsBw = openWriter("twitter_"+user.getUser().getId()+"_mentions.json");
			for(String mention:user.getMentions()) {
				TwitterUser mentionUser = this.createTwitterUser(mention);
				write(mentionsBw, mentionUser.getUser());
				if(!mention.equals(parent)) {
					processUser(mentionUser, maxLevel, user.getLevel(), pages, count, 
							user.getUser().getScreenName());
				}
			}
			closeWriter(mentionsBw);
		}
	}
	
	private BufferedWriter openWriter(String fileName) {
		File file = null;
		FileWriter fw = null;
		try {
			file =new File(this.dir, fileName);
	    	if(!file.exists()){
				file.createNewFile();
	    	}
			fw = new FileWriter(file);
//	    	this.bw = new BufferedWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new BufferedWriter(fw);
	}
	
	private void closeWriter(BufferedWriter bw) {
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
//		this.openWriter(this.fileName);
		TwitterUser user = this.createTwitterUser(this.startingPoint);
		BufferedWriter startingPointBw = openWriter("twitter_"+user.getUser().getId()+".json");
		write(startingPointBw, user.getUser());
		closeWriter(startingPointBw);
		this.processUser(user, this.maxLevel, 0, 1, this.count, "");
//		this.closeWriter();
	}
	
	
	public static void main(String[] args) {
		String startPoint = args[0];
		int levels = Integer.valueOf(args[1]);
		String dir = args[2];
		int count = Integer.valueOf(args[3]);
		TwitterQuery tw = new TwitterQuery(startPoint, levels, dir, count);
		tw.run();
	}

}
