package automic.restful.server

import com.automic.DisplayFilters
import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.connection.ConnectionPoolItem;
import com.uc4.api.SearchResultItem
import com.uc4.communication.Connection
import com.uc4.communication.requests.SearchObject
import com.uc4.communication.requests.TemplateList
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests;
import com.automic.utils.MiscUtils;

import groovy.json.JsonBuilder

class JobsController {

    def index() { }
	
	String[] SupportedOperations=['search'];
	
	def help = {
		JsonBuilder json = CommonJSONRequests.getStringListAsJSONFormat("operation",SupportedOperations);
		render(text: json, contentType: "text/json", encoding: "UTF-8")
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
			
			JsonBuilder myRes = com.automic.actions.JobsActions."${OPERATION}"(VERSION,params,conn);
			render(text:  myRes, contentType: "text/json", encoding: "UTF-8")
		}
		
	}

	
}
