package com.automic;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.UnresolvedAddressException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.uc4.communication.Connection;
import com.uc4.communication.requests.CreateSession;

public final class ConnectionManager {


	//public ArrayList<Connection> ConnectionList = new ArrayList<Connection>();
	
	public static HashMap<String, ConnectionPoolItem> ConnectionMap = new HashMap<String, ConnectionPoolItem>();
	public static String GlobalExpDate = "20161231235959";
	public ConnectionManager(){ 
		
	} 
	
	public static String connectToClient(AECredentials credentials) throws IOException{ 
		Connection conn = null;
		//System.out.println("Authenticating to Client "+credentials.getAEClientToConnect()+" with user "+credentials.getAEUserLogin());
		try{ 
			conn = Connection.open(credentials.getAEHostnameOrIp(), credentials.getAECPPort());
		}catch (UnresolvedAddressException e){
			System.out.println(" -- ERROR: Could Not Resolve Host or IP: "+credentials.getAEHostnameOrIp());
			return null;
		}catch (ConnectException c){
			System.out.println(" -- ERROR: Could Not Connect to Host: " + credentials.getAEHostnameOrIp());
			System.out.println(" --     Hint: is the host or IP reachable?");
			return null;
			
		}
		
		CreateSession sess = conn.login(credentials.getAEClientToConnect(), credentials.getAEUserLogin(), 
				credentials.getAEDepartment(), credentials.getAEUserPassword(), credentials.getAEMessageLanguage());
		
		if(sess.getMessageBox()!=null){
			System.out.println("-- Error: " + sess.getMessageBox()); 
			//System.exit(990);
			return null;
		}
		// Check Server Version:
//		String serverVersion = conn.getSessionInfo().getServerVersion();
//		if(! SupportedAEVersions.SupportedVersions.contains(serverVersion)){
//			System.err.println( " -- Error! Version of the Automation Engine does not seem supported.");
//			System.err.println( " -- current version is: "+serverVersion);
//			System.err.println( " -- versions supported: "+SupportedAEVersions.SupportedVersions.toString());
//			System.exit(1);
//		}
		
		SessionIdentifierGenerator sig = new SessionIdentifierGenerator();
		String CONNTOKEN = sig.nextSessionId();
		
		ConnectionPoolItem ConnItem = new ConnectionPoolItem(conn);
		ConnItem.setExpirationDate(GlobalExpDate);

		ConnectionMap.put(CONNTOKEN,ConnItem);
		return CONNTOKEN;
		
	}
	
	public static int checkTokenValidity(String token) throws IOException, ParseException{
		if(token == null || token == ""){
			return -1;
		}else{
			if(!isTokenValid(token)){
				return -2;
			}else{
				// token none null & token actually known in hash
				String ExpDate = getExpDateFromToken(token);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
				Date ExpDateAsDate = sdf.parse(ExpDate);
				Date now = new Date();
				if(now.before(ExpDateAsDate)){
					return 0;
				}else{
					return -3;
				}
			}
		}
	}
	
	public static boolean isTokenValid(String token){
		if(ConnectionMap.get(token)!=null){
			return true;
		}else{
			return false;
		}
	}
	public static ConnectionPoolItem getConnectionItemFromToken(String token) throws IOException{
		return ConnectionMap.get(token);
	}
	
	public static Connection getConnectionFromToken(String token) throws IOException{
		return ConnectionMap.get(token).getConnection();
	}
	
	public static String getExpDateFromToken(String token) throws IOException{
		return ConnectionMap.get(token).getExpirationDate();
	}
	
	public static void showConnectionPoolContent(){
		 Set set = ConnectionMap.entrySet();
	      Iterator iterator = set.iterator();
	      while(iterator.hasNext()) {
	         Map.Entry mentry = (Map.Entry)iterator.next(); 
	         System.out.print("Token: "+ mentry.getKey());
	         ConnectionPoolItem c1 = (ConnectionPoolItem)mentry.getValue();
	         System.out.println("\t Exp Date: "+ c1.ExpirationDate);
	      }
	}

	public static boolean runTokenChecks(String token) {
		int CheckTokenRes = ConnectionManager.checkTokenValidity(token);
		if(CheckTokenRes == 0){
			return true;
		}else{ 
		String txt =''; 
			if(CheckTokenRes == -1){txt = '{"status":"error","message":"no token passed"}'};
			if(CheckTokenRes == -2){txt = '{"status":"error","message":"token invalid"}'};
			if(CheckTokenRes == -3){txt = '{"status":"error","message":"token expired"}'};
			//render(text:  txt, contentType: "text/json", encoding: "UTF-8")
			return false;
		}
	}
	
	public static String bypassAuth(){
		char LANG = 'E';
		AECredentials creds = new AECredentials("192.168.1.60",2217,200,"BSP","AUTOMIC",'Un1ver$e',LANG);
		String TOKEN = ConnectionManager.connectToClient(creds);
		return TOKEN;
	}
}
