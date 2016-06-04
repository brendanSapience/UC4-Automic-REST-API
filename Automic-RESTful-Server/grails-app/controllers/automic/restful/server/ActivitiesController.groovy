package automic.restful.server

import com.automic.AECredentials
import com.automic.DisplayFilters
import com.automic.ConnectionManager
import com.uc4.api.SearchResultItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter;
import com.uc4.communication.requests.ActivityList
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
	
	String[] SupportedOperations=['search','rerun','unblock'];
	
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
			'optional_filters': ['status (format: filters=[status:1900])','key1 (format: filters=[key1:*.*]','type (format: filters=[type:JOBF])'],
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
				
				// !!! status MUST following a very strict structure: comma seperated return codes OR range: "1850,1851" or "1800-1899"
				if(dispFilters.doesKeyExistInFilter("status")){taskFilter.setStatus(dispFilters.getValueFromKey("status"));}
				if(dispFilters.doesKeyExistInFilter("key1")){taskFilter.setArchiveKey1(dispFilters.getValueFromKey("key1"));}
				if(dispFilters.doesKeyExistInFilter("type")){
					String TYPE = dispFilters.getValueFromKey("type").toUpperCase();
					println "Type: " + TYPE;
					taskFilter.unselectAllObjects();
					switch(TYPE) {
						case 'API':taskFilter.setTypeAPI(true);break;
						case 'C_HOSTG':taskFilter.setTypeC_HOSTG(true);break;
						case 'C_PERIOD':taskFilter.setTypeC_PERIOD(true);break;
						case 'CALL':taskFilter.setTypeCALL(true);break;
						case 'CPIT':taskFilter.setTypeCPIT(true);break;
						case 'EVNT':taskFilter.setTypeEVNT(true);break;
						case 'HOSTG':taskFilter.setTypeHOSTG(true);break;
						case 'JOBD':taskFilter.setTypeJOBD(true);break;
						case 'JOBF':taskFilter.setTypeJOBF(true);break;
						case 'JOBG':taskFilter.setTypeJOBG(true);break;
						case 'JOBP':taskFilter.setTypeJOBP(true);break;
						case 'JOBQ':taskFilter.setTypeJOBQ(true);break;
						case 'JOBS':taskFilter.setTypeJOBS(true);break;
						case 'JSCH':taskFilter.setTypeJSCH(true);break;
						case 'PERIOD':taskFilter.setTypePERIOD(true);break;
						case 'REPORT':taskFilter.setTypeREPORT(true);break;
						case 'SCRI':taskFilter.setTypeSCRI(true);break;
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
}
