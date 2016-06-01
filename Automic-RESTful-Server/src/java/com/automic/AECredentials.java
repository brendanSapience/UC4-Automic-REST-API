package com.automic;

public class AECredentials {

	/** 
	 *    !!! Change the credentials below to your own system !!!
	 **/
	
	private String AEHostnameOrIp;  // Automation Engine IP Adr 
	private int AECPPort; 		    // "Primary" Communication Process port
	private int AEClientToConnect;  // AE Client Number (0 - 9999)
	private String AEDepartment;    // User Department
	private String AEUserLogin;  	// AE User Login
	private String AEUserPassword; 	// AE User Password
	private char AEMessageLanguage; // Language: 'E' or 'D', or 'F'
	
	public AECredentials(String AEHostnameOrIp,int AECPPort,int AEClientToConnect,String AEUserLogin, String AEDepartment,String AEUserPassword,char AEMessageLanguage ){
		
		this.AEHostnameOrIp = AEHostnameOrIp;
		this.AECPPort = AECPPort;
		this.AEClientToConnect = AEClientToConnect;
		this.AEDepartment = AEDepartment;
		this.AEUserLogin = AEUserLogin;
		this.AEUserPassword = AEUserPassword;
		this.AEMessageLanguage = AEMessageLanguage;
		
	}
	public void setAEHostnameOrIp(String aEHostnameOrIp) {
		AEHostnameOrIp = aEHostnameOrIp;
	}
	public void setAECPPort(int aECPPort) {
		AECPPort = aECPPort;
	}
	public void setAEClientToConnect(int aEClientToConnect) {
		AEClientToConnect = aEClientToConnect;
	}
	public void setAEDepartment(String aEDepartment) {
		AEDepartment = aEDepartment;
	}
	public void setAEUserLogin(String aEUserLogin) {
		AEUserLogin = aEUserLogin;
	}
	public void setAEUserPassword(String aEUserPassword) {
		AEUserPassword = aEUserPassword;
	}
	public void setAEMessageLanguage(char aEMessageLanguage) {
		AEMessageLanguage = aEMessageLanguage;
	}
	public String getAEHostnameOrIp() {
		return AEHostnameOrIp;
	}
	public int getAECPPort() {
		return AECPPort;
	}
	public int getAEClientToConnect() {
		return AEClientToConnect;
	}
	public String getAEDepartment() {
		return AEDepartment;
	}
	public String getAEUserLogin() {
		return AEUserLogin;
	}
	public String getAEUserPassword() {
		return AEUserPassword;
	}
	public char getAEMessageLanguage() {
		return AEMessageLanguage;
	}
}

