package com.automic.connection;

public class ConnectionPoolItem {

	private com.uc4.communication.Connection Connection;
	public String ExpirationDate;
	public String User;
	public String Client;
	public String Dept;
	public String Host;
	public String Language;
	
	
	public String getHost() {
		return Host;
	}

	public void setHost(String host) {
		Host = host;
	}

	public String getLanguage() {
		return Language;
	}

	public void setLanguage(String language) {
		Language = language;
	}

	public String getUser() {
		return User;
	}

	public void setUser(String user) {
		User = user;
	}

	public String getClient() {
		return Client;
	}

	public void setClient(String client) {
		Client = client;
	}

	public String getDept() {
		return Dept;
	}

	public void setDept(String dept) {
		Dept = dept;
	}

	public ConnectionPoolItem(com.uc4.communication.Connection connection){
		this.Connection = connection;
	}
	
	public void setExpirationDate(String date){
		this.ExpirationDate = date;
	}
	
	public String getExpirationDate(){
		return this.ExpirationDate;
	}
	
	public com.uc4.communication.Connection getConnection(){
		return this.Connection;
	}
}
