package com.automic.ae.actions.get

import com.uc4.api.SearchResultItem
import com.uc4.api.UC4ObjectName
import com.uc4.api.VersionControlListItem
import com.uc4.api.objects.Job
import com.uc4.api.objects.UC4Object
import com.uc4.communication.Connection;
import com.uc4.communication.requests.SearchObject
import com.uc4.communication.requests.VersionControlList
import groovy.json.JsonBuilder
import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests;
import com.automic.utils.MiscUtils;
import com.automic.spec.JOBSSpecDisplay;

class JobsGETActions {
	
	public static def search(String version, params,Connection conn,request, grailsattr){return "search${version}"(params,conn)}
	public static def display(String version, params,Connection conn,request, grailsattr){return "display${version}"(params,conn)}
	
	
	public static def displayv1(params,Connection conn) {
		
			def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name= < String >)'],
				'optional_parameters': ['v (format: v=<integer>) -> display a particular version number for selected object.'],
				'optional_filters': [],
				'required_methods': [],
				'optional_methods': ['usage'],
				'developer_comment': ''
				]
			
			String FILTERS = params.filters;
			String TOKEN = params.token;
			String METHOD = params.method ?: ''
			String OBJVERSION = params.v;
			
			if(METHOD == "usage"){
				JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
				return json
			}else{
					// check mandatory stuff here
					boolean IsCurrentVersion = true;
					int VERSION = -1;
					if(MiscUtils.checkParams(SupportedThings, params)){
						if(OBJVERSION != null && !OBJVERSION.equals("")){
							IsCurrentVersion = false;
							VERSION = OBJVERSION.toInteger(); 
						}
						UC4Object object;
						if(IsCurrentVersion){object = CommonAERequests.openObject(conn,params.name, true);}
						else{
							// get that version and open it
							VersionControlList vcl = new VersionControlList(CommonAERequests.getUC4ObjectNameFromString(params.name,false));
							CommonAERequests.sendSyncRequest(conn,vcl,false);
							Iterator<VersionControlListItem> iter = vcl.iterator();
							boolean notFound = true;
							
							while(iter.hasNext() && notFound){
								VersionControlListItem vcli = iter.next();
								if(vcli.getVersionNumber() == VERSION){
									object = CommonAERequests.openObject(conn,vcli.getSavedName().getName(), true);
									notFound = false;
								}
							}
							if(notFound){return new JsonBuilder([status: "error", message: "version not found in object"])}
						}
						// check if object is null
						Job job = (Job)object;
					
						JsonBuilder json = JOBSSpecDisplay.ShowObject(conn,job); 
						return json

					}else{
						JsonBuilder json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
						return json
					}
			}
		}
	
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
			String METHOD = params.method ?: '' 
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
}
