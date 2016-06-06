package com.automic.actions

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

class AuthActions {

	public static def login(String version, params,Connection conn){return "login${version}"(params)}
	public static def logout(String version, params,Connection conn){return "logout${version}"(params,conn)}

	public static def loginv1(params){
		
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
				
				String DEPT;
				char LANG;
				int PORT = 0;
				int CLIENTINT = CLIENTSTR.toInteger();
				String HOST = '';
				
				//def inputFile = new File("C:\\Users\\bsp\\Documents\\workspace-ggts-3.6.4.RELEASE\\Automic-RESTful-Server\\connection_config.json")
				def inputFile = new File("connection_config.json")
				def InputJSON = new JsonSlurper().parseText(inputFile.text)
				def ConnFoundInConfigFile = false;
				
				println InputJSON.connections.each{
					//println it.name
					if(it.name == CONNECTIONNAME){
						//println "DEBUG: Connection Found!";
						DEPT = it.dept;
						HOST = it.host;
						PORT = it.ports[0].toInteger();
						LANG = 'E';
					}
					
				}
				
				// connecting to AE Engine
				//String AEHostnameOrIp,int AECPPort,int AEClientToConnect,String AEUserLogin, String AEDepartment,String AEUserPassword,char AEMessageLanguage
				AECredentials creds = new AECredentials(HOST,PORT,CLIENTINT,LOGIN,DEPT,PWD,LANG);
				String token = ConnectionManager.connectToClient(creds);
		
				if(token == null){
					JsonBuilder json = new JsonBuilder([status: "error", message: "authentication failed"])
					return json		
				}else{
					String ExpDate = ConnectionManager.getExpDateFromToken(token);
					JsonBuilder json = new JsonBuilder([status: "success", token:token, expdate: ExpDate])
					return json
	
				}
			}
		}
	}
	public static def loginv2(params){ 
		
		def SupportedThings = [:]
		SupportedThings = [
			'required_parameters': ['login (format: login= < text > )','pwd (format: pwd= < text > )','client (format: client= < integer > )','connection (format: connection= < text > )',
				'lang (format: lang= < character > )'],
			'optional_parameters': [],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
		
		String LOGIN = params.login;
		String PWD = params.pwd;
		String CLIENTSTR = params.client;
		String CONNECTIONNAME = params.connection;
		String METHOD = params.method ?: ''
		String LANGASSTR = params.lang

		if(METHOD.equalsIgnoreCase("usage")){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			return json
		}else{
		
			if(!LOGIN || !PWD || !CLIENTSTR || !CONNECTIONNAME || !LANGASSTR){
				JsonBuilder json = new JsonBuilder([status: "error", message: "wrong parameters or parameters missing (5 required: login= ,pwd= ,client= ,connection= , lang="])
				return json
			}else{
				
				String DEPT;
				char LANG = LANGASSTR.toUpperCase()[0];
				int PORT = 0;
				int CLIENTINT = CLIENTSTR.toInteger();
				String HOST = '';
				
				//def inputFile = new File("C:\\Users\\bsp\\Documents\\workspace-ggts-3.6.4.RELEASE\\Automic-RESTful-Server\\connection_config.json")
				def inputFile = new File("connection_config.json")
				def InputJSON = new JsonSlurper().parseText(inputFile.text)
				def ConnFoundInConfigFile = false;
				
				println InputJSON.connections.each{
					//println it.name
					if(it.name == CONNECTIONNAME){
						//println "DEBUG: Connection Found!";
						DEPT = it.dept;
						HOST = it.host;
						PORT = it.ports[0].toInteger();
						//LANG = 'E';
					}
					
				}
				
				// connecting to AE Engine
				//String AEHostnameOrIp,int AECPPort,int AEClientToConnect,String AEUserLogin, String AEDepartment,String AEUserPassword,char AEMessageLanguage
				AECredentials creds = new AECredentials(HOST,PORT,CLIENTINT,LOGIN,DEPT,PWD,LANG);
				String token = ConnectionManager.connectToClient(creds);
		
				if(token == null){
					JsonBuilder json = new JsonBuilder([status: "error", message: "authentication failed"])
					return json
				}else{
					String ExpDate = ConnectionManager.getExpDateFromToken(token);
					JsonBuilder json = new JsonBuilder([status: "success", token:token, expdate: ExpDate])
					return json
	
				}
			}
		}
	}
	
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
		
		if(METHOD.equalsIgnoreCase("usage")){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			return json
		}else{
			boolean wasTokenFound = ConnectionManager.removeToken(TOKEN);
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
