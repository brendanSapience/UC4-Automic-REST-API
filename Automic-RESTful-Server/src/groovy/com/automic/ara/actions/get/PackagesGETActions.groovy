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
import com.automic.objects.CommonAERequests
import com.automic.objects.CommonARARequests
import com.automic.utils.CommonJSONRequests
import com.automic.utils.MiscUtils
import com.uc4.communication.requests.GetComments.Comment

class PackagesGETActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def create(String version, String TOKEN, params,request, grailsattr){return "create${version}"(TOKEN, params)}
	public static def chgstate(String version, String TOKEN, params,request, grailsattr){return "chgstate${version}"(TOKEN, params)}
	public static def getstate(String version, String TOKEN, params,request, grailsattr){return "getstate${version}"(TOKEN, params)}
	//public static def startappwf(String version, String TOKEN, params,request, grailsattr){return "startappwf${version}"(TOKEN, params)}
	
	/**
	 * @purpose create an ARA Package
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def createv1(TOKEN, params){
		def AllParamMap = [:]
		AllParamMap = [
			//name (format: name= < UC4RegEx > )
			'required_parameters': [
				'pck (format: pck=<String> (Package Name)',
				'folder (format: folder=<String> (Folder Name)',
				'type (format: type=<String> (Package Type)',
				'app (format: app=<String> (Application Name)'],
			'optional_parameters': [],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]

		String FILTERS = params.filters;
		String METHOD = params.method;	
		
		String PCKNAME = params.pck;
		String FOLDER = params.folder;
		String TYPE = params.type;
		String APPNAME = params.app;
			
		
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
				if(MiscUtils.checkParams(AllParamMap, params)){
		
					ConnectionManager.getConnectionItemFromToken(TOKEN)
					JsonBuilder res = CommonARARequests.createDeployPackage(PCKNAME,FOLDER,TYPE,APPNAME,ConnectionManager.getConnectionItemFromToken(TOKEN));

					return res;
				}else{
				 	return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				 }
		}
	}
	
	/**
	 * @purpose change an ARA package state
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def chgstatev1(TOKEN, params){
		def AllParamMap = [:]
		AllParamMap = [
			//name (format: name= < UC4RegEx > )
			'required_parameters': [
				'pck (format: pck=<String> (Package Name)',
				'newstate (format: newstate=<String> (Target State for Package)',
				'currentstate (format: currentstate=<String> (Current Package State)',
				'notmatching (format: notmatching=<SKIP|FAIL> (What to do if operation fails)'],
			'optional_parameters': [],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]

		String FILTERS = params.filters;
		String METHOD = params.method;
		
		String PCKNAME = params.pck;
		String NEWSTATE = params.newstate;
		String CURRENTSTATE = params.currentstate;
		String NOTMATCHING = params.notmatching;
			
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
				if(MiscUtils.checkParams(AllParamMap, params)){
					ConnectionManager.getConnectionItemFromToken(TOKEN)
					JsonBuilder res = CommonARARequests.setPackageState(PCKNAME,NEWSTATE,CURRENTSTATE,NOTMATCHING,ConnectionManager.getConnectionItemFromToken(TOKEN));
					return res;
				}else{
					 return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				 }
		}
	}
	/**
	 * @purpose change an ARA package state
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def getstatev1(TOKEN, params){
		def AllParamMap = [:]
		AllParamMap = [
			//name (format: name= < UC4RegEx > )
			'required_parameters': [
				'pck (format: pck=<String> (Package Name or ID)'
				],
			'optional_parameters': [],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]

		String FILTERS = params.filters;
		String METHOD = params.method;
		String PCKNAME = params.pck;
			
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
				if(MiscUtils.checkParams(AllParamMap, params)){
					ConnectionManager.getConnectionItemFromToken(TOKEN) 
					JsonBuilder res = CommonARARequests.getPackageState(PCKNAME,ConnectionManager.getConnectionItemFromToken(TOKEN));
					return res;
				}else{
					 return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				 }
		}
	}
}
