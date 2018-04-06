package com.automic.ae.actions.get

import com.automic.DisplayFilters
import com.uc4.api.DateTime
import com.uc4.api.InvalidUC4NameException
import com.uc4.api.SearchResultItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter
import com.uc4.api.UC4HostName;
import com.uc4.api.UC4ObjectName
import com.uc4.api.UC4TimezoneName
import com.uc4.api.TaskFilter.TimeFrame
import com.uc4.api.systemoverview.AgentListItem
import com.uc4.communication.Connection
import com.uc4.communication.requests.ActivityList
import com.uc4.communication.requests.AdoptTask
import com.uc4.communication.requests.CancelTask
import com.uc4.communication.requests.DeactivateTask
import com.uc4.communication.requests.DeleteObject
import com.uc4.communication.requests.DisconnectHost
import com.uc4.communication.requests.ExecuteObject
import com.uc4.communication.requests.GenericStatistics
import com.uc4.communication.requests.GetChangeLog
import com.uc4.communication.requests.GetComments
import com.uc4.communication.requests.GetSessionTZ
import com.uc4.communication.requests.QuitTask
import com.uc4.communication.requests.RenewTransferKey
import com.uc4.communication.requests.Report
import com.uc4.communication.requests.RestartTask
import com.uc4.communication.requests.ResumeTask
import com.uc4.communication.requests.RollbackTask
import com.uc4.communication.requests.SearchObject
import com.uc4.communication.requests.StartHost
import com.uc4.communication.requests.SuspendTask
import com.uc4.communication.requests.TerminateHost
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

class AgentsGETActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def start(String version, params,Connection conn,request, grailsattr){return "start${version}"(params,conn)}
	public static def quit(String version, params,Connection conn,request, grailsattr){return "quit${version}"(params,conn)}
	public static def disconnect(String version, params,Connection conn,request, grailsattr){return "disconnect${version}"(params,conn)}
	public static def show(String version, params,Connection conn,request, grailsattr){return "show${version}"(params,conn)}
	public static def delete(String version, params,Connection conn,request, grailsattr){return "delete${version}"(params,conn)}
	public static def renewkey(String version, params,Connection conn,request, grailsattr){return "renewkey${version}"(params,conn)}
	
	/**
	 * @purpose renew Transfer Key for an Agent
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def renewkeyv1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name=<String> Agent Name)'],
				'optional_parameters': [],
				'optional_filters': [
				],
				'required_methods': [],
				'optional_methods': ['usage']
				]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		String NAMEASSTR = params.name;
		
		JsonBuilder json;
		
		// Helper Methods
		if(METHOD == "usage"){
			json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
			
			// check mandatory stuff here
			if(MiscUtils.checkParams(SupportedThings, params)){
				
				UC4HostName agentName
				
				try{
					agentName = new UC4HostName(NAMEASSTR);
				}catch(InvalidUC4NameException i){
					return CommonJSONRequests.renderErrorAsJSON("Invalid Agent Name: " + i.getMessage())
				}
				AgentListItem myItem = CommonAERequests.getAgentListItemByName(NAMEASSTR,conn);
				if(myItem==null){
					return CommonJSONRequests.renderErrorAsJSON("Agent Could not be found in Client 0.")
				}
				RenewTransferKey req = new RenewTransferKey(myItem);
				CommonAERequests.sendSyncRequest(conn, req, false);

				if(req.getMessageBox()!=null){
					return CommonJSONRequests.renderErrorAsJSON("Transfer Key could not be renewed: "+req.getMessageBox().getText())
				}
				
				return CommonJSONRequests.renderOKAsJSON("Transfer Key Renewal Sent.")
				
			}else{
					json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
					return json
			}
		
		}
	}
	/**
	 * @purpose delete an agent
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def deletev1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name=<String> Agent Name)'],
				'optional_parameters': [],
				'optional_filters': [
				],
				'required_methods': [],
				'optional_methods': ['usage']
				]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		String NAMEASSTR = params.name;
		
		JsonBuilder json;
		
		// Helper Methods
		if(METHOD == "usage"){
			json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
			
			// check mandatory stuff here
			if(MiscUtils.checkParams(SupportedThings, params)){
				
				UC4HostName agentName
				
				try{
					agentName = new UC4HostName(NAMEASSTR);
				}catch(InvalidUC4NameException i){
					return CommonJSONRequests.renderErrorAsJSON("Invalid Agent Name: " + i.getMessage())
				}
				
				DeleteObject req1 = new DeleteObject(agentName);		
				CommonAERequests.sendSyncRequest(conn, req1, false);
				
				if(req1.getMessageBox()!=null){
					return CommonJSONRequests.renderErrorAsJSON("Agent Could not be Deleted: "+req1.getMessageBox().getText())
				}
				
				return CommonJSONRequests.renderOKAsJSON("Agent Deleted.")
				
			}else{
					json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
					return json
			}
		
		}
	}
	
	
	/**
	 * @purpose start an agent
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def startv1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name=<String> Agent Name)'],
				'optional_parameters': [],
				'optional_filters': [
				],
				'required_methods': [],
				'optional_methods': ['usage']
				]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		String NAMEASSTR = params.name;
		
		JsonBuilder json;
		
		// Helper Methods
		if(METHOD == "usage"){
			json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
			
			// check mandatory stuff here
			if(MiscUtils.checkParams(SupportedThings, params)){
				
				UC4HostName agentName
				
				try{
					agentName = new UC4HostName(NAMEASSTR);
				}catch(InvalidUC4NameException i){
					return CommonJSONRequests.renderErrorAsJSON("Invalid Agent Name: " + i.getMessage())
				}
				
				
				StartHost req = new StartHost(agentName);
				
				CommonAERequests.sendSyncRequest(conn, req, false);
				
				return CommonJSONRequests.renderOKAsJSON("Agent Start Request Processed.")
				
			}else{
					json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
					return json
			}
		
		}
	}
	
	/**
	 * @purpose quit an agent
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def quitv1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name=<String> Agent Name)'],
				'optional_parameters': [],
				'optional_filters': [
				],
				'required_methods': [],
				'optional_methods': ['usage']
				]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		String NAMEASSTR = params.name;
		
		JsonBuilder json;
		
		// Helper Methods
		if(METHOD == "usage"){
			json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
			
			// check mandatory stuff here
			if(MiscUtils.checkParams(SupportedThings, params)){
				
				UC4HostName agentName
				
				try{
					agentName = new UC4HostName(NAMEASSTR);
				}catch(InvalidUC4NameException i){
					return CommonJSONRequests.renderErrorAsJSON("Invalid Agent Name: " + i.getMessage())
				}
				
				
				TerminateHost req = new TerminateHost(agentName);
				
				CommonAERequests.sendSyncRequest(conn, req, false);
				
				return CommonJSONRequests.renderOKAsJSON("Agent Termination Request Processed.")
				
			}else{
					json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
					return json
			}
		
		}
	}
	/**
	 * @purpose show agent
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def showv1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name=<String> Agent Name)'],
				'optional_parameters': [],
				'optional_filters': [
				],
				'required_methods': [],
				'optional_methods': ['usage']
				]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		String NAMEASSTR = params.name;
		
		JsonBuilder json;
		
		// Helper Methods
		if(METHOD == "usage"){
			json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
			
			// check mandatory stuff here
			if(MiscUtils.checkParams(SupportedThings, params)){
				
				AgentListItem item = CommonAERequests.getAgentListItemByName(NAMEASSTR, conn);
				if(item==null){
					return CommonJSONRequests.renderErrorAsJSON("Invalid Agent Name. Agent Not Found.")
				}
	
						return new JsonBuilder(
							[
								status: "success",
								count: 1,
								name: item.name,
								active: item.active,
								authenticated: item.authenticated,
								authorizations: item.authorizations,
								compression: item.compression,
								cp: item.cp,
								hardware: item.hardware,
								ip: item.ipAddress,
								linked: item.isLinked,
								jcl: item.jclVariant,
								keepalive: item.keepAlive,
								lastcheck: item.lastCheck.toString(),
								lastlogoff: item.lastLogoff.toString(),
								liccategory: item.licenseCategory,
								licclass: item.licenseClass,
								linked: item.linked,
								maxfiletransfer: item.maxFileTransferResources,
								maxjobresource: item.maxJobResources,
								mib: item.mib,
								netarea: item.netArea,
								clients: item.numberOfClients,
								port: item.port,
								processid: item.processID,
								roles: item.roles,
								service: item.service,
								software: item.software,
								softwareversion: item.softwareVersion,
								timediff: item.timeDifference,
								version: item.version,
									
							  ])
				
			}else{
					json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
					return json
			}
		
		}
	}
	/**
	 * @purpose disconnect an agent
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def disconnectv1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name=<String> Agent Name)'],
				'optional_parameters': [],
				'optional_filters': [
				],
				'required_methods': [],
				'optional_methods': ['usage']
				]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		String NAMEASSTR = params.name;
		
		JsonBuilder json;
		
		// Helper Methods
		if(METHOD == "usage"){
			json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
			
			// check mandatory stuff here
			if(MiscUtils.checkParams(SupportedThings, params)){
				
				AgentListItem item = CommonAERequests.getAgentListItemByName(NAMEASSTR, conn);
				if(item==null){
					return CommonJSONRequests.renderErrorAsJSON("Invalid Agent Name. Agent Not Found.")
				}
	
				DisconnectHost req = new DisconnectHost(item);
				
				CommonAERequests.sendSyncRequest(conn, req, false);
				
				return CommonJSONRequests.renderOKAsJSON("Agent Disconnect Request Processed.")
				
			}else{
					json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
					return json
			}
		
		}
	}
}
