package automic.restful.server

import com.automic.AECredentials
import com.automic.DisplayFilters
import com.automic.ConnectionManager
import com.uc4.api.DateTime
import com.uc4.api.SearchResultItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter;
import com.uc4.api.UC4ObjectName
import com.uc4.api.TaskFilter.TimeFrame;
import com.uc4.communication.requests.ActivityList
import com.uc4.communication.requests.GenericStatistics;
import com.uc4.communication.requests.RestartTask
import com.uc4.communication.requests.SearchObject
import com.uc4.communication.requests.UnblockJobPlanTask
import com.uc4.communication.requests.UnblockWorkflow
import com.uc4.communication.requests.XMLRequest
import com.automic.MiscUtils

import groovy.json.JsonBuilder

import com.automic.objects.CommonAERequests
import com.automic.CommonJSONRequests

class ActivitiesController {
	
	def index() { }
	
	String[] SupportedOperations=['search','rerun','unblock'];// to come: deactivate,quit,restart,cancel,stop,modifystatus,addcomment
	
	def help = {
		JsonBuilder json = CommonJSONRequests.getStringListAsJSONFormat("operation",SupportedOperations);
		render(text: json, contentType: "text/json", encoding: "UTF-8")
	}
	
	// Each function is versioned.. 
	def searchv1 = {
	
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
			render(text: json, contentType: "text/json", encoding: "UTF-8")
		}else{
			if(request.getHeader("Token")){TOKEN = request.getHeader("Token")};
			if(TOKEN == "DEV"){TOKEN = ConnectionManager.bypassAuth();}
			
			if(ConnectionManager.runTokenChecks(TOKEN)){
				com.uc4.communication.Connection conn = ConnectionManager.getConnectionFromToken(TOKEN);
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
					HandleDateFilter(taskFilter, RawDate);
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
				
				render(text:  json, contentType: "text/json", encoding: "UTF-8")
				
			}
		}

	}

	def rerunv1 = {

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
			render(text: json, contentType: "text/json", encoding: "UTF-8")
		}else{
			if(request.getHeader("Token")){TOKEN = request.getHeader("Token")};
			if(TOKEN == "DEV"){TOKEN = ConnectionManager.bypassAuth();}
			
			String RUNIDASSTR = params.runid;
			int RUNID = RUNIDASSTR.toInteger();
				
			//String FILTERS = params.filters;

			if(ConnectionManager.runTokenChecks(TOKEN)){
				com.uc4.communication.Connection conn = ConnectionManager.getConnectionFromToken(TOKEN);
				if(MiscUtils.checkParams(AllParamMap, params)){
					
					RestartTask req = new RestartTask(RUNID);
			
					XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);
					String json;
					if(res == null){
						json = '{"status":"error","message":"could not restart task"}';
					}else{
						json = '{"status":"success","message":"task restarted"}';
					}
					render(text:  json, contentType: "text/json", encoding: "UTF-8")
					
				}
			
				
			}		
		}
	}
	
	// unblock JOBP, and Tasks within JobPlans (NETFLIX case should be covered also)
	def unblockv1 = {

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
			render(text: json, contentType: "text/json", encoding: "UTF-8")
		}else{
			if(request.getHeader("Token")){TOKEN = request.getHeader("Token")};
			if(TOKEN == "DEV"){TOKEN = ConnectionManager.bypassAuth();}
			
			String RUNIDASSTR = params.runid;
			String LNRASSTR = params.lnr;
			String TYPE = params.type;
			
			int RUNID = -1;
			int LNR = -1;
			
			if(RUNIDASSTR != null){RUNID = RUNIDASSTR.toInteger();}
			if(LNRASSTR != null){LNR = LNRASSTR.toInteger();}
			
			String json;
			
			if((TYPE.toUpperCase() != "JOBS" && TYPE.toUpperCase() != "JOBP") || RUNID == -1){
				json = '{"status":"error","message":"mandatory parameter missing"}';
			}else{
				if(ConnectionManager.runTokenChecks(TOKEN)){
					com.uc4.communication.Connection conn = ConnectionManager.getConnectionFromToken(TOKEN);
					
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
						json = '{"status":"error","message":"could not unblock task"}';
					}else{
						json = '{"status":"success","message":"task unblocked"}';
					}	
				}
			}
			render(text:  json, contentType: "text/json", encoding: "UTF-8")
		}
	}
	
	def HandleDateFilter(TaskFilter req, String RawDate){
		if(RawDate.contains('-')){
			String RawBeginDate = RawDate.split("-")[0];
			String RawEndDate = RawDate.split("-")[1];
			DateTime BeginDate = null;
			DateTime EndDate = null;
			
			String BeginNumberExtracted = RawBeginDate.findAll( /\d+/ )[0] //makes sure we have no other character
			
			// Adjusting the length to 14 char
			if(BeginNumberExtracted.length() == 8){
				BeginNumberExtracted = BeginNumberExtracted + '000000'
			}else if(BeginNumberExtracted.length() == 12){
				BeginNumberExtracted = BeginNumberExtracted + '00'
			}
			// if the length is still incorrect.. we just give an arbitrary window. Why? Because..
			if(BeginNumberExtracted.length() != 14){
				BeginDate = DateTime.now().addMinutes(-4*60);
			}else{
				BeginDate = new DateTime(BeginNumberExtracted.substring(0, 4).toInteger(),BeginNumberExtracted.substring(4, 6).toInteger(),BeginNumberExtracted.substring(6, 8).toInteger(),
					BeginNumberExtracted.substring(8, 10).toInteger(),BeginNumberExtracted.substring(10, 12).toInteger(),BeginNumberExtracted.substring(12, 14).toInteger())
			
			
			}
			
			if(RawEndDate.toUpperCase() =~/NOW/){
				EndDate = DateTime.now();
			} else{
				String EndNumberExtracted = RawEndDate.findAll( /\d+/ )[0] //makes sure we have no other character
				
				// Adjusting the length to 14 char
				if(EndNumberExtracted.length() == 8){
					EndNumberExtracted = EndNumberExtracted + '000000'
				}else if(EndNumberExtracted.length() == 12){
					EndNumberExtracted = EndNumberExtracted + '00'
				}
				// if the length is still incorrect.. we just give an arbitrary window. Why? Because..
				if(EndNumberExtracted.length() != 14){
					EndDate = DateTime.now();
				}else{
					EndDate = new DateTime(EndNumberExtracted.substring(0, 4).toInteger(),EndNumberExtracted.substring(4, 6).toInteger(),EndNumberExtracted.substring(6, 8).toInteger(),
						EndNumberExtracted.substring(8, 10).toInteger(),EndNumberExtracted.substring(10, 12).toInteger(),EndNumberExtracted.substring(12, 14).toInteger())
				}
			}
			
			
			// 20160101 2100 -201701012100
			// 20160101-20170101
			// 201601012100-NOW
			// LASTNHOURS
			
			//println "Begin: " + BeginDate.toString()
			//println "End: " + EndDate.toString()
			req.setTimestampFrom(BeginDate);
			req.setTimestampTo(EndDate);

			
			
		}else{ //LASTNHOURS / LASTNMINUTES / LASTNDAYS |  type
			
		if(RawDate.toUpperCase() =~ /LAST[0-9]+(YEARS|YEAR|YR|Y|MONTHS|MONTH|MTH|DAYS|DAY|D|HOURS|HR|HOUR|H|MIN|MINUTE|MINUTES|SECONDS|SECOND|SEC|S|)/){
			
			String NumberOfUnits = RawDate.findAll( /\d+/ )[0]
			
			int Number = NumberOfUnits.toInteger()
			String Type = RawDate.split(NumberOfUnits)[1]
			
			DateTime NOW = DateTime.now()
			DateTime BEGINNING = DateTime.now()
			
			if(Type.toUpperCase() =~/YEARS|YEAR|YR|Y/){
				BEGINNING.addYears(-Number)
			}
			if(Type.toUpperCase() =~/MONTHS|MONTH|MTH/){
				BEGINNING.addMonth(-Number)
			}
			if(Type.toUpperCase() =~/DAY|DAYS|D/){
				BEGINNING.addDays(-Number)
			}
			if(Type.toUpperCase() =~/HOURS|HOUR|HR|H/){
				BEGINNING.addMinutes(-60*Number)
			}
			if(Type.toUpperCase() =~/MIN|MINUTE|MINUTES/){
				BEGINNING.addMinutes(-Number)
			}
			if(Type.toUpperCase() =~/SECONDS|SECOND|SEC|S/){
				BEGINNING.addSeconds(-Number)
			}

			req.setTimestampFrom(BEGINNING);
			req.setTimestampTo(NOW);
		}

	}
	}
}
