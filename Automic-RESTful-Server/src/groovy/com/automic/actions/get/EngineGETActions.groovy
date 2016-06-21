package com.automic.actions.get

import java.util.List;

import com.uc4.api.SearchResultItem;
import com.uc4.api.systemoverview.AgentGroupListItem
import com.uc4.api.systemoverview.AgentListItem
import com.uc4.api.systemoverview.ClientListItem
import com.uc4.api.systemoverview.ServerListItem
import com.uc4.api.systemoverview.UserListItem
import com.uc4.communication.Connection;
import com.uc4.communication.requests.AgentGroupList
import com.uc4.communication.requests.AgentList
import com.uc4.communication.requests.ClientList
import com.uc4.communication.requests.GetDatabaseInfo
import com.uc4.communication.requests.ServerList
import com.uc4.communication.requests.UserList

import groovy.json.JsonBuilder

import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests;

class EngineGETActions {
	
	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def display(String version, params,Connection conn,request){return "display${version}"(params,conn)}
	
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
			'optional_methods': ['usage','showdb (show AE DB Info)','showclients (show AE Client Info)','showhosts (show agents info)','showhostgroups (show agentgroups info)','showusers (show connected users info)','showengine (Default: show Automation Engine Info)']
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
				JsonBuilder json = getServerListAsJSON(reqList);
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
						connectionstring:info.getOdbcConnectString()
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
		println "In Here!:" + METHOD
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
						version:it.getVersion()
						]}
				  ]
			)
		
		}
	}
	
	private static JsonBuilder getServerListAsJSON(List<ServerListItem> ObjList){
		def data = [
			status: "success",
			count: ObjList.size(),
			data: ObjList.collect {[name: it.name, connections: it.connections, active: it.active, 
				type: it.port, hostname: it.hostName, ip:it.ipAddress, role:it.role, servertime:it.serverTime.toString(),
				version:it.version]}
		  ]
		
		//, folder:it.folder, modified:it.modified, type: it.title
		def json = new JsonBuilder(data)
		return json;
	}
}
