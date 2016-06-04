package automic.restful.server

import com.automic.AECredentials
import com.automic.DisplayFilters
import com.automic.ConnectionManager
import com.uc4.api.SearchResultItem
import com.uc4.communication.requests.SearchObject
import com.automic.MiscUtils
import groovy.json.JsonBuilder
import com.automic.objects.CommonAERequests
import com.automic.CommonJSONRequests

class AllController {
	
	def index() { }
	
	String[] SupportedOperations=['search'];
	
	def help = {
		JsonBuilder json = CommonJSONRequests.getStringListAsJSONFormat("operation",SupportedOperations);
		render(text: json, contentType: "text/json", encoding: "UTF-8")
	}
	def searchv1 = {
	
		def SupportedThings = [:]
		SupportedThings = [
			'required_parameters': ['name (format: name= < UC4RegEx > )'],
			'optional_parameters': ['search_usage (format: search_usage=Y)'],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': []
			]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			render(text: json, contentType: "text/json", encoding: "UTF-8")
		}else{
			if(request.getHeader("Token")){TOKEN = request.getHeader("Token")};
			
			//Temp For Tests
			if(TOKEN == "DEV"){TOKEN = ConnectionManager.bypassAuth();}
			
			if(ConnectionManager.runTokenChecks(TOKEN)){
				com.uc4.communication.Connection conn = ConnectionManager.getConnectionFromToken(TOKEN);
				
				// check mandatory stuff here
				if(MiscUtils.checkParams(SupportedThings, params)){
					
					//DisplayFilters dispFilters = new DisplayFilters(FILTERS);
					
					SearchObject req = new SearchObject();
					req.selectAllObjectTypes();
					if(params.search_usage.equals('Y')){req.setSearchUseOfObjects(true);}
					
					List<SearchResultItem> JobList = CommonAERequests.GenericSearchObjects(conn, params.name, req);
					
					JsonBuilder json = CommonJSONRequests.getResultListAsJSONFormat(JobList);
					render(text:  json, contentType: "text/json", encoding: "UTF-8")
				}else{
					String json =  '{"status":"error","message":"missing mandatory parameters"}'
					render(text:  json, contentType: "text/json", encoding: "UTF-8")
				}
			}
		}
	}
}
