package com.automic.actions

import java.util.List;

import com.uc4.api.SearchResultItem;
import com.uc4.api.systemoverview.ClientListItem
import com.uc4.api.systemoverview.ServerListItem
import com.uc4.communication.Connection;
import com.uc4.communication.requests.ClientList
import com.uc4.communication.requests.GetDatabaseInfo
import com.uc4.communication.requests.ServerList

import groovy.json.JsonBuilder

import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests;

class EngineActions {
	
	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def display(String version, params,Connection conn){return "display${version}"(params,conn)}
	
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
			'optional_methods': ['usage','showdb (show AE DB Info)','showclients (show AE Client Info)','showengine (Default: show Automation Engine Info']
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
				
		}else if(METHOD == "showdb"){
			GetDatabaseInfo info = new GetDatabaseInfo();
			conn.sendRequestAndWait(info);
			return new JsonBuilder(
				[
					success: true,
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
			
		}else if(METHOD == "showclients"){
			ClientList req = new ClientList();
			CommonAERequests.sendSyncRequest(conn, req, false)
			ArrayList<ClientListItem> reqList = req.iterator().toList();
			return new JsonBuilder(
				[
					success: true,
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
		}
	}
	
	private static JsonBuilder getServerListAsJSON(List<ServerListItem> ObjList){
		def data = [
			success: true,
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
