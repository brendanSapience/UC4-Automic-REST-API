package com.automic.ara.actions.get

import com.automic.DisplayFilters
import com.uc4.api.DateTime
import com.uc4.api.SearchResultItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter
import com.uc4.api.UC4ObjectName
import com.uc4.api.UC4TimezoneName
import com.uc4.api.TaskFilter.TimeFrame
import com.uc4.ara.feature.rm.CreateDeployPackage
//import com.uc4.ara.feature.rm.*
import com.uc4.communication.Connection
import com.uc4.communication.requests.ActivityList
import com.uc4.communication.requests.AdoptTask
import com.uc4.communication.requests.CancelTask
import com.uc4.communication.requests.DeactivateTask
import com.uc4.communication.requests.ExecuteObject
import com.uc4.communication.requests.GenericStatistics
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
import com.automic.connection.ConnectionPoolItem;
import com.automic.objects.CommonAERequests
import com.automic.objects.CommonARARequests
import com.automic.utils.CommonJSONRequests
import com.automic.utils.MiscUtils
import com.uc4.communication.requests.GetComments.Comment

class ProfilesGETActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def create(String version, String TOKEN, params,request, grailsattr){return "create${version}"(TOKEN, params)}
	public static def getid(String version, String TOKEN, params,request, grailsattr){return "getid${version}"(TOKEN, params)}

	/**
	 * @purpose Archive ARA Entities
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def getidv1(TOKEN, params){
		def AllParamMap = [:]
		AllParamMap = [
			//name (format: name= < UC4RegEx > )
			'required_parameters': [
				'name (format: name=<String> (Profile Name)',
				'app (format: app=<String> (Application Name)',
			],
			'optional_parameters': [

			],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]

		String FILTERS = params.filters;
		String METHOD = params.method;
		
		String NAME = params.name; // Mandatory
		String APP = params.app; // Mandatory
		
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
				if(MiscUtils.checkParams(AllParamMap, params)){
		//archiveEntities(String ENTNAME,String ENTOWNER, String ENTFOLDER,String ENTTYPE, String CUSTOMENTTYPE,
		//	String STARTDATE,String ENDDATE,String CONDITIONS,ConnectionPoolItem item)
					ConnectionManager.getConnectionItemFromToken(TOKEN)
					JsonBuilder res = CommonARARequests.getProfileID(NAME,APP,ConnectionManager.getConnectionItemFromToken(TOKEN));

					return res;
				}else{
					 return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				 }
		}
	}
	
	/**
	 * @purpose Create ARA Profile
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def createv1(TOKEN, params){
		def AllParamMap = [:]
		AllParamMap = [
			//name (format: name= < UC4RegEx > )
			'required_parameters': [
				'name (format: name=<String> (Target Name)',
				'folder (format: folder=<String> (Folder)',
				'owner (format: owner=<String> (Owner)',
				'env (format: env=<String> (Environment Name or ID)',
				'app (format: app=<String> (Application Name or ID)',
			],
			'optional_parameters': [
				
				'login (format: login=<String> (Login Name)',
				'failifexists (format: failifexists (Fail if Target Exists, otherwise Update Target)',
			],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]

		String FILTERS = params.filters;
		String METHOD = params.method;
		
		String NAME = params.name; // Mandatory
		String ENV = params.env; // Mandatory
		String APP = params.app; // Mandatory
		String OWNER = params.owner; // Mandatory
		String FOLDER = params.folder; // Mandatory
		
		String LOGIN = params.login;
		String FAILIFEXISTS = params.failifexists;
		
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
				if(MiscUtils.checkParams(AllParamMap, params)){
		//archiveEntities(String ENTNAME,String ENTOWNER, String ENTFOLDER,String ENTTYPE, String CUSTOMENTTYPE,
		//	String STARTDATE,String ENDDATE,String CONDITIONS,ConnectionPoolItem item)
					ConnectionManager.getConnectionItemFromToken(TOKEN)
					JsonBuilder res = CommonARARequests.createProfile(NAME,ENV,APP,OWNER,FOLDER,LOGIN,FAILIFEXISTS,ConnectionManager.getConnectionItemFromToken(TOKEN));

					return res;
				}else{
				 	return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				 }
		}
	}
	
}
