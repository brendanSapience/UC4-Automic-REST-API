package com.automic.actions.get

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
import com.uc4.communication.requests.ResumeClient
import com.uc4.communication.requests.ServerList
import com.uc4.communication.requests.SuspendClient
import com.uc4.communication.requests.UserList

import groovy.json.JsonBuilder

import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests;

class ClientGETActions {
	
	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def resume(String version, params,Connection conn,request){return "resume${version}"(params,conn)}
	public static def go(String version, params,Connection conn,request){return "resume${version}"(params,conn)}
	public static def stop(String version, params,Connection conn,request){return "stop${version}"(params,conn)}
	public static def suspend(String version, params,Connection conn,request){return "stop${version}"(params,conn)}
	/**
	 * @purpose
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def resumev1(params,Connection conn){
		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': [],
			'optional_parameters': ['client (format: client=<client number>) => can only be done from Client 0'],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		String CLIENT = params.client;
		
		
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
			
		}else{
		
			if(CLIENT == null || CLIENT ==""){
				ResumeClient req = new ResumeClient();
				CommonAERequests.sendSyncRequest(conn, req, false)
				
				if(req.getMessageBox()!=null){
					return CommonJSONRequests.renderErrorAsJSON(req.getMessageBox());
				}else{
					return CommonJSONRequests.renderOKAsJSON("client started");
				}
			}else{
				// check if we are in Client 0
				if(!conn.getSessionInfo().getClient().equals("0000")){
					return CommonJSONRequests.renderErrorAsJSON("client= parameter can only be used from Client 0. Current Client: " + conn.getSessionInfo().getClient());
				}else{
					// when in client 0...
					ClientList clList = new ClientList();
					conn.sendRequestAndWait(clList);
					ArrayList<ClientListItem> reqList = clList.iterator().toList();
					CLIENT = enforceClientName(CLIENT);
					boolean FoundMatch = false;
					for(int i=0;i<reqList.size();i++){
						ClientListItem item = reqList.get(i);
						
						String ClientName = enforceClientName(item.getClient().toString());
						
						if(ClientName.equals(CLIENT)){
							FoundMatch=true;
							ResumeClient req = new ResumeClient(item);
							CommonAERequests.sendSyncRequest(conn, req, false)
							if(req.getMessageBox()!=null){
								return CommonJSONRequests.renderErrorAsJSON(req.getMessageBox());
							}else{
								return CommonJSONRequests.renderOKAsJSON("client resumed");
							}
						}	
					}
					if(!FoundMatch){
						return CommonJSONRequests.renderErrorAsJSON("Client " + CLIENT + " is not an existing Client.");
					}
				}
			}

		}
	}
	/**
	 * @purpose
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def stopv1(params,Connection conn){
		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': [],
			'optional_parameters': ['client (format: client=<client number>) => can only be done from Client 0'],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		String CLIENT = params.client;
		
		
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
			
		}else{
		
			if(CLIENT == null || CLIENT ==""){
				SuspendClient req = new SuspendClient();
				CommonAERequests.sendSyncRequest(conn, req, false)
				
				if(req.getMessageBox()!=null){
					return CommonJSONRequests.renderErrorAsJSON(req.getMessageBox());
				}else{
					return CommonJSONRequests.renderOKAsJSON("client stopped");
				}
			}else{
				// check if we are in Client 0
				if(!conn.getSessionInfo().getClient().equals("0000")){
					return CommonJSONRequests.renderErrorAsJSON("client= parameter can only be used from Client 0. Current Client: " + conn.getSessionInfo().getClient());
				}else{
					// when in client 0...
					ClientList clList = new ClientList();
					conn.sendRequestAndWait(clList);
					ArrayList<ClientListItem> reqList = clList.iterator().toList();
					CLIENT = enforceClientName(CLIENT);
					boolean FoundMatch = false;
					for(int i=0;i<reqList.size();i++){
						ClientListItem item = reqList.get(i);
						
						String ClientName = enforceClientName(item.getClient().toString());
						
						if(ClientName.equals(CLIENT)){
							FoundMatch=true;
							SuspendClient req = new SuspendClient(item);
							CommonAERequests.sendSyncRequest(conn, req, false)
							if(req.getMessageBox()!=null){
								return CommonJSONRequests.renderErrorAsJSON(req.getMessageBox());
							}else{
								return CommonJSONRequests.renderOKAsJSON("client stopped");
							}
						}
					}
					if(!FoundMatch){
						return CommonJSONRequests.renderErrorAsJSON("Client " + CLIENT + " is not an existing Client.");
					}
				}
			}

		}
	}
	private static def enforceClientName(String ClientName){
		if(ClientName.length()==1){ClientName = "000" + ClientName}
		if(ClientName.length()==2){ClientName = "00" + ClientName}
		if(ClientName.length()==3){ClientName = "0" + ClientName}
		return ClientName;
	}
}
