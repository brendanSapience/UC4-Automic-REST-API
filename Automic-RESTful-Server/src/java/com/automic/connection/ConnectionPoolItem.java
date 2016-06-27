package com.automic.connection;

import java.io.File;

/**
 * 
 * @author bsp
 * @purpose this class represents one single item of the active pool of connections opened to AE by the REST server (including the actual Connection object)
 * 
 *
 */

public class ConnectionPoolItem {

	private com.uc4.communication.Connection Connection;
	private String ExpirationDate;
	private String User;
	private String Client;
	private String Dept;
	private String Host;
	private String Language;
	private String CreationDate;
	private String Password;
	private String ARAUrl;
	private boolean isAdmin = false;
	
	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	public String getARAUrl() {
		return ARAUrl;
	}

	public void setARAUrl(String url) {
		this.ARAUrl = url;
	}
	public String getCreationDate() {
		return CreationDate;
	}

	public void setCreationDate(String creationDate) {
		CreationDate = creationDate;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}

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
