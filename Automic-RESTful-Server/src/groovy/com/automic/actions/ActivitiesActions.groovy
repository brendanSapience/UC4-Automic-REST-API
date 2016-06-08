package com.automic.actions

import com.automic.DisplayFilters
import com.uc4.api.DateTime
import com.uc4.api.SearchResultItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter;
import com.uc4.api.UC4ObjectName
import com.uc4.api.TaskFilter.TimeFrame;
import com.uc4.communication.Connection
import com.uc4.communication.requests.ActivityList
import com.uc4.communication.requests.CancelTask
import com.uc4.communication.requests.DeactivateTask
import com.uc4.communication.requests.GenericStatistics;
import com.uc4.communication.requests.QuitTask
import com.uc4.communication.requests.RestartTask
import com.uc4.communication.requests.ResumeTask
import com.uc4.communication.requests.RollbackTask
import com.uc4.communication.requests.SearchObject
import com.uc4.communication.requests.SuspendTask
import com.uc4.communication.requests.UnblockJobPlanTask
import com.uc4.communication.requests.UnblockWorkflow
import com.uc4.communication.requests.XMLRequest

import groovy.json.JsonBuilder

import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests;
import com.automic.utils.MiscUtils;

class ActivitiesActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def search(String version, params,Connection conn){return "search${version}"(params,conn)}
	public static def deactivate(String version, params,Connection conn){return "deactivate${version}"(params,conn)}
	public static def rerun(String version, params,Connection conn){return "rerun${version}"(params,conn)}
	public static def quit(String version, params,Connection conn){return "quit${version}"(params,conn)}
	public static def unblock(String version, params,Connection conn){return "unblock${version}"(params,conn)}
	public static def cancel(String version, params,Connection conn){return "cancel${version}"(params,conn)}
	public static def resume(String version, params,Connection conn){return "resume${version}"(params,conn)}
	public static def rollback(String version, params,Connection conn){return "rollback${version}"(params,conn)}
	public static def suspend(String version, params,Connection conn){return "suspend${version}"(params,conn)}

	
	/**
	 * @purpose search activities (Activities window) against filters
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def searchv1(params,Connection conn){
	
		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': [],
			'optional_parameters': [],
			'optional_filters': ['name (format: filters=[name:DEM.*])','status (format: filters=[status:1900])','key1 (format: filters=[key1:*.*]','key2 (format: filters=[key2:*.*]',
				'queue (format: filters=[queue:*])','activation (format: filters=[activation:YYYYMMDDHHMM-YYYYMMDDHHMM])','host (format: filters=[host:WIN01*])',
				'parentrunid (format: filters=[parentrunid:12345678901100])','toprunid (format: filters=[toprunid:12345678901100])',
				'user (format: filters=[user:*])','platform (format: filters=[platform:WIN|UNIX])','type (format: filters=[type:JOBF|JOBS])',
				'exkey1 (format: filters=[exkey1])','exkey2 (format: filters=[exkey2])','exhost (format: filters=[exhost])',
				'exname (format: filters=[exname])','exuser (format: filters=[exuser])'
				],
			'required_methods': [],
			'optional_methods': ['usage']
			]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
		
				TaskFilter taskFilter = new TaskFilter();
				
				//if(FILTERS == null){FILTERS='';}
				DisplayFilters dispFilters = new DisplayFilters(FILTERS);
				taskFilter.setTimeFrame(TimeFrame.ALL);
				// !!! status MUST following a very strict structure: comma seperated return codes OR range: "1850,1851" or "1800-1899"
				if(dispFilters.doesKeyExistInFilter("status")){taskFilter.setStatus(dispFilters.getValueFromKey("status"));}
				if(dispFilters.doesKeyExistInFilter("key1")){taskFilter.setArchiveKey1(dispFilters.getValueFromKey("key1"));}
				if(dispFilters.doesKeyExistInFilter("key2")){taskFilter.setArchiveKey2(dispFilters.getValueFromKey("key2"));}
				if(dispFilters.doesKeyExistInFilter("name")){taskFilter.setObjectName(dispFilters.getValueFromKey("name"));}
				if(dispFilters.doesKeyExistInFilter("queue")){taskFilter.setQueueFilter(new UC4ObjectName(dispFilters.getValueFromKey("queue")));}
				if(dispFilters.doesKeyExistInFilter("host")){taskFilter.setHost(dispFilters.getValueFromKey("host"));}
				if(dispFilters.doesKeyExistInFilter("parentrunid")){taskFilter.setParentRunID(dispFilters.getValueFromKey("parentrunid").toInteger());}
				if(dispFilters.doesKeyExistInFilter("toprunid")){taskFilter.setTopRunID(dispFilters.getValueFromKey("toprunid").toInteger());}
				if(dispFilters.doesKeyExistInFilter("user")){taskFilter.setUser(dispFilters.getValueFromKey("user"));}
				
				if(dispFilters.doesKeyExistInFilter("exkey1")){taskFilter.setExcludeArchiveKey1(true);}
				if(dispFilters.doesKeyExistInFilter("exkey2")){taskFilter.setExcludeArchiveKey2(true);}
				if(dispFilters.doesKeyExistInFilter("exhost")){taskFilter.setExcludeHost(true);}
				if(dispFilters.doesKeyExistInFilter("exname")){taskFilter.setExcludeObjectName(true);}
				if(dispFilters.doesKeyExistInFilter("exuser")){taskFilter.setExcludeUser(true);}
				
				if(dispFilters.doesKeyExistInFilter("activation")){
					taskFilter.setTimeFrame(TimeFrame.BETWEEN);
					String RawDate = dispFilters.getValueFromKey("activation");
					DateTime[] DTs = MiscUtils.HandleDateFilter(RawDate);
					taskFilter.setTimestampFrom(DTs[0]);
					taskFilter.setTimestampTo(DTs[1]);
				}
				
				// filters=[platform:UNIX|WIN|CIT]
				if(dispFilters.doesKeyExistInFilter("platform")){
					taskFilter.unselectAllPlatforms();
					String AllPlatformSelected = dispFilters.getValueFromKey("platform");
					String[] AllPlatformsArray = AllPlatformSelected.split("\\|");
					
					AllPlatformsArray.each {
							switch(it.toUpperCase()){
								case ~/WIN|WINDOWS/:taskFilter.setPlatformWindows(true);break;
								case ~/UNX|UNIX|LINUX|REDHAT|UBUNTU|SOLARIS|UX/:taskFilter.setPlatformUNIX(true);break;
								case ~/BS2000|BS/:taskFilter.setPlatformBS2000(true);break;
								case ~/CIT|RA/:taskFilter.setPlatformCIT(true);break;
								case ~/GCO|GCOS8/:taskFilter.setPlatformGCOS8(true);break;
								case ~/JMX/:taskFilter.setPlatformJMX(true);break;
								case ~/MAIL/:taskFilter.setPlatformMAIL(true);break;
								case ~/MPE/:taskFilter.setPlatformMPE(true);break;
								case ~/MVS/:taskFilter.setPlatformMVS(true);break;
								case ~/NSK/:taskFilter.setPlatformNSK(true);break;
								case ~/OA/:taskFilter.setPlatformOA(true);break;
								case ~/OS400|AS400/:taskFilter.setPlatformOS400(true);break;
								case ~/PS/:taskFilter.setPlatformPS(true);break;
								case ~/SIEBEL|SEIBEL/:taskFilter.setPlatformSiebel(true);break;
								case ~/R3/:taskFilter.setPlatformR3(true);break;
								case ~/SQL/:taskFilter.setPlatformSQL(true);break;
								case ~/VMS/:taskFilter.setPlatformVMS(true);break;
								}
						}
				}
				
				if(dispFilters.doesKeyExistInFilter("type")){
					
					taskFilter.unselectAllObjects();
					String AllTypesSelected = dispFilters.getValueFromKey("type");
					String[] AllTypessArray = AllTypesSelected.split("\\|");
					AllTypessArray.each{
						switch(it.toUpperCase()) {
							case ~/HOSTG/:taskFilter.setTypeHOSTG(true);break;
							case ~/PERIOD/:taskFilter.setTypePERIOD(true);break;
							case ~/API/:taskFilter.setTypeAPI(true);break;
							case ~/C_HOSTG/:taskFilter.setTypeC_HOSTG(true);break;
							case ~/C_PERIOD/:taskFilter.setTypeC_PERIOD(true);break;
							case ~/CALL|NOTIFICATION/:taskFilter.setTypeCALL(true);break;
							case ~/CPIT|COCKPIT/:taskFilter.setTypeCPIT(true);break;
							case ~/EVNT|EVENT/:taskFilter.setTypeEVNT(true);break;
							case ~/JOBD/:taskFilter.setTypeJOBD(true);break;
							case ~/JOBF|MFT|FILETRANSFER|TRANSFER/:taskFilter.setTypeJOBF(true);break;
							case ~/JOBG/:taskFilter.setTypeJOBG(true);break;
							case ~/JOBP|JOBPLAN|WORKFLOW|JOBFLOW/:taskFilter.setTypeJOBP(true);break;
							case ~/JOBQ/:taskFilter.setTypeJOBQ(true);break;
							case ~/JOBS|JOB/:taskFilter.setTypeJOBS(true);break;
							case ~/JSCH|SCHEDULE|JOBSCH|SCHED/:taskFilter.setTypeJSCH(true);break;
							case ~/REPORT|REP/:taskFilter.setTypeREPORT(true);break;
							case ~/SCRI|SCRIPT/:taskFilter.setTypeSCRI(true);break;

						}
					}
				}
					
				List<Task> TaskList = CommonAERequests.getActivityWindowContent(conn,taskFilter);
				
				JsonBuilder json = CommonJSONRequests.getActivityListAsJSONFormat(TaskList);
				return json
		}

	}
	
	/**
	* @purpose deactivate a given activity by runid
	* @return JsonBuilder object
	* @version v1
	*/
	public static def deactivatev1(params,Connection conn) {

		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': ['runid (format: runid= < integer >'],
			'optional_parameters': ['force (format: force=Y)'],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]

		String FILTERS = params.filters;
		String METHOD = params.method;
		String FORCE = params.force;
		
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			return json
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
		}else{

				if(MiscUtils.checkParams(AllParamMap, params)){
					
					String RUNIDASSTR = params.runid;
					int RUNID = RUNIDASSTR.toInteger();

					boolean ForceDeactivate = false;
					if(FORCE.toUpperCase() =~/Y|YES|OK|TRUE/){
						ForceDeactivate=true;
					}
					DeactivateTask req = new DeactivateTask(RUNID,ForceDeactivate);
			
					XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);
					if(res == null){
						JsonBuilder json = new JsonBuilder([status: "error", message: "could not deactivate task"])
						return json
					}else{
						JsonBuilder json = new JsonBuilder([status: "success", message: "task deactivated"])
						return json
					}					
				}
		}
	}
	
	/**
	 * @purpose rollback a given activity by runid
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def rollbackv1(params,Connection conn){
		
		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': ['runid (format: runid= <integer>'],
			'optional_parameters': [],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
	
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
				
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			return json
		}else{
			if(MiscUtils.checkParams(AllParamMap, params)){
				String RUNIDASSTR = params.runid;
				int RUNID = RUNIDASSTR.toInteger();
				String RECURSIVEASSTR = params.recursive
				boolean RECURSIVE = false;
				if(RECURSIVEASSTR != null && RECURSIVEASSTR.toUpperCase() =~/Y|YES|OK/){RECURSIVE=true;}
	
				RollbackTask req = new RollbackTask(RUNID,RECURSIVE);
				XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);
				if(res == null){
					JsonBuilder json = new JsonBuilder([status: "error", message: "could not rollback task"])
					return json
				}else{
					JsonBuilder json = new JsonBuilder([status: "success", message: "task rolled back"])
					return json
				}
			}else{
				JsonBuilder json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
				return json
			}
		}
	}
	
	/**
	 * @purpose suspend a given activity by runid
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def suspendv1(params,Connection conn){
		
		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': ['runid (format: runid= <integer>'],
			'optional_parameters': ['recursive (format: recursive=[Y|N])'],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
	
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
				
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			return json
		}else{
			if(MiscUtils.checkParams(AllParamMap, params)){
				String RUNIDASSTR = params.runid;
				int RUNID = RUNIDASSTR.toInteger();
				String RECURSIVEASSTR = params.recursive
				boolean RECURSIVE = false;
				if(RECURSIVEASSTR != null && RECURSIVEASSTR.toUpperCase() =~/Y|YES|OK/){RECURSIVE=true;}
	
				SuspendTask req = new SuspendTask(RUNID,RECURSIVE);
				XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);
				if(res == null){
					JsonBuilder json = new JsonBuilder([status: "error", message: "could not resume task"])
					return json
				}else{
					JsonBuilder json = new JsonBuilder([status: "success", message: "task resumed"])
					return json
				}
			}else{
				JsonBuilder json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
				return json
			}
		}
	}
	
	/**
	 * @purpose resume a given activity by runid
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def resumev1(params,Connection conn){
		
		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': ['runid (format: runid= <integer>'],
			'optional_parameters': ['recursive (format: recursive=[Y|N])'],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
	
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
				
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			return json
		}else{
			if(MiscUtils.checkParams(AllParamMap, params)){
				String RUNIDASSTR = params.runid;
				int RUNID = RUNIDASSTR.toInteger();
				String RECURSIVEASSTR = params.recursive
				boolean RECURSIVE = false;
				if(RECURSIVEASSTR != null && RECURSIVEASSTR.toUpperCase() =~/Y|YES|OK/){RECURSIVE=true;}
	
				ResumeTask req = new ResumeTask(RUNID,RECURSIVE);
				XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);
				if(res == null){
					JsonBuilder json = new JsonBuilder([status: "error", message: "could not resume task"])
					return json
				}else{
					JsonBuilder json = new JsonBuilder([status: "success", message: "task resumed"])
					return json
				}
			}else{
				JsonBuilder json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
				return json
			}
		}
	}
	
	/**
	 * @purpose cancel a given activity by runid
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def cancelv1(params,Connection conn){
		
		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': ['runid (format: runid= <integer>'],
			'optional_parameters': ['recursive (format: recursive=[Y|N])'],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
	
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
				
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			return json
		}else{
			if(MiscUtils.checkParams(AllParamMap, params)){
				String RUNIDASSTR = params.runid;
				int RUNID = RUNIDASSTR.toInteger();
				String RECURSIVEASSTR = params.recursive
				boolean RECURSIVE = false;
				if(RECURSIVEASSTR != null && RECURSIVEASSTR.toUpperCase() =~/Y|YES|OK/){RECURSIVE=true;}
	
				CancelTask req = new CancelTask(RUNID,RECURSIVE);
				XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);
				if(res == null){
					JsonBuilder json = new JsonBuilder([status: "error", message: "could not cancel task"])
					return json
				}else{
					JsonBuilder json = new JsonBuilder([status: "success", message: "task cancelled"])
					return json
				}
			}else{
						JsonBuilder json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
						return json
			}
		}
	}
	
	/**
	 * @purpose quit a given activity by runid
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def quitv1(params,Connection conn){

		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': ['runid (format: runid= < integer >'],
			'optional_parameters': [],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
	
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
				
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			return json
		}else{		
			if(MiscUtils.checkParams(AllParamMap, params)){
				String RUNIDASSTR = params.runid;
				int RUNID = RUNIDASSTR.toInteger();
	
				QuitTask req = new QuitTask(RUNID);
					
				XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);
				if(res == null){
					JsonBuilder json = new JsonBuilder([status: "error", message: "could not quit task"])
					return json
				}else{
					JsonBuilder json = new JsonBuilder([status: "success", message: "task quit done"])
					return json
				}		
			}
		}
	}
	
	/**
	 * @purpose rerun a given activity by runid
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def rerunv1(params,Connection conn){

		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': ['runid (format: runid= < integer >'],
			'optional_parameters': [],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]

		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
				if(MiscUtils.checkParams(AllParamMap, params)){
					String RUNIDASSTR = params.runid;
					int RUNID = RUNIDASSTR.toInteger();
					RestartTask req = new RestartTask(RUNID);
			
					XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);

					if(res == null){
						JsonBuilder json = new JsonBuilder([status: "error", message: "could not restart task"])
						return json
					}else{
						JsonBuilder json = new JsonBuilder([status: "success", message: "task restarted"])
						return json
					}
				}

		}
	}
	
	/**
	* @purpose unblock a given activity by runid (as standalone or part of a workflow (in which case it needs lnr))
	* @return JsonBuilder object
	* @version v1
	*/
	public static def unblockv1(params,Connection conn){

		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': ['runid (format: runid= < integer >','type (format: type=(JOBP|JOBS) )'],
			'optional_parameters': ['lnr (format: lnr= < integer >'],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]

		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			return json
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
		}else{
			if(MiscUtils.checkParams(AllParamMap, params)){
				String RUNIDASSTR = params.runid;
				String LNRASSTR = params.lnr;
				String TYPE = params.type;
			
				int RUNID = -1;
				int LNR = -1;
			
				if(RUNIDASSTR != null){RUNID = RUNIDASSTR.toInteger();}
				if(LNRASSTR != null){LNR = LNRASSTR.toInteger();}
			
				if((TYPE.toUpperCase() != "JOBS" && TYPE.toUpperCase() != "JOBP") || RUNID == -1){
					JsonBuilder json = new JsonBuilder([status: "error", message: "mandatory parameter missing"])
					return json
				}else{
					
						XMLRequest req;
					
						if(TYPE.toUpperCase() == "JOBS"){
							if(LNR != -1){
								req = (UnblockJobPlanTask) new UnblockJobPlanTask(RUNID,LNR);
							}else{
								req = (UnblockJobPlanTask) new UnblockJobPlanTask(RUNID);
							}
						}else if(TYPE.toUpperCase() == "JOBP"){
							req = (UnblockWorkflow) new UnblockWorkflow(RUNID);
						}
			
						XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);
					
						if(res == null){
							JsonBuilder json = new JsonBuilder([status: "error", message: "could not unblock task"])
							return json
						}else{
							JsonBuilder json = new JsonBuilder([status: "success", message: "task unblocked"])
							return json
						}
				}
			}
		}
	}
}
