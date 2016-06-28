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

class WorkflowsGETActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def deployapp(String version, String TOKEN, params,request, grailsattr){return "deployapp${version}"(TOKEN, params)}
	public static def deploygen(String version, String TOKEN, params,request, grailsattr){return "deploygen${version}"(TOKEN, params)}
	
	/**
	 * @purpose Start Application Workflow
	 * @return JsonBuilder object
	 * @version v1
	 */
	
//	"--url",ARAURL,"--username",USERSTRING, "--password",PWD,
//	"--workflowname", WFNAME,
//	"--application",APPNAME,
//	"--package",PCKNAME,
//	"--profile",PROFILE,
//	"--skipifinstalled","NO",   //optional YES or NO
	public static def deployappv1(TOKEN, params){
		def AllParamMap = [:]
		AllParamMap = [
			//name (format: name= < UC4RegEx > )
			'required_parameters': [
				'wfname (format: wfname=<String> (Workflow Name)',
				'appname (format: appname=<String> (Application Name)',
				'pckname (format: pckname=<String> (Package Name)',
				'profile (format: profile=<String> (Profile to use for deployment)',
				'skip (format: skip=<YES|NO> (Skip or Force if already installed)'
				],
			
			'optional_parameters': [],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]

		String FILTERS = params.filters;
		String METHOD = params.method;
		
		String WFNAME = params.wfname;
		String APPNAME = params.appname;
		String PCKNAME = params.pckname;
		String PROFILE = params.profile;
		String SKIP = params.skip;
		
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
				if(MiscUtils.checkParams(AllParamMap, params)){
					ConnectionManager.getConnectionItemFromToken(TOKEN)
					JsonBuilder res = CommonARARequests.executeApplicationWorkflow(WFNAME,APPNAME,PCKNAME,PROFILE,SKIP,ConnectionManager.getConnectionItemFromToken(TOKEN));
					return res;
				}else{
					 return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				 }
		}
	}
	public static def deploygenv1(TOKEN, params){
		def AllParamMap = [:]
		AllParamMap = [
			//name (format: name= < UC4RegEx > )
			'required_parameters': [
				'wfname (format: wfname=<String> (Workflow Name)'
				],
			
			'optional_parameters': [],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]

		String FILTERS = params.filters;
		String METHOD = params.method;
		
		String WFNAME = params.wfname;
		
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
				if(MiscUtils.checkParams(AllParamMap, params)){
					ConnectionManager.getConnectionItemFromToken(TOKEN)
					JsonBuilder res = CommonARARequests.executeGeneralWorkflow(WFNAME,ConnectionManager.getConnectionItemFromToken(TOKEN));
					return res;
				}else{
					 return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				 }
		}
	}
}
