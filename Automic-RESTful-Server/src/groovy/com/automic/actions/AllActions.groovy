package com.automic.actions

import com.uc4.communication.Connection;
import com.uc4.api.SearchResultItem
import com.uc4.communication.Connection;
import com.uc4.communication.requests.SearchObject
import groovy.json.JsonBuilder
import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests;
import com.automic.utils.MiscUtils;

class AllActions {
	
	String[] SearchAvailableVersions = ['v1']
	public static def search(String version, params,Connection conn){return "search${version}"(params,conn)}
	
	private static def searchv1(params,Connection conn) {
		
			def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name= < UC4RegEx > )'],
				'optional_parameters': ['search_usage (format: search_usage=Y)'],
				'optional_filters': [],
				'required_methods': [],
				'optional_methods': ['usage']
				]
			
			String FILTERS = params.filters;
			String TOKEN = params.token;
			String METHOD = params.method;
			String SEARCHUSAGE = params.search_usage;
			
			if(METHOD == "usage"){
				JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
				return json
			}else{
					// check mandatory stuff here
					if(MiscUtils.checkParams(SupportedThings, params)){
						
						//DisplayFilters dispFilters = new DisplayFilters(FILTERS);
						
						SearchObject req = new SearchObject();
						req.selectAllObjectTypes();
						
						if(SEARCHUSAGE!=null && SEARCHUSAGE.toUpperCase() =~/Y|YES|OK|O/){req.setSearchUseOfObjects(true);}
						
						List<SearchResultItem> JobList = CommonAERequests.GenericSearchObjects(conn, params.name, req);
						
						JsonBuilder json = CommonJSONRequests.getResultListAsJSONFormat(JobList);
						return json
						
					}else{
						JsonBuilder json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
						
						return json
					}
				
			}
		}
}
