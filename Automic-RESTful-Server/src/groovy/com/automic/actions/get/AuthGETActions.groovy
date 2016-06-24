package com.automic.actions.get

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
	public static def logout(String version, params,Connection conn,request,String TOKEN){return "logout${version}"(params,conn)}
	public static def admin(String version, params,Connection conn,request,String TOKEN){return "admin${version}"(params,TOKEN)}
	
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
				int VALIDITY = 0;
				boolean ISADMIN = false;
				
				def InputJSON = new JsonSlurper().parseText(ConnectionFile.text)
				def ConnFoundInConfigFile = false;
				
				println InputJSON.connections.each{
					if(it.name == CONNECTIONNAME){
						if(it.dept!=null){DEPT = it.dept;}
						if(it.host!=null){HOST = it.host;}
						if(it.ports[0] != null && it.ports[0].isInteger()){PORT = it.ports[0].toInteger();}
						LANG = it.lang.toCharArray()[0];
						if(it.validity != null && it.validity.toInteger()){VALIDITY = it.validity.toInteger();}
						ISADMIN = it.RESTadmin;
					}
					
				}
				// connecting to AE Engine
				//String AEHostnameOrIp,int AECPPort,int AEClientToConnect,String AEUserLogin, String AEDepartment,String AEUserPassword,char AEMessageLanguage
				AECredentials creds = new AECredentials(HOST,PORT,CLIENTINT,LOGIN,DEPT,PWD,LANG);
				String token = ConnectionManager.connectToClient(creds,VALIDITY,ISADMIN);
		
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

	public static def adminv1(params,TOKEN){
		def SupportedThings = [:]
		SupportedThings = [
			'required_parameters': [],
			'optional_parameters': [],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage', 'clearall', 'encrypt (-> requires a binary key file)', 'decrypt (-> requires a binary key file)']
			]
		
		//String TOKEN = params.token;
		String METHOD = params.method;
		//String ADMINKEY = params.key;
		
		if( METHOD != null && METHOD.equalsIgnoreCase("usage")){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			return json
		}else{
			// added a method to clear all tokens (except the initiator of the request)
			if(METHOD != null && ConnectionManager.getConnectionItemFromToken(TOKEN).isAdmin() && METHOD.equalsIgnoreCase("clearall")){
				ConnectionManager.clearAllTokens(TOKEN)
			}
		
			// undocumented method for now?
			if(METHOD != null && ConnectionManager.getConnectionItemFromToken(TOKEN).isAdmin() && METHOD.equalsIgnoreCase("encrypt")){
				String CLEARSTR = params.key;
				if(CLEARSTR == null || CLEARSTR.equals("")){
					CommonJSONRequests.renderErrorAsJSON("parameter key cannot be empty.")
				}else{
				
					if(AECrypter.isBinKeyFilePresent()){
						String EncStrWithFile = AECrypter.enMaximWithBinFile(CLEARSTR)
						JsonBuilder json = new JsonBuilder([status: "success", encrypted: EncStrWithFile])
						return json
					}else{
						return CommonJSONRequests.renderErrorAsJSON("No key file found on your system.")
					}

				}
			
			}
			
			// undocumented method for now?
			else if(METHOD != null && ConnectionManager.getConnectionItemFromToken(TOKEN).isAdmin() && METHOD.equalsIgnoreCase("decrypt")){
				String KEY = params.key;
				if(KEY == null || KEY.equals("")){
					return CommonJSONRequests.renderErrorAsJSON("parameter key cannot be empty.")
				}else{
					if(AECrypter.isBinKeyFilePresent()){
						String ClearStrWithFile = AECrypter.deMaximWithBinFile(KEY)
						JsonBuilder json = new JsonBuilder([status: "success", decrypted: ClearStrWithFile])
						return json
					}else{
						return CommonJSONRequests.renderErrorAsJSON("No key file found on your system.")
					}

				}
			
			}
			
			else{
				//if(ADMINKEY != null && ADMINKEY.equals(ADMINKEYFORCONNDISPLAY)){
				if(ConnectionManager.getConnectionItemFromToken(TOKEN).isAdmin() && METHOD == null){
						JsonBuilder json = ConnectionManager.getJSONFromConnectionPoolContent()
						return json
					
				}else{
					JsonBuilder json = new JsonBuilder([status: "error", message: "request denied"])
					return json
				}
			}
			

		}
	}
	
	/**
	 * @purpose Provide logout services for REST Server & AE
	 * @param params: URL params unsorted
	 * @param conn: Active AE Connection object to the AE
	 * @return formatted JSON response
	 */
	
	public static def logoutv1(params,Connection conn){
		def SupportedThings = [:]
		SupportedThings = [
			'required_parameters': ['token (format: token= < text > )'],
			'optional_parameters': [],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
		
		String TOKEN = params.token;
		String METHOD = params.method;
		
		if( METHOD != null && METHOD.equalsIgnoreCase("usage")){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			return json
		}else{
			boolean wasTokenFound = ConnectionManager.removeToken(TOKEN);
			conn.close();
			
			if(wasTokenFound){
				JsonBuilder json = new JsonBuilder([status: "success", message: "token "+ TOKEN+" was removed successfully"])
				return json
			}else{
				JsonBuilder json = new JsonBuilder([status: "success", message: "token "+ TOKEN+" was not found"])
				return json
			}
		}
	}
	
}
