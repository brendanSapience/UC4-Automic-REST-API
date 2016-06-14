package automic.restful.server

import com.automic.actions.get.AuthGETActions;
import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.utils.ActionClassUtils
import com.uc4.communication.Connection

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class AuthController {

	static String ConnectionSettingsFileName = "ConnectionConfig.json"
	
	/**
	 * @name help
	 * @purpose return a JSON structure containing the list of available operations for a given Object Type
	 * @note this method should be in all controllers
	 */
	
	def help = {
		// all operations and all versions available - no list to maintained.. its dynamically calculated :)
		ActionClassUtils utils = new ActionClassUtils(new AuthGETActions().metaClass.methods*.name.unique())
		render(text: utils.getOpsAndVersionsAsJSON(), contentType: "text/json", encoding: "UTF-8")
	}
	
	/**
	 * @name router
	 * @purpose checks authentication & token validity
	 * @purpose dynamically calls the appropriate method corresponding to the url param api category (all methods should be in *Actions classes)
	 * @note this method should be in all controllers
	 */
	
	def router = {
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String VERSION = params.version;
		String METHOD = params.method;
		String OPERATION = params.operation.toString().toLowerCase(); // makes sure the operation is lower case
		String OBJECT = params.object.toString().toLowerCase().capitalize(); //Makes sure we have camel case on the Object Name
		String HTTPMETHOD = request.method;
		
		if(request.getHeader("Token")){TOKEN = request.getHeader("Token")};
		
			boolean NeedToken = true;
			if(OPERATION.equalsIgnoreCase("help") || OPERATION.equalsIgnoreCase("login")){NeedToken=false;}
			
			if(ConnectionManager.runTokenChecks(TOKEN)==null || !NeedToken){
				com.uc4.communication.Connection conn;
				if(NeedToken){conn = ConnectionManager.getConnectionFromToken(TOKEN);}
				
				// go to Actions and trigger $OPERATION$VERSION(params, conn)
				JsonBuilder myRes;
				// Dynamically loading the Class based on Object name, and HTTP Method (GET, POST etc.)
				Class actionClass
				boolean ClassFound = true;
				try{
					actionClass = this.class.getClassLoader().loadClass("com.automic.actions."+HTTPMETHOD.toLowerCase()+"."+OBJECT+HTTPMETHOD+"Actions");
				}catch (ClassNotFoundException c){
					ClassFound = false;
					myRes = new JsonBuilder([status: "error", message: "Method "+HTTPMETHOD+" is not supported for Object: "+OBJECT + " and operation: " +OPERATION ])
					render(text:  myRes, contentType: "text/json", encoding: "UTF-8")
				}
				if(ClassFound){
				
					if(OPERATION.equalsIgnoreCase("login")){
						def ConnectonFile = grailsAttributes.getApplicationContext().getResource(ConnectionSettingsFileName).getFile()
						myRes = actionClass."${OPERATION}"(VERSION,params,ConnectonFile,request);
					}else{
						myRes = actionClass."${OPERATION}"(VERSION,params,conn,request);
					}
					
					//}catch(MissingMethodException){
					//	myRes = new JsonBuilder([status: "error", message: "version "+VERSION+" does not exist for operation: "+OPERATION])
					//}
					render(text:  myRes, contentType: "text/json", encoding: "UTF-8")
				}
			}else if(ConnectionManager.runTokenChecks(TOKEN)!=null && NeedToken){render(text:  ConnectionManager.runTokenChecks(TOKEN), contentType: "text/json", encoding: "UTF-8")}
	}
}
