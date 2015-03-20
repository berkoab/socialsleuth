package com.iswcorp.socialsleuth.socialnetworks;

import java.util.ArrayList;

import twitter4j.TwitterObjectFactory;
import twitter4j.User;

public class TwitterUser {
	private User user;
	private ArrayList<User> friends = new ArrayList<User>();
	private ArrayList<User> followers = new ArrayList<User>();
	private ArrayList<String> statuses = new ArrayList<String>();
	private ArrayList<String> mentions = new ArrayList<String>();
	private int level;
	private String pedigree = "";
	
	public TwitterUser() {}
	
	public TwitterUser(User user, int level) {
		this.user = user;
		this.level = level;
	}
	
	public TwitterUser(User user, TwitterUser parent, int level) {
		this.user = user;
		this.setPedigree(parent.getPedigree() + " <- " + this.user.getScreenName());
		this.level = level;
	}
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public ArrayList<User> getFriends() {
		return friends;
	}
	public void setFriends(ArrayList<User> friends) {
		this.friends = friends;
	}
	public ArrayList<User> getFollowers() {
		return followers;
	}
	public void setFollowers(ArrayList<User> followers) {
		this.followers = followers;
	}

	public ArrayList<String> getMentions() {
		return mentions;
	}

	public void setMentions(ArrayList<String> mentions) {
		this.mentions = mentions;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public ArrayList<String> getStatuses() {
		return statuses;
	}

	public void setStatuses(ArrayList<String> statuses) {
		this.statuses = statuses;
	}
	
	public String toString() {
		return TwitterObjectFactory.getRawJSON(this.user);
	}

	public String getPedigree() {
		return this.pedigree;
	}

	public void setPedigree(String pedigree) {
		this.pedigree = pedigree;
	}
}
