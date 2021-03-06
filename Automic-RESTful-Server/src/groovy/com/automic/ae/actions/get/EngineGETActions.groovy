package com.automic.ae.actions.get

import java.util.List;

import com.uc4.api.SearchResultItem;
import com.uc4.api.systemoverview.AgentGroupListItem
import com.uc4.api.systemoverview.AgentListItem
import com.uc4.api.systemoverview.ClientListItem
import com.uc4.api.systemoverview.MessageListItem
import com.uc4.api.systemoverview.ServerListItem
import com.uc4.api.systemoverview.UserListItem
import com.uc4.api.systemoverview.WorkloadItem
import com.uc4.communication.Connection;
import com.uc4.communication.requests.AgentGroupList
import com.uc4.communication.requests.AgentList
import com.uc4.communication.requests.ClientList
import com.uc4.communication.requests.GetDatabaseInfo
import com.uc4.communication.requests.MessageList
import com.uc4.communication.requests.ServerList
import com.uc4.communication.requests.StartServer
import com.uc4.communication.requests.SystemWorkload
import com.uc4.communication.requests.TerminateServer
import com.uc4.communication.requests.UserList
import com.uc4.communication.requests.GetDatabaseInfo.MQEntry

import groovy.json.JsonBuilder

import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests;
import com.automic.utils.MiscUtils

class EngineGETActions {
	
	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def display(String version, params,Connection conn,request,grailsAttributes){return "display${version}"(params,conn)}
	public static def start(String version, params,Connection conn,request,grailsAttributes){return "start${version}"(params,conn)}
	public static def stop(String version, params,Connection conn,request,grailsAttributes){return "stop${version}"(params,conn)}
	
	/**
	 * @purpose stop a server process
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def stopv1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name=<String> Process Name(ex: UC4#WP002))'],
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
				
				TerminateServer req = new TerminateServer(NAMEASSTR);
				
				CommonAERequests.sendSyncRequest(conn, req, false);
				
				return CommonJSONRequests.renderOKAsJSON("Process Start Request Processed.")
				
			}else{
					json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
					return json
			}
		
		}
	}
	
	/**
	 * @purpose start a server process
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def startv1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name=<String> Process Name(ex: UC4#WP002))'],
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
				
				StartServer req = new StartServer(NAMEASSTR);
				
				CommonAERequests.sendSyncRequest(conn, req, false);
				
				return CommonJSONRequests.renderOKAsJSON("Process Start Request Processed.")
				
			}else{
					json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
					return json
			}
		
		}
	}
	
	/**
	 * @purpose 
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def displayv1(params,Connection conn){
		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': [],
			'optional_parameters': [],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage','showusage (show AE Usage info)', 'showmessages (show AE Messages)', 'showdb (show AE DB Info)','showclients (show AE Client Info)','showhosts (show agents info)','showhostgroups (show agentgroups info)','showusers (show connected users info)','showengine (Default: show Automation Engine Info)']
			]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
			//Default is to show engine info
		}else if( METHOD == null || METHOD == "showengine" || METHOD == "" ){
		
				ServerList req = new ServerList();
				CommonAERequests.sendSyncRequest(conn, req, false)
				ArrayList<ServerListItem> reqList = req.iterator().toList();
				JsonBuilder json = getServerListAsJSON(reqList, conn);
				return json
				
		}else if(METHOD.toLowerCase() =~ /showdb|db|database|databases|showdatabase/){
			GetDatabaseInfo info = new GetDatabaseInfo();
			conn.sendRequestAndWait(info);
			return new JsonBuilder(
				[
					status: "success",
					data:[
						dbtype: info.getDbmsName(),
						dbversion:info.getDbmsVersion(),
						dbname:info.getDbName(),
						drivername:info.getDriverName(),
						driverversion:info.getDriverVersion(),
						dataversion:info.getInitialDataVersion(),
						odbcversion:info.getOdbcVersion(),
						connectionstring:info.getOdbcConnectString(),
						mqdwp:info.getMqdwp(),
						mqjwp:info.getMqjwp(),
						mqmem:info.getMqmem(),
						mqowp:info.getMqowp(),
						mqpwp:info.getMqpwp(),
						mqrwp:info.getMqrwp(),
						mqqwp:info.getMqwp(),
						mqentries:getMQEntriesAsJson(info.getMessageQueues())
					]
				  ]
			)
			
		}else if(METHOD =~ /showclients|client|clients|showclient|clnt|showclnt/){
			ClientList req = new ClientList();
			CommonAERequests.sendSyncRequest(conn, req, false)
			ArrayList<ClientListItem> reqList = req.iterator().toList();
			return new JsonBuilder(
				[
					status: "success",
					count: reqList.size(),
					data: reqList.collect {[
						client:it.client,
						id:it.id,
						active:it.clientActive,
						activities:it.numberOfActivities,
						objects:it.numberOfObjects,
						users:it.numberOfUsers,
						priority:it.priority,
						taskpriority:it.taskPriority,
						tz:it.timezone,
						title:it.title
						]}
				  ]
			)
		}else if(METHOD =~ /showmessages|getmessages/){
			MessageList req = new MessageList();
			req.setAdminMessages(true);
			req.setSecurityMessages(true);
			
			CommonAERequests.sendSyncRequest(conn, req, false)
			ArrayList<MessageListItem> reqList = req.iterator().toList();
			return new JsonBuilder(
				[
					status: "success",
					count: reqList.size(),
					data: reqList.collect {[
						category:it.category,
						type:it.messageType,
						message:it.message,
						source:it.source,
						timestamp:it.timestamp.toString(),
			
						]}
				  ]
			)
		}else if(METHOD =~ /showusage|getusage/){
			SystemWorkload req = new SystemWorkload();
			CommonAERequests.sendSyncRequest(conn, req, false);
			ArrayList<WorkloadItem> reqList = req.iterator().toList();
			// Add All Clients perf: (marked as -1 in JSON output)
			reqList.add(req.getTotalWorkload())
			
			return new JsonBuilder(
				[
					status: "success",
					count: reqList.size(),
					data: reqList.collect {[
						client:it.client,
						average:it.averageLoad,
						active:it.hours
						]}
				  ]
			)
		}	else if(METHOD =~ /showagentgroups|showhostgroups|agentgroups|hostgroups|agentgroup|hostgroup/){
			AgentGroupList req = new AgentGroupList();
			CommonAERequests.sendSyncRequest(conn, req, false)
			ArrayList<AgentGroupListItem> reqList = req.iterator().toList();
			return new JsonBuilder(
				[
					status: "success",
					count: reqList.size(),
					data: reqList.collect {[
						name:it.getName(),
						client:it.getClient(),
						variant:it.getJclVariant(),
						mode:it.getMode(),
						numparalleltasks:it.getParallelTasks(),
						]}
				  ]
			)
		
		}
		else if(METHOD =~ /showagents|showhosts|shownodes|agents|hosts|nodes|agent|host|node/){
			AgentList req = new AgentList();
			CommonAERequests.sendSyncRequest(conn, req, false)
			ArrayList<AgentListItem> reqList = req.iterator().toList();
			return new JsonBuilder(
				[
					status: "success",
					count: reqList.size(),
					data: reqList.collect {[
						name:it.getName(),
						auth:it.getAuthorizations(),
						hardware:it.getHardware(),
						ip:it.getIpAddress(),
						variant:it.getJclVariant(),
						keepalive:it.getKeepAlive().toString(),
						lastcheck:it.getLastCheck().toString(),
						liccategory:it.getLicenseCategory(),
						licclass:it.getLicenseClass(),
						maxjobres:it.getMaxJobResources(),
						numclients:it.getNumberOfClients().toString(),
						roles:it.getRoles(),
						software:it.getSoftware(),
						softwareversion: it.getSoftwareVersion(),
						version:it.getVersion()
						]}
				  ]
			)
		
		}
	
		else if(METHOD =~ /showusers|showuser|users|user/){
			
			UserList req = new UserList();
			CommonAERequests.sendSyncRequest(conn, req, false)
			ArrayList<UserListItem> reqList = req.iterator().toList();
			return new JsonBuilder(
				[
					status: "success",
					count: reqList.size(),
					data: reqList.collect {[
						name:it.getName(),
						firstname:it.getFirstName(),
						lastname:it.getLastName(),
						isloggedon:it.isLoggedOn,
						arch1:it.getArchiveKey1(),
						arch2:it.getArchiveKey2(),
						client:it.getClient(),
						host:it.getHost(),
						language:it.getLanguage(),
						lastsession:it.getLastSession().toString(),
						mail1:it.getMail1(),
						mail2:it.getMail2(),
						remoteid:it.getRemoteID(),
						sessionid:it.getSessionId(),
						sessiontz: it.getSessionTimeZone(),
						usertz:it.getUserTimeZone(),
						version:it.getVersion(),
						active:it.active,
						cp:it.cp,
						id:it.id,
						sessiontz:it.sessionTimeZone.toString()
						]}
				  ]
			)
		
		}
	}
	
	private static getMQEntriesAsJson(MQEntry[] entries){
		return  [
			entries.collect {[
				name: it.name, 
				count: it.count,
				lastsec:it.lastSecond,
				last10sec:it.last10Seconds,
				last60sec:it.last60Seconds,
				lastmin: it.getResponseLastMinute(), 
				last10min: it.getResponseTime10Minutes(), 
				last60min: it.getResponseTime60Minutes()
				]}
		]
	}
	// not used:
	private static JsonBuilder getServerWorkload(ServerListItem item, Connection conn){
		SystemWorkload req = new SystemWorkload(item);
		
		CommonAERequests.sendSyncRequest(conn, req, false);
		if(req.getMessageBox()!=null){
			return new JsonBuilder({[]})
		}else{
			WorkloadItem WorkloadIt = req.size();
			
			return new JsonBuilder(
				{[
						//client:WorkloadIt.client,
						average:WorkloadIt.averageLoad,
						active:WorkloadIt.hours
				]}
			)
		}

	}
	private static JsonBuilder getServerListAsJSON(List<ServerListItem> ObjList, Connection conn){
		
		SystemWorkload req = new SystemWorkload();
		CommonAERequests.sendSyncRequest(conn, req, false);
		
		
		def data = [
			status: "success",
			count: ObjList.size(),
			data: ObjList.collect {[
				name: it.name, 
				connections: it.connections, 
				active: it.active, 
				type: it.getType(), 
				hostname: it.hostName, 
				ip:it.ipAddress, 
				role:it.role, 
				servertime:it.serverTime.toString(),
				version:it.version,
				b01:it.getB01(),
				b10:it.getB10(),
				b60:it.getB60(),
				mqactive:it.getMqSetActive(),
				mqown:it.getMqSetOwn(),
				netarea:it.getNetArea(),
				port:it.getPort(),
				id:it.getId(),
				processid:it.getProcessId(),
				//workload: {getServerWorkload(it,conn)}
				]}
		  ]
		
		//, folder:it.folder, modified:it.modified, type: it.title
		def json = new JsonBuilder(data)
		return json;
	}
}
