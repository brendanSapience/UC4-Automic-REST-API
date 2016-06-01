package automic.restful.server

import com.automic.AECredentials
import com.automic.DisplayFilters
import com.automic.ConnectionManager
import com.uc4.api.SearchResultItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter;
import com.uc4.communication.requests.ActivityList
import com.uc4.communication.requests.SearchObject

import groovy.json.JsonBuilder

import com.automic.objects.CommonAERequests
import com.automic.CommonJSONRequests

class ActivitiesController {
	
	def index() { }
	
	String[] SupportedOperations=['search'];
	
	def help = {
		JsonBuilder json = CommonJSONRequests.getStringListAsJSONFormat("operation",SupportedOperations);
		render(text: json, contentType: "text/json", encoding: "UTF-8")
	}
	
	def search = {
	
		String[] SupportedFilters=['status'];
		
		String PRODUCT = params.product;
		String APIVERSION = params.version;
		String TOKEN = params.token;
		String METHOD = params.method;
		String FILTERS = params.filters;
		
		//Temp For Tests
		if(TOKEN == "DEV"){TOKEN = ConnectionManager.bypassAuth();}
		
		// Helper Methods
		if(METHOD == "showfilters"){
			JsonBuilder json = CommonJSONRequests.getStringListAsJSONFormat("name",SupportedFilters);
			render(text: json, contentType: "text/json", encoding: "UTF-8")
		}
		
		if(ConnectionManager.runTokenChecks(TOKEN)){
			com.uc4.communication.Connection conn = ConnectionManager.getConnectionFromToken(TOKEN);
			TaskFilter taskFilter = new TaskFilter();
			
			DisplayFilters dispFilters = new DisplayFilters(FILTERS);
			
			// !!! status MUST following a very strict structure: comma seperated return codes OR range: "1850,1851" or "1800-1899"
			if(dispFilters.doesKeyExistInFilter("status")){taskFilter.setStatus(dispFilters.getValueFromKey("status"));}
				

			List<Task> TaskList = CommonAERequests.getActivityWindowContent(conn,taskFilter);
			
			JsonBuilder json = CommonJSONRequests.getActivityListAsJSONFormat(TaskList); 
			
			render(text:  json, contentType: "text/json", encoding: "UTF-8")
			
		}

	}

}
