package com.automic.ae.actions.get

import com.automic.DisplayFilters
import com.uc4.api.DateTime
import com.uc4.api.SearchResultItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter
import com.uc4.api.UC4ObjectName
import com.uc4.api.UC4TimezoneName
import com.uc4.api.UC4UserName
import com.uc4.api.TaskFilter.TimeFrame
import com.uc4.api.systemoverview.UserListItem
import com.uc4.communication.Connection
import com.uc4.communication.requests.ActivityList
import com.uc4.communication.requests.AdoptTask
import com.uc4.communication.requests.CancelTask
import com.uc4.communication.requests.CheckAuthorizations
import com.uc4.communication.requests.CheckUserPrivileges
import com.uc4.communication.requests.DeactivateTask
import com.uc4.communication.requests.DisconnectUser
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
import com.uc4.communication.requests.SuspendTask
import com.uc4.communication.requests.UnblockJobPlanTask
import com.uc4.communication.requests.UnblockWorkflow
import com.uc4.communication.requests.UserList
import com.uc4.communication.requests.XMLRequest

import groovy.json.JsonBuilder

import com.automic.connection.AECredentials
import com.automic.connection.ConnectionManager
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests
import com.automic.utils.MiscUtils
import com.uc4.communication.requests.GetComments.Comment

class UsersGETActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def list(String version, params,Connection conn,request, grailsattr){return "list${version}"(params,conn)}
	public static def disconnect(String version, params,Connection conn,request, grailsattr){return "disconnect${version}"(params,conn)}
	public static def checkperm(String version, params,Connection conn,request, grailsattr){return "checkperm${version}"(params,conn)}
	public static def checkpriv(String version, params,Connection conn,request, grailsattr){return "checkpriv${version}"(params,conn)}
 
	/**
	 * @purpose list all users active & inactive on AE
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def checkprivv1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name= < String > as USER/DEPT )'],
				'optional_parameters': [],
				'optional_filters': [
				],
				'required_methods': [],
				'optional_methods': ['usage']
				]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		String NAME = params.name;

		JsonBuilder json;
		
		// Helper Methods
		if(METHOD == "usage"){
			json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
			
			// check mandatory stuff here
			if(MiscUtils.checkParams(SupportedThings, params)){
				
				CheckUserPrivileges.Candidate p1 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.ACCESS_NOFOLDER);
				CheckUserPrivileges.Candidate p2 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.ACCESS_RECYCLE_BIN);
				CheckUserPrivileges.Candidate p3 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.ACCESS_SYSTEM_OVERVIEW);
				CheckUserPrivileges.Candidate p4 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.ACCESS_TRANSPORT_CASE);
				CheckUserPrivileges.Candidate p5 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.AUTHORIZATIONS_OBJECT_LEVEL);
				CheckUserPrivileges.Candidate p6 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.AUTO_FORECAST);
				CheckUserPrivileges.Candidate p7 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.BACKEND_VARIABLE);
				CheckUserPrivileges.Candidate p8 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.CHANGE_SYSTEM_STATUS);
				CheckUserPrivileges.Candidate p9 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.CREATE_DIAGNOSTIC_INFO);
				CheckUserPrivileges.Candidate p10 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.ECC_ACCESS_ANALYTICS);
				CheckUserPrivileges.Candidate p11 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.ECC_ADMINISTRATION);
				CheckUserPrivileges.Candidate p12 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.ECC_DASHBOARDS);
				CheckUserPrivileges.Candidate p13 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.ECC_DECISION_AUTOMATION);
				CheckUserPrivileges.Candidate p14 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.ECC_MANAGE_SLA_AND_BU);
				CheckUserPrivileges.Candidate p15 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.ECC_MESSAGES);
				CheckUserPrivileges.Candidate p16 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.ECC_PREDICTIVE_ANALYSIS);
				CheckUserPrivileges.Candidate p17 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.ECC_PROCESS_AUTOMATION);
				CheckUserPrivileges.Candidate p18 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.ECC_PROCESS_MONITORING);
				CheckUserPrivileges.Candidate p19 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.ECC_SERVICE_LEVEL_GOVENOR);
				CheckUserPrivileges.Candidate p20 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.EXECUTE_ZERO_DOWNTIME_UPGRADE);
				CheckUserPrivileges.Candidate p21 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.FAVORITES_USER_GROUP);
				CheckUserPrivileges.Candidate p22 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.FILE_TRANSFER_WITHOUT_USERID);
				CheckUserPrivileges.Candidate p23 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.FILEEVENTS_WITHOUT_LOGIN);
				CheckUserPrivileges.Candidate p24 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.ILM_ACTIONS);
				CheckUserPrivileges.Candidate p25 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.LOGON_CALL_API);
				CheckUserPrivileges.Candidate p26 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.MESSAGES_ADMINISTRATORS);
				CheckUserPrivileges.Candidate p27 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.MESSAGES_OWN_CLIENT);
				CheckUserPrivileges.Candidate p28 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.MESSAGES_OWN_GROUP);
				CheckUserPrivileges.Candidate p29 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.MESSAGES_SECURTY);
				CheckUserPrivileges.Candidate p30 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.MODIFY_STATUS_MANUALLY);
				CheckUserPrivileges.Candidate p31 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.RESET_OPEN_FLAG);
				CheckUserPrivileges.Candidate p32 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.SAP_CRITERIA_MANAGER);
				CheckUserPrivileges.Candidate p33 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.SELECTIVE_STATISTICS);
				CheckUserPrivileges.Candidate p34 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.SERVER_USAGE_ALL_CLIENTS);
				CheckUserPrivileges.Candidate p35 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.SQL_VARIABLE);
				CheckUserPrivileges.Candidate p36 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.TAKE_OVER_TASK);
				CheckUserPrivileges.Candidate p37 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.VERSION_MANAGEMENT);
				CheckUserPrivileges.Candidate p38 = new CheckUserPrivileges.Candidate(new UC4UserName(NAME), 	com.uc4.api.objects.UserPrivileges.Privilege.WORK_IN_RUNBOOK_MODE);

				CheckUserPrivileges checkPriv = new CheckUserPrivileges(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10,
					p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23, p24, p25, p26, p27, p28,
					p29, p30, p31, p32, p33, p34, p35, p36, p37, p38);
				
				CommonAERequests.sendSyncRequest(conn, checkPriv, false);
				
				ArrayList<CheckUserPrivileges.Candidate> reqList = checkPriv.iterator().toList();
				
				return new JsonBuilder(
					[
						status: "success",
						count: reqList.size(),
						data: reqList.collect {[
							name:it.name.name,
							privilege:it.privilege,
							result:it.result,
							]}
					  ]
				)
				
			}else{
				json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
				return json
			}
		}
		
	}
	
	/**
	 * @purpose list all users active & inactive on AE
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def checkpermv1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name= < String > )','type (format: type= < String > )',
					'right (format: right= < char (R (Read), W (Write), X (eXecute), D, C, S, P, M, L) > )'],
				'optional_parameters': [],
				'optional_filters': [
				],
				'required_methods': [],
				'optional_methods': ['usage']
				]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		String NAME = params.name;
		String TYPE = params.type;
		String RIGHTASSTR = params.right;
		char RIGHT;
		if(RIGHTASSTR.length() == 1){
			RIGHT = RIGHTASSTR.charAt(0);
		}else{
			// return invalid right..
		}
		JsonBuilder json;
		
		// Helper Methods
		if(METHOD == "usage"){
			json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
			
			// check mandatory stuff here
			if(MiscUtils.checkParams(SupportedThings, params)){
				CheckAuthorizations.Candidate check1;
				try{
					check1 = new CheckAuthorizations.Candidate(new UC4ObjectName(NAME), com.uc4.api.objects.UserRight.Type.valueOf(TYPE), RIGHT);
				}catch(IllegalArgumentException i){
					return CommonJSONRequests.renderErrorAsJSON(i.getMessage())
				}
				
				CheckAuthorizations checkAuth = new CheckAuthorizations(check1);
				CommonAERequests.sendSyncRequest(conn, checkAuth, false);
				
				for (CheckAuthorizations.Candidate c : checkAuth) {
					return new JsonBuilder(
						[
							status: "success",
							count: 1,
							access: c.access,
							name:c.name.name,
							type:c.objectType,
							result:c.result,
						  ]
					)
					//System.out.println("Access mode "+c.getAccess()+" for object "+c.getName()+ (c.getResult() ? " is allowed" : " has been denied"));
				}
				
			}else{
				json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
				return json
			}
		}
		
	}
	
	/**
	 * @purpose list all users active & inactive on AE
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def disconnectv1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name= < String > as USER/DEPT)'],
				'optional_parameters': [],
				'optional_filters': [
				],
				'required_methods': [],
				'optional_methods': ['usage']
				]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		String NAME = params.name;
		
		JsonBuilder json;
		
		// Helper Methods
		if(METHOD == "usage"){
			json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
			
			// check mandatory stuff here
			if(MiscUtils.checkParams(SupportedThings, params)){
				UserList req = new UserList();
				CommonAERequests.sendSyncRequest(conn, req, false)
				ArrayList<UserListItem> reqList = req.iterator().toList();
				boolean UserFound = false;
				for(int i =0 ; i<reqList.size();i++){
					UserListItem userItem = reqList.get(i);
					if(userItem.getName().toString().equalsIgnoreCase(NAME)){
						UserFound = true;
						DisconnectUser disReq = new DisconnectUser(userItem);
						CommonAERequests.sendSyncRequest(conn,disReq, false);
						return CommonJSONRequests.renderOKAsJSON("Disconnect Request Processed.");
					}
				}
				if(!UserFound){
					return CommonJSONRequests.renderOKAsJSON("User Not Found.");
				}
			}else{
				json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
				return json
			}
		}
		
	}
	
	/**
	 * @purpose list all users active & inactive on AE
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def listv1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': [],
				'optional_parameters': [],
				'optional_filters': [
				],
				'required_methods': [],
				'optional_methods': ['usage']
				]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		
		JsonBuilder json;
		
		// Helper Methods
		if(METHOD == "usage"){
			json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
			
			// check mandatory stuff here
			if(MiscUtils.checkParams(SupportedThings, params)){
				UserList req = new UserList();
				CommonAERequests.sendSyncRequest(conn, req, false)
				ArrayList<UserListItem> reqList = req.iterator().toList();
				return new JsonBuilder(
					[
						status: "success",
						count: reqList.size(),
						data: reqList.collect {[
							name:it.getName().name,
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
			}else{
				json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
				return json
			}
		}
		
	}
}
