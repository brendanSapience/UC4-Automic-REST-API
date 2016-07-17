package com.automic.ae.actions.get

import com.automic.DisplayFilters
import com.uc4.api.DateTime
import com.uc4.api.SearchResultItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter
import com.uc4.api.UC4ObjectName
import com.uc4.api.UC4TimezoneName
import com.uc4.api.UC4UserName
import com.uc4.api.VersionControlListItem
import com.uc4.api.TaskFilter.TimeFrame
import com.uc4.api.objects.UC4Object
import com.uc4.api.systemoverview.UserListItem
import com.uc4.communication.Connection
import com.uc4.communication.requests.ActivityList
import com.uc4.communication.requests.AdoptTask
import com.uc4.communication.requests.CalendarList
import com.uc4.communication.requests.CancelTask
import com.uc4.communication.requests.CheckAuthorizations
import com.uc4.communication.requests.CheckUserPrivileges
import com.uc4.communication.requests.DeactivateTask
import com.uc4.communication.requests.DisconnectUser
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
import com.uc4.communication.requests.UserList
import com.uc4.communication.requests.VersionControlList
import com.uc4.communication.requests.XMLRequest
import com.automic.spec.CALESpecDisplay
import groovy.json.JsonBuilder

import com.automic.connection.AECredentials
import com.automic.connection.ConnectionManager
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests
import com.automic.utils.MiscUtils
import com.uc4.communication.requests.CalendarList.CalendarListItem
import com.uc4.communication.requests.GetComments.Comment

class CalendarsGETActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def list(String version, params,Connection conn,request, grailsattr){return "list${version}"(params,conn)}
	public static def display(String version, params,Connection conn,request, grailsattr){return "display${version}"(params,conn)}

	/**
	 * @purpose show a calendar
	 * @return JsonBuilder object
	 * @version v1
	 */
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
						com.uc4.api.objects.Calendar obj = (com.uc4.api.objects.Calendar)object;
					
						JsonBuilder json = CALESpecDisplay.ShowObject(conn,obj);
						return json

					}else{
						JsonBuilder json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
						return json
					}
			}
		}
	
	/**
	 * @purpose list all existing calendars
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def listv1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': [],
				'optional_parameters': [],
				'optional_filters': [
				],
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
				CalendarList req = new CalendarList();
				CommonAERequests.sendSyncRequest(conn, req, false)
				ArrayList<CalendarListItem> reqList = req.iterator().toList();
				return new JsonBuilder(
					[
						status: "success",
						count: reqList.size(),
						data: reqList.collect {[
							name:it.getName(),
							id:it.id,
							]}
					  ]
				)
			}else{
				json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
				return json
			}
		}
		
	}
}
