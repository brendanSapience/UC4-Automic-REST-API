package automic.restful.server

import com.automic.DisplayFilters
import com.uc4.api.SearchResultItem
import com.uc4.communication.requests.SearchObject

import groovy.json.JsonBuilder

import com.automic.ae.actions.get.AllGETActions;
import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.objects.CommonAERequests
import com.automic.utils.ActionClassUtils
import com.automic.utils.CommonJSONRequests;
import com.automic.utils.MiscUtils;

import grails.util.Environment

class AllController {
	
	Class actionClass
	boolean ClassFound = true;
	String RootPackage = "com.automic.ae.actions.";
	/**
	 * @name help
	 * @purpose return a JSON structure containing the list of available operations for a given Object Type
	 * @note this method should be in all controllers
	 */
	
	def help = {
		// all operations and all versions available - no list to maintained.. its dynamically calculated :)
		actionClass = this.class.getClassLoader().loadClass(RootPackage+request.method.toLowerCase()+"."+params.object.toString().toLowerCase().capitalize()+request.method+"Actions");
		ActionClassUtils utils = new ActionClassUtils(actionClass.metaClass.methods*.name.unique(),request.method)
		render(text: utils.getOpsAndVersionsAsJSON2(), contentType: "text/json", encoding: "UTF-8")
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
			try{
				actionClass = this.class.getClassLoader().loadClass(RootPackage+HTTPMETHOD.toLowerCase()+"."+OBJECT+HTTPMETHOD+"Actions");
			}catch (ClassNotFoundException c){
				ClassFound = false;
				myRes = new JsonBuilder([status: "error", message: "Method "+HTTPMETHOD+" is not supported for Object: "+OBJECT + " and operation: " +OPERATION ])
				render(text:  myRes, contentType: "text/json", encoding: "UTF-8")
			}
			if(ClassFound){
				// if not in Prod we are ok to show stacktrace
				if(false){ //Environment.current == Environment.DEVELOPMENT){
					
					if(OPERATION.equals("export") && params.commit != null && params.commit.toUpperCase().equals("Y")){
						// if commit.. return is file
							File file = actionClass."${OPERATION}"(VERSION,params,conn,request,grailsAttributes);
							//println "debug:" + file.getAbsolutePath()
							if(file != null){
								response.setHeader "Content-disposition", "attachment; filename=${file.name}"
								response.contentType = 'text/xml'
								response.outputStream << file.text
							}else{
								myRes = CommonJSONRequests.renderErrorAsJSON("Unspecified Error.  is the JWP active?")
								render(text:  myRes, contentType: "text/json", encoding: "UTF-8")
							}

						// else, return is json
					}else{
		
						myRes = actionClass."${OPERATION}"(VERSION,params,conn,request,grailsAttributes);
						render(text:  myRes, contentType: "text/json", encoding: "UTF-8")
					}
				}else{
				// otherwise it needs to be caught
					try{
						// if we are doing an export WITH commit set to Y, then we need to serve a file instead of a JSON response
						if(OPERATION.equals("export") && params.commit != null && params.commit.toUpperCase().equals("Y")){
							// if commit.. return is file
								File file = actionClass."${OPERATION}"(VERSION,params,conn,request,grailsAttributes);
								//println "debug:" + file.getAbsolutePath()
								if(file != null){
									response.setHeader "Content-disposition", "attachment; filename=${file.name}"
									response.contentType = 'text/xml'
									response.outputStream << file.text
								}else{
									myRes = CommonJSONRequests.renderErrorAsJSON("Unspecified Error.  is the JWP active?")
									render(text:  myRes, contentType: "text/json", encoding: "UTF-8")
								}
						// else, return is json as usual
						}else{
							myRes = actionClass."${OPERATION}"(VERSION,params,conn,request,grailsAttributes);
							render(text:  myRes, contentType: "text/json", encoding: "UTF-8")
						}
					
					}catch(MissingMethodException m){
						myRes = new JsonBuilder([status: "error", message: "an error occured for operation "+OPERATION+" in version "+VERSION, details: m.getMessage()])
						render(text:  myRes, contentType: "text/json", encoding: "UTF-8")
					}
				}
				//println "Debug"+ myRes.getClass()
				//render(text:  myRes, contentType: "text/xml", encoding: "UTF-8")

			}
		}else{render(text:  ConnectionManager.runTokenChecks(TOKEN), contentType: "text/json", encoding: "UTF-8")}
		
	}
}
