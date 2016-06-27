package com.automic.ae.actions.get

import com.automic.DisplayFilters
import com.uc4.api.DateTime
import com.uc4.api.SearchResultItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter
import com.uc4.api.UC4ObjectName
import com.uc4.api.UC4TimezoneName
import com.uc4.api.TaskFilter.TimeFrame
import com.uc4.communication.Connection
import com.uc4.communication.requests.ActivityList
import com.uc4.communication.requests.AdoptTask
import com.uc4.communication.requests.CancelTask
import com.uc4.communication.requests.DeactivateTask
import com.uc4.communication.requests.ExecuteObject
import com.uc4.communication.requests.GenericStatistics
import com.uc4.communication.requests.GetChangeLog
import com.uc4.communication.requests.GetComments
import com.uc4.communication.requests.GetSessionTZ
import com.uc4.communication.requests.QuitTask
import com.uc4.communication.requests.Report
import com.uc4.communication.requests.RestartTask
import com.uc4.communication.requests.ResumeTask
import com.uc4.communication.requests.RollbackTask
import com.uc4.communication.requests.SearchObject
import com.uc4.communication.requests.SuspendTask
import com.uc4.communication.requests.UnblockJobPlanTask
import com.uc4.communication.requests.UnblockWorkflow
import com.uc4.communication.requests.XMLRequest

import groovy.json.JsonBuilder

import com.automic.connection.AECredentials
import com.automic.connection.ConnectionManager
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests
import com.automic.utils.MiscUtils
import com.uc4.communication.requests.GetComments.Comment

class ChangesGETActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def search(String version, params,Connection conn,request, grailsattr){return "search${version}"(params,conn)}

	
	/**
	 * @purpose search changes (audit trail) against filters
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def searchv1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': [],
				'optional_parameters': ['maxresults (format: maxresults=1000)'],
				'optional_filters': [
					'type (format: filters=[type:RESTART|CANCEL]) Available Types are: RESTART,CANCEL,DELETE,OBJ_MOD,CREATE,RENAME,MOVE,IMPORT,RESTART,RUN_MOD',
					'range (format: filters=[range:YYYYMMDDHHMM-YYYYMMDDHHMM], or filters=[range:LAST4DAYS] (DAYS can be substituted with: SECS, MINS, HOURS, DAYS, MONTHS, YEARS) ',
					'objname (format: filters=[objname:<RegEx>])',
					'username (format: filters=[username:<RegEx>])',
					'title (format: filters=[title:<RegEx>])',
					'firstname (format: filters=[firstname:<RegEx>])',
					'lastname (format: filters=[lastname:<RegEx>])'
				],
				'required_methods': [],
				'optional_methods': ['usage']
				]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		String MAXRES = params.maxresults;
		
		JsonBuilder json;
		
		// Helper Methods
		if(METHOD == "usage"){
			json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
			
			// check mandatory stuff here
			if(MiscUtils.checkParams(SupportedThings, params)){
				
				DisplayFilters dispFilters = new DisplayFilters(FILTERS);
				
				GetChangeLog req = new GetChangeLog();
				req.setMaxResultCount(0); // 0 to disable limit
				if(MAXRES!=null && MAXRES.isBigInteger()){req.setMaxResultCount(MAXRES.toBigInteger())}
				req.selectAllChangeTypes();
				req.selectAllObjects();
				
				CommonAERequests.sendSyncRequest(conn, req, false)
				
				Iterator<GetChangeLog.Entry> iterator = req.iterator()
				ArrayList<GetChangeLog.Entry> allentries = new ArrayList<GetChangeLog.Entry>()
				ArrayList<GetChangeLog.Entry> selectedentries = new ArrayList<GetChangeLog.Entry>()
				while(iterator.hasNext()){					
					GetChangeLog.Entry entry = iterator.next();
					allentries.add(entry);
				}

				if(dispFilters != null){

						for(int i=0;i<allentries.size();i++){
							GetChangeLog.Entry entry = allentries.get(i);
							boolean isSelected = true
							
							if(dispFilters.doesKeyExistInFilter("type")){
								String AllTypesSelected = dispFilters.getValueFromKey("type");
								String[] AllTypessArray = AllTypesSelected.split("\\|");
								if(!AllTypessArray.contains(entry.getType())){
									isSelected = false
								}
							}
							if(dispFilters.doesKeyExistInFilter("objname")){
								String filtervalue = dispFilters.getValueFromKey("objname");
								if(!entry.getObjectName().toString().matches(filtervalue)){
									isSelected = false
								}
							}
							if(dispFilters.doesKeyExistInFilter("username")){
								String filtervalue = dispFilters.getValueFromKey("username");
								if(!entry.getUserName().matches(filtervalue)){
									isSelected = false
								}
							}
							if(dispFilters.doesKeyExistInFilter("title")){
								String filtervalue = dispFilters.getValueFromKey("title");
								if(!entry.getTitle().matches(filtervalue)){
									isSelected = false
								}
							}
							if(dispFilters.doesKeyExistInFilter("firstname")){
								String filtervalue = dispFilters.getValueFromKey("firstname");
								if(!entry.getFirstName().matches(filtervalue)){
									isSelected = false
								}
							}
							if(dispFilters.doesKeyExistInFilter("lastname")){
								String filtervalue = dispFilters.getValueFromKey("lastname");
								if(!entry.getLastName().matches(filtervalue)){
									isSelected = false
								}
							}
							if(dispFilters.doesKeyExistInFilter("range")){
								String RawDate = dispFilters.getValueFromKey("range");
								DateTime[] RESULTS = MiscUtils.HandleDateFilter(RawDate);
								if(entry.getTimestamp().compareTo(RESULTS[0])==-1 || entry.getTimestamp().compareTo(RESULTS[1])==1){
									isSelected = false
								}
							}
							
							if(isSelected){selectedentries.add(entry)}
						}
				
				
				}else{
					selectedentries = allentries
				}
				
				json = CommonJSONRequests.getChangesAsJSON2(selectedentries);
				
			}
			
			return json
		}
		
	}
}
