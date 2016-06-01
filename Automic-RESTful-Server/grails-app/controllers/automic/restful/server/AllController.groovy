package automic.restful.server

import com.automic.AECredentials
import com.automic.DisplayFilters
import com.automic.ConnectionManager
import com.uc4.api.SearchResultItem
import com.uc4.communication.requests.SearchObject
import groovy.json.JsonBuilder
import com.automic.objects.CommonAERequests
import com.automic.CommonJSONRequests

class AllController {
	
	def index() { }
	
	def search = {
	
		String PRODUCT = params.product;
		String APIVERSION = params.version;
		String TOKEN = params.token;
		String FILTERS = params.filters;
		
		//Temp For Tests
		if(TOKEN == "DEV"){TOKEN = ConnectionManager.bypassAuth();}
		
		if(ConnectionManager.runTokenChecks(TOKEN)){
			com.uc4.communication.Connection conn = ConnectionManager.getConnectionFromToken(TOKEN);
 
			DisplayFilters dispFilters = new DisplayFilters(FILTERS);
			
			SearchObject req = new SearchObject();
			//req.unselectAllObjectTypes();
			req.selectAllObjectTypes();
			
			List<SearchResultItem> JobList = CommonAERequests.GenericSearchObjects(conn, dispFilters.getValueFromKey("name"), req);
			
			JsonBuilder json = CommonJSONRequests.getResultListAsJSONFormat(JobList);
			
			render(text:  json, contentType: "text/json", encoding: "UTF-8")
			
		}

	}

}