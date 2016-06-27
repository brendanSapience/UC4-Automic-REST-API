package com.automic.ae.actions.get

import com.uc4.api.SearchResultItem
import com.uc4.communication.Connection;
import com.uc4.communication.requests.SearchObject
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests;
import com.automic.utils.MiscUtils;
import com.automic.objects.AECrypter;

class AuthGETActions {

	public static def login(String version, params,File connFile,request){return "login${version}"(params,connFile)}
	public static def logout(String version, params,Connection conn,request,String TOKEN){return "logout${version}"(params,conn,TOKEN)}
	//public static def admin(String version, params,Connection conn,request,String TOKEN){return "admin${version}"(params,TOKEN)}
	public static def show(String version, params,Connection conn,request,String TOKEN){return "show${version}"(params,TOKEN)}
	//public static def manage(String version, params,Connection conn,request,String TOKEN){return "show${version}"(params,TOKEN)}
	
	/**
	 * @purpose Provide login / authentication services to AE
	 * @param params: URL params unsorted
	 * @param ConnectionFile: File (containing connection parameters to AE)
	 * @return formatted JSON response
	 */
	
	public static def loginv1(params, File ConnectionFile){
		
		def SupportedThings = [:]
		SupportedThings = [
			'required_parameters': ['login (format: login= < text > )','pwd (format: pwd= < text > )','client (format: client= < integer > )','connection (format: connection= < text > )'],
			'optional_parameters': [],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
		
		String LOGIN = params.login;
		String PWD = params.pwd;
		String CLIENTSTR = params.client;
		String CONNECTIONNAME = params.connection;
		String METHOD = params.method ?: ''  // // infamous Elvis Operator. if params.method is null then give METHOD ''
		
		if(METHOD.equalsIgnoreCase("usage")){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			return json
		}else{
		
			if(!LOGIN || !PWD || !CLIENTSTR || !CONNECTIONNAME){
				JsonBuilder json = new JsonBuilder([status: "error", message: "wrong parameters or parameters missing (4 required: login= ,pwd= ,client= ,connection= "])
				return json
			}else{
				
			// should test for this..
				int CLIENTINT = CLIENTSTR.toInteger();
			
				String DEPT = '';
				char LANG;
				int PORT = 0;
				String HOST = '';
				String ARAURL ='';
				int VALIDITY = 0;
				boolean ISADMIN = false;
				
				def InputJSON = new JsonSlurper().parseText(ConnectionFile.text)
				def ConnFoundInConfigFile = false;
				
				InputJSON.connections.each{
					if(it.name == CONNECTIONNAME){
						if(it.dept!=null){DEPT = it.dept;}
						if(it.host!=null){HOST = it.host;}
						if(it.ports[0] != null && it.ports[0].isInteger()){PORT = it.ports[0].toInteger();}
						LANG = it.lang.toCharArray()[0];
						if(it.validity != null && it.validity.toInteger()){VALIDITY = it.validity.toInteger();}
						if(it.araurl != null){ARAURL = it.araurl;}
						if(it.RESTadmin != null){ISADMIN = it.RESTadmin;}
						
						
					}
					
				}
				// connecting to AE Engine
				//String AEHostnameOrIp,int AECPPort,int AEClientToConnect,String AEUserLogin, String AEDepartment,String AEUserPassword,char AEMessageLanguage
				AECredentials creds = new AECredentials(HOST,PORT,CLIENTINT,LOGIN,DEPT,PWD,LANG);
				String token = ConnectionManager.connectToClient(creds,VALIDITY,ISADMIN,ARAURL);
		
				if(token.startsWith("--MESSAGE: ")){
					String Message = token.replace("--MESSAGE: ","")
					JsonBuilder json = new JsonBuilder([status: "error", message: Message])
					return json		
				}else{
					String ExpDate = ConnectionManager.getExpDateFromToken(token);
					JsonBuilder json = new JsonBuilder([status: "success", token:token, expdate: ExpDate])
					return json
	
				}
			}
		}
	}
	
	/**
	 * @purpose returns the list of active (valid & expired) tokens on the REST server + the connection parameters for each
	 * @param params: URL params unsorted
	 * @return formatted JSON response
	 * @additional Can only return content if the user / token passed in URL is marked as "RESTadmin" in the ConnectionConfig.json file
	 */

	public static def showv1(params,TOKEN){
		def SupportedThings = [:]
		SupportedThings = [
			'required_parameters': [],
			'optional_parameters': [],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
		
		//String TOKEN = params.token;
		String METHOD = params.method;
		//String ADMINKEY = params.key;
		
		if( METHOD != null && METHOD.equalsIgnoreCase("usage")){
			JsonBuilder json
			if(ConnectionManager.getConnectionItemFromToken(TOKEN).isAdmin()){
				json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			}else{
				json = CommonJSONRequests.renderErrorAsJSON("request denied")
			}
			return json
		}else{
			if(ConnectionManager.getConnectionItemFromToken(TOKEN).isAdmin() && METHOD == null){
				return ConnectionManager.getJSONFromConnectionPoolContent()
				
			}else{
				return CommonJSONRequests.renderErrorAsJSON("request denied")
			}
		}
	}
		
//			if(METHOD != null && ConnectionManager.getConnectionItemFromToken(TOKEN).isAdmin() && METHOD.equalsIgnoreCase("clearall")){
//				ConnectionManager.clearAllTokens(TOKEN)
//			}

	
	/**
	 * @purpose Provide logout services for REST Server & AE
	 * @param params: URL params unsorted
	 * @param conn: Active AE Connection object to the AE
	 * @return formatted JSON response
	 */
	
	public static def logoutv1(params,Connection conn,String TOKEN){
		def SupportedThings = [:]
		SupportedThings = [
			'required_parameters': ['token (format: token= < text > )'],
			'optional_parameters': ['scope (format: scope=all) -> removes all tokens / logout all users'],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
		
		//String TOKEN = params.token;
		String METHOD = params.method;
		String SCOPE = params.scope;
		
		if( METHOD != null && METHOD.equalsIgnoreCase("usage")){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			return json
		}else{
			if(SCOPE == null || !SCOPE.equals("all")){
				boolean wasTokenFound = ConnectionManager.removeToken(TOKEN);
				conn.close();
				
				if(wasTokenFound){
					JsonBuilder json = new JsonBuilder([status: "success", message: "token "+ TOKEN+" was removed successfully"])
					return json
				}else{
					JsonBuilder json = new JsonBuilder([status: "success", message: "token "+ TOKEN+" was not found"])
					return json
				}
			}else{
				if(ConnectionManager.getConnectionItemFromToken(TOKEN).isAdmin()){
					ConnectionManager.clearAllTokens(TOKEN)
					return ConnectionManager.getJSONFromConnectionPoolContent()
				}else{
					return CommonJSONRequests.renderErrorAsJSON("request denied")
				}
				
			}
		}
	}
}
