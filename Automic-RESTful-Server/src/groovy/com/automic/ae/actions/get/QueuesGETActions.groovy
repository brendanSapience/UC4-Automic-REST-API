package com.automic.ae.actions.get

import java.util.List;
import com.uc4.api.QueueStatus;
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
import com.uc4.api.objects.UC4Object
import com.uc4.api.systemoverview.AgentListItem
import com.uc4.api.systemoverview.QueueListItem
import com.uc4.communication.Connection
import com.uc4.communication.requests.ActivityList
import com.uc4.communication.requests.AdoptTask
import com.uc4.communication.requests.CancelTask
import com.uc4.communication.requests.DeactivateTask
import com.uc4.communication.requests.DisconnectHost
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

class QueuesGETActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */

	public static def list(String version, params,Connection conn,request, grailsattr){return "list${version}"(params,conn)}
	public static def start(String version, params,Connection conn,request, grailsattr){return "changestatus${version}"("START",params,conn)}
	public static def stop(String version, params,Connection conn,request, grailsattr){return "changestatus${version}"("STOP",params,conn)}
	
	/**
	 * @purpose start queues
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def changestatusv1(String ACTION,params,Connection conn){
	def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': ['name (format: name=<String> Process Name(ex: QUEUE.TST.1))'],
			'optional_parameters': [],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
		String TOKEN = params.token;
		String METHOD = params.method;
		String NAMEASSTR = params.name;
		JsonBuilder json;
		// Helper Methods
		if(METHOD == "usage"){
			json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
			if(MiscUtils.checkParams(AllParamMap, params)){

				if(ACTION.equalsIgnoreCase("START")){
					String Resp = CommonAERequests.ChangeQueueStatus(conn,NAMEASSTR,QueueStatus.GREEN)
					if(Resp == null){
						return new JsonBuilder([status: "OK", message: "Start Queue Request Sent"])
					}else{
						return new JsonBuilder([status: "Error", message: Resp])
					}
				}
				
				if(ACTION.equalsIgnoreCase("STOP")){
					String Resp = CommonAERequests.ChangeQueueStatus(conn,NAMEASSTR,QueueStatus.RED)
					if(Resp == null){
						return new JsonBuilder([status: "OK", message: "Stop Queue Request Sent"])
					}else{
						return new JsonBuilder([status: "Error", message: Resp])
					}
					}
		}else{
			json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
			return json
			}
		}
	}	
	
	/**
	 * @purpose list queues
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def listv1(params,Connection conn){
	def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': [],
			'optional_parameters': ['additional info (format: additional= < true|false >'],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
		
		String TOKEN = params.token;
		String METHOD = params.method;
		String ADDL = params.additional;
	
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{	
	
				ArrayList<QueueListItem> items = CommonAERequests.getQueueList(conn);
				
			if(ADDL != null && ADDL.equalsIgnoreCase("true")){
				ArrayList<com.uc4.api.objects.Queue> queues = new ArrayList<com.uc4.api.objects.Queue>();
				
				for(item in items){
					UC4Object MyObj = CommonAERequests.openObject(conn, item.getName(), true);
					com.uc4.api.objects.Queue myQueue = (com.uc4.api.objects.Queue) MyObj;
					queues.add(myQueue);	
				}
				return new JsonBuilder(
					[
						status: "success",
						count: queues.size(),
						data: queues.collect {[
							name: it.getName(),
							type: it.getType(),
							title: it.header().getTitle(),
							arch1: it.header().getArchiveKey1(),
							arch2: it.header().getArchiveKey2(),
							created: it.header().getCreated(),
							modified: it.header().getLastModified(),
							used: it.header().getLastUsed(),
							exceptioncount: it.queue().getExceptionCount(),
							maxslots: it.queue().getMaxSlots(),
							priority: it.queue().getPriority(),
							timezone: it.queue().getTimezone().getName(),
							exceptions: it.queue().exceptionIterator()

							]}
					  ]
				)
			}
			else{	
				
				return new JsonBuilder(
					[
						status: "success",
						count: items.size(),
						data: items.collect {[
							name: it.getName(),
							maxslots: it.getMaxSlots(),
							ERT: it.isConsiderERT(),
							priority: it.getPriority(),
							status: it.getStatus()

							]}
					  ]
				)	
			}
		}
	}
	
}
