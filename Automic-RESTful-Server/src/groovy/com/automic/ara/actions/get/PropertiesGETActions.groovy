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

class PropertiesGETActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def getid(String version, String TOKEN, params,request, grailsattr){return "getid${version}"(TOKEN, params)}
	public static def show(String version, String TOKEN, params,request, grailsattr){return "show${version}"(TOKEN, params)}
	public static def set(String version, String TOKEN, params,request, grailsattr){return "set${version}"(TOKEN, params)}
	
	/**
	 * @purpose Set ARA Dynamic Property
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def setv1(TOKEN, params){
		def AllParamMap = [:]
		AllParamMap = [
			//name (format: name= < UC4RegEx > )
			'required_parameters': [
				'name (format: name=<String> (Name of Dynamic Property)',
				'objname (format: objname=<String> (ID or Name of the object of the dynamic property to get)',
				'value (format: value=<String> (Value of Dynamic Property to create)',
				'valuetype (format: valuetype=<String> (value type of the dynamic property to create. Options are: Static, Expression, Prompt)',
				'proptype (format: proptype=<String> (dynamic property type of the dynamic property to create. Options: Number, Protected, Short Text, Single Line Text, List)',
			],
			'optional_parameters': [
				'objtype (format: objtype=<String> (Typeof Object that holds the property (can be ommited if objname is given the object ID))',
				'namespace (format: namespace=<String> (namespace of the dynamic property)',
				'highlighted (format: highlighted (highlight dynamic property)',
				'failiftypediffers (format: failiftypediffers (dynamic property will fail if the type differs to an already existing instance)',
				'failifexists (format: failifexists (Fail if Property isnt found)',
			],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]

		String FILTERS = params.filters;
		String METHOD = params.method;
		
		String NAME = params.name; // Mandatory
		String OBJNAME = params.objname; // Mandatory
		String VALUE = params.value; // Mandatory
		String VALUETYPE = params.valuetype; // Mandatory
		String PROPTYPE = params.proptype; // Mandatory
		
		String OBJTYPE = params.objtype;
		String NAMESPACE = params.namespace;
		String HIGHLIGHTED = params.highlighted;
		String FAILIFDIFFERS = params.failifdiffers;
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
					JsonBuilder res = CommonARARequests.setProperty(NAME,OBJNAME,VALUE,VALUETYPE,PROPTYPE,OBJTYPE,NAMESPACE,HIGHLIGHTED,FAILIFDIFFERS,FAILIFEXISTS,ConnectionManager.getConnectionItemFromToken(TOKEN));

					return res;
				}else{
					 return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				 }
		}
	}
	
	/**
	 * @purpose Show ARA Dynamic Property
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def showv1(TOKEN, params){
		def AllParamMap = [:]
		AllParamMap = [
			//name (format: name= < UC4RegEx > )
			'required_parameters': [
				'name (format: name=<String> (Name of Dynamic Property)',
				'objname (format: objname=<String> (ID or Name of the object of the dynamic property to get)',
			],
			'optional_parameters': [
				'objtype (format: objtype=<String> (Typeof Object that holds the property (can be ommited if objname is given the object ID))',
				'namespace (format: namespace=<String> (namespace of the dynamic property)',
				'failifmissing (format: failifexists (Fail if Property isnt found)',
			],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]

		String FILTERS = params.filters;
		String METHOD = params.method;
		
		String NAME = params.name; // Mandatory
		String OBJNAME = params.objname; // Mandatory
		
		String OBJTYPE = params.objtype;
		String NAMESPACE = params.namespace;
		String FAILIFMISSING = params.failifmissing;
		
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
					JsonBuilder res = CommonARARequests.getProperty(NAME,OBJNAME,OBJTYPE,NAMESPACE,FAILIFMISSING,ConnectionManager.getConnectionItemFromToken(TOKEN));

					return res;
				}else{
				 	return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				 }
		}
	}
	
	/**
	 * @purpose Get ARA Property ID
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def getidv1(TOKEN, params){
		def AllParamMap = [:]
		AllParamMap = [
			//name (format: name= < UC4RegEx > )
			'required_parameters': [
				'objname (format: objname=<String> (Object Name)',
				'name (format: name=<String> (Dynamic Property Name)',
				
			],
			'optional_parameters': [
				'objtype (format: objtype=<String> (Object Type)',
				'namespace (format: namespace=<String> (Namespace Type)',
			],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]

		String FILTERS = params.filters;
		String METHOD = params.method;
		
		String NAME = params.name; // Mandatory
		String OBJNAME = params.objname; // Mandatory
		
		String OBJTYPE = params.objtype;
		String NAMESPACE = params.namespace;
		
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
					JsonBuilder res = CommonARARequests.getPropertyID(NAME,OBJNAME,OBJTYPE,NAMESPACE,ConnectionManager.getConnectionItemFromToken(TOKEN));

					return res;
				}else{
					 return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				 }
		}
	}
	
}
