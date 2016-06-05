package com.automic.connection;

public class ConnectionPoolItem {

	private com.uc4.communication.Connection Connection;
	public String ExpirationDate;
	
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
