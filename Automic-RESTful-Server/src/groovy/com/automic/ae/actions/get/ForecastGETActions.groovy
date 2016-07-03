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
import com.uc4.communication.requests.CalculateForecast
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

class ForecastGETActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	//public static def calculate(String version, params,Connection conn,request, grailsattr){return "calculate${version}"(params,conn)}
	//public static def delete(String version, params,Connection conn,request, grailsattr){return "delete${version}"(params,conn)}
	
	
	/**
	 * @purpose Calculate a forecast for an object or task
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def calculatev1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': [],
				'optional_parameters': [],
				'optional_filters': [],
				'required_methods': [],
				'optional_methods': ['usage']
				]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		
		JsonBuilder json;
		
		// Helper Methods
		if(METHOD == "usage"){
			json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
			
			// check mandatory stuff here
			if(MiscUtils.checkParams(SupportedThings, params)){
				
				CalculateForecast req = new CalculateForecast();
				
				
				CommonAERequests.sendSyncRequest(conn, req, false)
				
			}
			
			return json
		}
		
	}
}
