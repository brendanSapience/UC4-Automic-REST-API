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

import groovy.json.JsonBuilder

import com.automic.objects.CommonAERequests
import com.automic.CommonJSONRequests

class ActivitiesController {
	
	def index() { }
	
	String[] SupportedOperations=['search','rerun'];
	
	def help = {
		JsonBuilder json = CommonJSONRequests.getStringListAsJSONFormat("operation",SupportedOperations);
		render(text: json, contentType: "text/json", encoding: "UTF-8")
	}
	
	// Each function is versioned.. 
	def searchv1 = {
	
		HashMap<String,String[]> SupportedThings = new HashMap<String,String[]>();
		SupportedThings.put('required_parameters', ['']);
		SupportedThings.put('optional_parameters', ['']);
		SupportedThings.put('filters', ['status']);
		
		String PRODUCT = params.product;
		String APIVERSION = params.version;
		String TOKEN = params.token;
		String METHOD = params.method;
		String FILTERS = params.filters;
		
		//Temp For Tests
		if(TOKEN == "DEV"){TOKEN = ConnectionManager.bypassAuth();}
		
		// Helper Methods
		if(METHOD == "showfilters"){
			//JsonBuilder json = CommonJSONRequests.getStringListAsJSONFormat("name",SupportedFilters);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
		}
		
		if(ConnectionManager.runTokenChecks(TOKEN)){
			com.uc4.communication.Connection conn = ConnectionManager.getConnectionFromToken(TOKEN);
			TaskFilter taskFilter = new TaskFilter();
			
			DisplayFilters dispFilters = new DisplayFilters(FILTERS);
			
			// !!! status MUST following a very strict structure: comma seperated return codes OR range: "1850,1851" or "1800-1899"
			if(dispFilters.doesKeyExistInFilter("status")){taskFilter.setStatus(dispFilters.getValueFromKey("status"));}
				

			List<Task> TaskList = CommonAERequests.getActivityWindowContent(conn,taskFilter);
			
			JsonBuilder json = CommonJSONRequests.getActivityListAsJSONFormat(TaskList); 
			
			render(text:  json, contentType: "text/json", encoding: "UTF-8")
			
		}

	}

	def rerunv1 = {
		String METHOD = params.method;
		
		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': ['runid=11234567'],
			'optional_parameters': [],
			'filters': [],
			//'optional_parameters': ['force=Y','force=N','commit=Y'],
			//'filters': ['[name:(.A?*)]','[title:(.A?*)]','[process:(regex)]'],
			'methods': []
			]

		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			render(text: json, contentType: "text/json", encoding: "UTF-8")
		}else{
			String PRODUCT = params.product;
			String APIVERSION = params.version;
			String TOKEN = params.token;
			String RUNIDASSTR = params.runid;
			int RUNID = RUNIDASSTR.toInteger();
				
			//String FILTERS = params.filters;
				
			//Temp For Tests
			if(TOKEN == "DEV"){TOKEN = ConnectionManager.bypassAuth();}
				
			if(ConnectionManager.runTokenChecks(TOKEN)){
				com.uc4.communication.Connection conn = ConnectionManager.getConnectionFromToken(TOKEN);
					
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
	
	// unblock JOBP, and Tasks within JobPlans (NETFLIX case should be covered also)
	def unblockv1 = {
		String METHOD = params.method;
		
		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': ['runid=11234567','type=(JOBP|JOBS)'],
			'optional_parameters': ['lnr=0000'],
			'filters': [],
			'methods': []
			]

		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			render(text: json, contentType: "text/json", encoding: "UTF-8")
		}else{
			String PRODUCT = params.product;
			String APIVERSION = params.version;
			String TOKEN = params.token;
			String RUNIDASSTR = params.runid;
			String LNRASSTR = params.lnr;
			String TYPE = params.type;
			
			int RUNID = -1;
			int LNR = -1;
			
			if(RUNIDASSTR != null){RUNID = RUNIDASSTR.toInteger();}
			if(LNRASSTR != null){LNR = LNRASSTR.toInteger();}
			
			String json;
			
			//Temp For Tests
			if(TOKEN == "DEV"){TOKEN = ConnectionManager.bypassAuth();}
			
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
