package com.iswcorp.socialsleuth;

import java.util.List;

import com.sappenin.ms.activitystrea.v1.Activity;

public class QueryResults {
	private List<String> usernames;
	private List<String> emails;
	private List<String> userids;
	private List<TwitterInfo> twitterInfos;
	private Activity activity;
	
	public List<String> getUsernames() {
		return usernames;
	}
	public List<String> getEmails() {
		return emails;
	}
	public List<String> getUserids() {
		return userids;
	}
	public List<TwitterInfo> getTwitterInfos() {
		return twitterInfos;
	}
	public String export() {
		return null;
	}
}