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
import com.uc4.communication.requests.ActiveNotifications
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
import com.uc4.communication.requests.ActiveNotifications.Entry
import com.uc4.communication.requests.GetComments.Comment

class NotificationsGETActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def show(String version, params,Connection conn,request, grailsattr){return "show${version}"(params,conn)}
	//public static def delete(String version, params,Connection conn,request, grailsattr){return "delete${version}"(params,conn)}
	
	
	/**
	 * @purpose Show active notifications
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def showv1(params,Connection conn){
	
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
				
				ActiveNotifications req = new ActiveNotifications();
				
				CommonAERequests.sendSyncRequest(conn, req, false)
				
				ArrayList<com.uc4.communication.requests.ActiveNotifications.Entry> reqList = req.iterator().toList()
				
				return new JsonBuilder(
					[
						status: "success",
						count: reqList.size(),
						data: reqList.collect {[
							name:it.name,
							comments:it.comment,
							escalationtime:it.escalation.toString(),
							message:it.message,
							priority:it.priority,
							runid:it.runID,
							starttime:it.startTime.toString(),
							statuscode:it.statusCode,
							status:it.statusText,
							type:it.type,
							]}
					  ]
				)
			}else{
					 return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				 }
			}
		
	}
}
