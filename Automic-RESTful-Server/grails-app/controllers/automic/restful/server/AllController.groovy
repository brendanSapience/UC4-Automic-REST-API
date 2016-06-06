package automic.restful.server

import com.automic.DisplayFilters
import com.uc4.api.SearchResultItem
import com.uc4.communication.requests.SearchObject

import groovy.json.JsonBuilder

import com.automic.actions.AllActions
import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.objects.CommonAERequests
import com.automic.utils.ActionClassUtils
import com.automic.utils.CommonJSONRequests;
import com.automic.utils.MiscUtils;

class AllController {
	
	def index() { }
	
	def help = {
		// all operations and all versions available - no list to maintained.. its dynamically calculated :)
		ActionClassUtils utils = new ActionClassUtils(new AllActions().metaClass.methods*.name.unique())
		render(text: utils.getOpsAndVersionsAsJSON(), contentType: "text/json", encoding: "UTF-8")
	}
	
	def router = {
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String VERSION = params.version;
		String METHOD = params.method;
		String OPERATION = params.operation;
		
		//OPERATION = 'search';
		
		if(request.getHeader("Token")){TOKEN = request.getHeader("Token")};
		if(TOKEN == "DEV"){TOKEN = ConnectionManager.bypassAuth();}
		
		if(ConnectionManager.runTokenChecks(TOKEN)){
			com.uc4.communication.Connection conn = ConnectionManager.getConnectionFromToken(TOKEN);
			
			// go to JobsActions and trigger $OPERATION$VERSION(params, conn)
			JsonBuilder myRes;
			try{
				myRes = com.automic.actions.AllActions."${OPERATION}"(VERSION,params,conn);
			}catch(MissingMethodException){
				myRes = new JsonBuilder([status: "error", message: "version "+VERSION+" does not exist for operation: "+OPERATION])
			}
		}
		
	}
}
