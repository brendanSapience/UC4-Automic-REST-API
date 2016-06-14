package com.automic.actions

import com.uc4.api.SearchResultItem
import com.uc4.communication.Connection;
import com.uc4.communication.requests.SearchObject
import groovy.json.JsonBuilder
import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests;
import com.automic.utils.MiscUtils;

class JobsGETActions {
	
	public static def search(String version, params,Connection conn){return "search${version}"(params,conn)}
	public static def delete(String version, params,Connection conn){return "delete${version}"(params,conn)}
	
	public static def searchv1(params,Connection conn) {
		
			def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name= < UC4RegEx > )'],
				'optional_parameters': ['search_usage (format: search_usage=Y)'],
				'optional_filters': [],
				'required_methods': [],
				'optional_methods': ['usage'],
				'developer_comment': 'Deprecated. Use api category "All" instead of "Jobs" and apply a filter on "JOBS" type instead.'
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
						req.unselectAllObjectTypes();
						req.setTypeJOBS(true);
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
	public static def searchv2(params,Connection conn) {
		
			def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name= < UC4RegEx > )'],
				'optional_parameters': ['search_usage (format: search_usage=Y)'],
				'optional_filters': [],
				'required_methods': [],
				'optional_methods': ['usage'],
				'developer_comment': 'Deprecated. Use api category "All" instead of "Jobs" and apply a filter on "JOBS" type instead.'
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
						req.unselectAllObjectTypes();
						req.setTypeJOBS(true);
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
	public static def deletev1(params,Connection conn) {
		
	}
}
