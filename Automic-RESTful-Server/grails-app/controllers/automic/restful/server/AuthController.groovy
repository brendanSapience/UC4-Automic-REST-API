package automic.restful.server

import com.automic.AECredentials
import com.automic.ConnectionManager
import com.uc4.communication.Connection
import groovy.json.JsonSlurper

class AuthController {

    def index() { }
	
	def authenticate = {
		
		String PRODUCT = params.product;
		String APIVERSION = params.version;
		String LOGIN = params.login;
		String PWD = params.pwd;
		String CLIENTSTR = params.client;
		String CONNECTIONNAME = params.connection;
		
		if(!LOGIN || !PWD || !CLIENTSTR || !CONNECTIONNAME){
			String txt = '{"status":"error","message":"wrong parameters or parameters missing"}';
			render(text: txt , contentType: "text/json", encoding: "UTF-8")
		}else{
			
			String DEPT;
			char LANG;
			int PORT = 0;
			int CLIENTINT = CLIENTSTR.toInteger();
			String HOST = '';
			
			//def inputFile = new File("C:\\Users\\bsp\\Documents\\workspace-ggts-3.6.4.RELEASE\\Automic-RESTful-Server\\connection_config.json")
			def inputFile = new File("connection_config.json")
			def InputJSON = new JsonSlurper().parseText(inputFile.text)
			//InputJSON.each{ println it.connection.name}
			
			def ConnFoundInConfigFile = false;
			
			println InputJSON.connections.each{
				//println it.name
				if(it.name == CONNECTIONNAME){
					println "DEBUG: Connection Found!";
					DEPT = it.dept;
					HOST = it.host;
					PORT = it.ports[0].toInteger();
					LANG = 'E';
					println "DEBUG: Connection Params:" + DEPT +":"+ HOST +":"+PORT;
				}
				
			}
			
			// connecting to AE Engine
			//String AEHostnameOrIp,int AECPPort,int AEClientToConnect,String AEUserLogin, String AEDepartment,String AEUserPassword,char AEMessageLanguage
			AECredentials creds = new AECredentials(HOST,PORT,CLIENTINT,LOGIN,DEPT,PWD,LANG);
			String token = ConnectionManager.connectToClient(creds);
	
			if(token == null){
				String txt = '{"status":"error","message":"authentication failed"}';
				render(text:  txt, contentType: "text/json", encoding: "UTF-8");
				
			}else{
				String ExpDate = ConnectionManager.getExpDateFromToken(token);
				String txt = '{"status":"success","token":"'+token+'","expdate":"'+ExpDate+'"}';
				render(text:  txt, contentType: "text/json", encoding: "UTF-8");

			}
			
			//Connection conn = ConnectionManager.getConnectionFromToken(token);

		
		
		}
		

	}
	
}
