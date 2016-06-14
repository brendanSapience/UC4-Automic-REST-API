package automic.restful.server

import com.automic.DisplayFilters
import com.automic.actions.JobsGETActions
import com.automic.actions.JobsPOSTActions
import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.connection.ConnectionPoolItem;
import com.uc4.api.SearchResultItem
import com.uc4.communication.Connection
import com.uc4.communication.requests.SearchObject
import com.uc4.communication.requests.TemplateList
import com.automic.objects.CommonAERequests
import com.automic.utils.ActionClassUtils
import com.automic.utils.CommonJSONRequests;
import com.automic.utils.MiscUtils;

import groovy.json.JsonBuilder
import grails.util.Environment

class JobsController {

	/**
	 * @name help
	 * @purpose return a JSON structure containing the list of available operations for a given Object Type
	 * @note this method should be in all controllers
	 */
	
	def help = {
		// all operations and all versions available - no list to maintained.. its dynamically calculated :)
		ActionClassUtils utils = new ActionClassUtils(new JobsGETActions().metaClass.methods*.name.unique())
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
		String OPERATION = params.operation;
		
		//OPERATION = 'search';
		//println request.
		if(request.getHeader("Token")){TOKEN = request.getHeader("Token")};
		if(Environment.current == Environment.DEVELOPMENT){TOKEN = ConnectionManager.bypassAuth();}
		
		if(ConnectionManager.runTokenChecks(TOKEN)==null){
			com.uc4.communication.Connection conn = ConnectionManager.getConnectionFromToken(TOKEN);
			
			JsonBuilder myRes;
			// if not in Prod we are ok to show stacktrace
			if(Environment.current == Environment.DEVELOPMENT){
				String HTTPMETHOD = request.method
				ClassLoader classLoader = this.class.getClassLoader();
				String CLASSNAME = "com.automic.actions.Jobs"+HTTPMETHOD+"Actions"
				println "Debug: " + CLASSNAME
				Class aClass = classLoader.loadClass(CLASSNAME);
				//if(request.method.equals("POST")){myRes = com.automic.actions.JobsPOSTActions."${OPERATION}"(VERSION,params,conn,request);}
				if(request.method.equals("POST")){myRes = aClass."${OPERATION}"(VERSION,params,conn,request);}
				else{myRes = com.automic.actions.JobsGETActions."${OPERATION}"(VERSION,params,conn,request);}
			}else{
			// otherwise it needs to be caught
				try{
					println "DEBUG: Is Post: " + request.method
					if(request.method.equals("POST")){myRes = com.automic.actions.JobsPOSTActions."${OPERATION}"(VERSION,params,conn,request);}
					else{myRes = com.automic.actions.JobsGETActions."${OPERATION}"(VERSION,params,conn,request);}
				//	myRes = com.automic.actions.JobsActions."${OPERATION}"(VERSION,params,conn);
				}catch(MissingMethodException){
					myRes = new JsonBuilder([status: "error", message: "an error occured for operation "+OPERATION+" in version "+VERSION])
				}
			}
			render(text:  myRes, contentType: "text/json", encoding: "UTF-8")
		}else{render(text:  ConnectionManager.runTokenChecks(TOKEN), contentType: "text/json", encoding: "UTF-8")}
		
	}

	
}
