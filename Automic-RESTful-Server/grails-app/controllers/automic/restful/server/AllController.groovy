package automic.restful.server

import com.automic.DisplayFilters
import com.uc4.api.SearchResultItem
import com.uc4.communication.requests.SearchObject

import groovy.json.JsonBuilder

import com.automic.actions.get.AllGETActions;
import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.objects.CommonAERequests
import com.automic.utils.ActionClassUtils
import com.automic.utils.CommonJSONRequests;
import com.automic.utils.MiscUtils;

import grails.util.Environment

class AllController {
	
	/**
	 * @name help
	 * @purpose return a JSON structure containing the list of available operations for a given Object Type
	 * @note this method should be in all controllers
	 */
	
	def help = {
		// all operations and all versions available - no list to maintained.. its dynamically calculated :)
		ActionClassUtils utils = new ActionClassUtils(new AllGETActions().metaClass.methods*.name.unique())
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
		if(Environment.current == Environment.DEVELOPMENT){TOKEN = ConnectionManager.bypassAuth();}
	
		if(ConnectionManager.runTokenChecks(TOKEN)==null){
			com.uc4.communication.Connection conn = ConnectionManager.getConnectionFromToken(TOKEN);
			
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
				// if not in Prod we are ok to show stacktrace
				if(Environment.current == Environment.DEVELOPMENT){
					myRes = actionClass."${OPERATION}"(VERSION,params,conn,request);
				}else{
				// otherwise it needs to be caught
					try{
						if(request.method.equals("GET")){myRes = actionClass."${OPERATION}"(VERSION,params,conn,request);}
						//if(request.method.equals("POST")){myRes = com.automic.actions.AllPOSTActions."${OPERATION}"(VERSION,params,conn,request);}
						
					}catch(MissingMethodException){
						myRes = new JsonBuilder([status: "error", message: "an error occured for operation "+OPERATION+" in version "+VERSION])
					}
				}
				render(text:  myRes, contentType: "text/json", encoding: "UTF-8")
			}
		}else{render(text:  ConnectionManager.runTokenChecks(TOKEN), contentType: "text/json", encoding: "UTF-8")}
		
	}
}
