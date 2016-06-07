package com.automic.connection;

import groovy.json.JsonBuilder
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

import com.uc4.api.DateTime
import com.uc4.communication.Connection;
import com.uc4.communication.requests.CreateSession;

public final class ConnectionManager {


	//public ArrayList<Connection> ConnectionList = new ArrayList<Connection>();
	
	public static HashMap<String, ConnectionPoolItem> ConnectionMap = new HashMap<String, ConnectionPoolItem>();
//	public static String GlobalExpDate = "20161231235959";
	public ConnectionManager(){ 
		
	} 
	
	public static String connectToClient(AECredentials credentials, int ValidityMinutes) throws IOException{ 
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
			return null;
		}
		
		SessionIdentifierGenerator sig = new SessionIdentifierGenerator();
		String CONNTOKEN = sig.nextSessionId();
		
		ConnectionPoolItem ConnItem = new ConnectionPoolItem(conn);
		
		DateTime NOW = DateTime.now()
		DateTime EXPDATE = DateTime.now().addMinutes(ValidityMinutes)
		String ExpDate = EXPDATE.getYear().toString()+EXPDATE.getMonth().toString()+EXPDATE.getDay().toString()+EXPDATE.getHour().toString()+EXPDATE.getMinute().toString()+EXPDATE.getSecond().toString()
		ConnItem.setExpirationDate(EXPDATE.toString());

		ConnectionMap.put(CONNTOKEN,ConnItem);
		//showConnectionPoolContent();
		return CONNTOKEN;
		
	}
	public static boolean removeToken(String token){
		ConnectionPoolItem item = ConnectionMap.remove(token);
		if(item == null){
			return false;
		}else{
			return true;
		}
	}
	public static int checkTokenValidity(String token) throws IOException, ParseException{
		if(token == null || token == ""){
			return -1;
		}else{
			if(!isTokenValid(token)){
				//token doesnt exist
				return -2;
			}else{
				// token none null & token actually known in hash
				String ExpDate = getExpDateFromToken(token);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date ExpDateAsDate = sdf.parse(ExpDate);
				Date now = new Date();
				if(now.before(ExpDateAsDate)){
					// token ok
					return 0;
				}else{
				// expired
					removeToken(token)
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

	public static JsonBuilder runTokenChecks(String token) {
		int CheckTokenRes = ConnectionManager.checkTokenValidity(token);
		if(CheckTokenRes == 0){
			return null;
		}else{ 
			JsonBuilder txt; 
			if(CheckTokenRes == -1){txt = new JsonBuilder([status: "error", message: "no token passed"])};
			if(CheckTokenRes == -2){txt = new JsonBuilder([status: "error", message: "token invalid"])};
			if(CheckTokenRes == -3){txt = new JsonBuilder([status: "error", message: "token expired"])};
			return txt;
		}
	}
	
	public static String bypassAuth(){
		char LANG = 'E';
		AECredentials creds = new AECredentials("192.168.1.60",2217,200,"BSP","AUTOMIC",'Un1ver$e',LANG);
		String TOKEN = ConnectionManager.connectToClient(creds,525600);
		return TOKEN;
	}
}
