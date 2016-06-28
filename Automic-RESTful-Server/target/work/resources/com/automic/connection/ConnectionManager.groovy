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
import com.automic.objects.AECrypter

/**
 *
 * @author bsp
 * @purpose Static class (it needs to be accessible in multiple places). This is what actively handles the pool of connections.
 * => Connections, when successfully established, are stored in a HashMap (ConnectionMap) with the key being the token itself (unique) and the value being a ConnectionPoolItem (itself containing the Connection object)
 *
 */

public final class ConnectionManager {

	// main HashMap containing all ConnectionPoolItems
	public static HashMap<String, ConnectionPoolItem> ConnectionMap = new HashMap<String, ConnectionPoolItem>();

	
	public static String DEVHOST = "AETestHost";
	public static int DEVPORT = 2217;
	public static int DEVCLIENT = 100;
	public static String DEVLOGIN = "ARA"; //"BSP";
	public static String DEVDEPT = "ARA";
	public static String DEVPWD = 'ara';
	public static char LANG = 'E';
	public static int DEVEXPIRYPERIOD = 525600;
	public static String DEVARAURL = "http://AETestHost/ARA";
	
	public ConnectionManager(){ 
		// leave empty
	} 
	
	public static String connectToClient(AECredentials credentials, int ValidityMinutes,boolean IsAdmin) throws IOException{
		return connectToClient( credentials,  ValidityMinutes, IsAdmin,"");
	}
	
	// Establish a connection to AE: if successful, a new ConnectionPoolItem is stored and the corresponding token is returned
	// IF Fails: returns an error message with details.
	
	public static String connectToClient(AECredentials credentials, int ValidityMinutes,boolean IsAdmin, String ARAUrl) throws IOException{ 
		Connection conn = null;
		//should try the list of ports
		try{ 
			//println "DEBUG : " + credentials.getAEHostnameOrIp() +":"+credentials.getAECPPort()
			conn = Connection.open(credentials.getAEHostnameOrIp(), credentials.getAECPPort());
		}catch (UnresolvedAddressException e){
			//System.out.println(" -- ERROR: Could Not Resolve Host or IP: "+credentials.getAEHostnameOrIp());
			return "--MESSAGE: Could Not Resolve Host or IP: "+credentials.getAEHostnameOrIp()
		}catch (ConnectException c){
			//System.out.println(" -- ERROR: Could Not Connect to Host: " + credentials.getAEHostnameOrIp());
			//System.out.println(" --     Hint: is the host or IP reachable?");
			return "--MESSAGE: Could Not Reach Host or IP: "+credentials.getAEHostnameOrIp()
		}catch(NoRouteToHostException n){
			return "--MESSAGE: Could Not Reach Host or IP: "+credentials.getAEHostnameOrIp()
		}
		
		String PASSWORD = ''
		if(credentials.getAEUserPassword().startsWith("--10")){
			PASSWORD = AECrypter.deMaximWithInternalKey(credentials.getAEUserPassword())
		}else{
			PASSWORD = credentials.getAEUserPassword()
		}
		
		CreateSession sess = conn.login(credentials.getAEClientToConnect(), credentials.getAEUserLogin(), 
				credentials.getAEDepartment(), PASSWORD, credentials.getAEMessageLanguage());
		
		if(sess.getMessageBox()!=null){
			return "--MESSAGE: " + sess.getMessageBox(); 
			//return null;
		}
		
		SessionIdentifierGenerator sig = new SessionIdentifierGenerator();
		String CONNTOKEN = sig.nextSessionId();
		
		ConnectionPoolItem ConnItem = new ConnectionPoolItem(conn);

		DateTime NOW = DateTime.now()
		DateTime EXPDATE = DateTime.now().addMinutes(ValidityMinutes)
		String ExpDate = EXPDATE.getYear().toString()+EXPDATE.getMonth().toString()+EXPDATE.getDay().toString()+EXPDATE.getHour().toString()+EXPDATE.getMinute().toString()+EXPDATE.getSecond().toString()
		ConnItem.setExpirationDate(EXPDATE.toString());
		ConnItem.setUser(credentials.getAEUserLogin());
		ConnItem.setClient(credentials.getAEClientToConnect().toString());
		ConnItem.setDept(credentials.getAEDepartment());
		ConnItem.setHost(credentials.getAEHostnameOrIp());
		ConnItem.setLanguage(credentials.getAEMessageLanguage().toString());
		ConnItem.setCreationDate(DateTime.now().toString());
		ConnItem.setPassword(credentials.getAEUserPassword());
		ConnItem.setAdmin(IsAdmin);
		ConnItem.setARAUrl(ARAUrl);
		
		ConnectionMap.put(CONNTOKEN,ConnItem);

		return CONNTOKEN;
		
	}
	//Clear token / Connection pool
	public static boolean clearAllTokens(String initiatorToken){
		ArrayList<String> TokensToClear = new ArrayList<String>()
		
		ConnectionMap.entrySet().each {
			if(!it.key.equals(initiatorToken)){
				TokensToClear.add(it.key)
			}
		}
		
		TokensToClear.each {
			ConnectionMap.remove(it)
		}
	}
	
	//on logout operations, the token needs to be removed from the Hash and the Connection needs to be closed
	public static boolean removeToken(String token){
		ConnectionPoolItem item = ConnectionMap.remove(token);
		if(item == null){
			return false;
		}else{
			return true;
		}
	}
	
	// Check the status of a token: no token or Valid or expired or inexistant. should be a private method?
	private static int checkTokenValidity(String token) throws IOException, ParseException{
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
	
	// checks if token is known regardless of status
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
	
	// this is more of a debug method.. it isnt used anywhere actively.
	public static void showConnectionPoolContent(){
		 Set set = ConnectionMap.entrySet();
	      Iterator iterator = set.iterator();
	      while(iterator.hasNext()) {
	         Map.Entry mentry = (Map.Entry)iterator.next(); 
	         //System.out.print("Token: "+ mentry.getKey());
	         ConnectionPoolItem c1 = (ConnectionPoolItem)mentry.getValue();
	        // System.out.println("\t Exp Date: "+ c1.ExpirationDate);
	      }
	}

	// returns a JSON formatted message with all ConnectionMap Hash info
	public static JsonBuilder getJSONFromConnectionPoolContent(){
			def data = [
			status: "success",
			count: ConnectionMap.size(),
			data: ConnectionMap.collect {k,v ->
				["token": k, "expdate":v.getExpirationDate(), "host":v.getHost(),"user":v.getUser(),"client":v.getClient(),"dept":v.getDept(), "created" : v.getCreationDate()]
			}
		  ]

		def json = new JsonBuilder(data)
		return json;

   }
	
	public static JsonBuilder runTokenChecks(String token) {
		int CheckTokenRes = ConnectionManager.checkTokenValidity(token);
		if(CheckTokenRes == 0){
			return null;
		}else{ 
			JsonBuilder txt; 
			if(CheckTokenRes == -1){txt = new JsonBuilder([status: "error", message: "no token passed"])};
			if(CheckTokenRes == -2){txt = new JsonBuilder([status: "error", message: "token invalid: " + token])};
			if(CheckTokenRes == -3){txt = new JsonBuilder([status: "error", message: "token expired: " + token])};
			return txt;
		}
	}
	
	// this method is only used in Dev
	public static String bypassAuth(){
		AECredentials creds = new AECredentials(DEVHOST,DEVPORT,DEVCLIENT,DEVLOGIN,DEVDEPT,DEVPWD,LANG);
		return ConnectionManager.connectToClient(creds,DEVEXPIRYPERIOD,true,DEVARAURL);
	}
}
