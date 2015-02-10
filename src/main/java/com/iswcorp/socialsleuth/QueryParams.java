package com.iswcorp.socialsleuth;

public class QueryParams {
	private String username = null;
	private String email = null;
	private String userid = null;
	
	public QueryParams() {}
	
	public QueryParams(String username, String email, String userid) {
		this.username = username;
		this.email = email;
		this.userid = userid;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}	
}
