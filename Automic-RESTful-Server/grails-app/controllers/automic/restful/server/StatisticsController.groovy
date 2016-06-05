package automic.restful.server


import java.util.Iterator;

import com.automic.DisplayFilters
import com.uc4.api.DateTime
import com.uc4.api.SearchResultItem
import com.uc4.api.StatisticSearchItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter;
import com.uc4.communication.requests.ActivityList
import com.uc4.communication.requests.GenericStatistics;
import com.uc4.communication.requests.RestartTask
import com.uc4.communication.requests.SearchObject
import com.uc4.communication.requests.UnblockJobPlanTask
import com.uc4.communication.requests.UnblockWorkflow
import com.uc4.communication.requests.XMLRequest

import groovy.json.JsonBuilder

import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests;
import com.automic.utils.MiscUtils;

class StatisticsController {
	
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
			
			JsonBuilder myRes = com.automic.actions.StatisticsActions."${OPERATION}"(VERSION,params,conn);
			render(text:  myRes, contentType: "text/json", encoding: "UTF-8")
		}
		
	}

	

}