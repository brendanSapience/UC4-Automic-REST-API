package automic.restful.server


import java.util.Iterator;

import com.automic.AECredentials
import com.automic.DisplayFilters
import com.automic.ConnectionManager
import com.uc4.api.SearchResultItem
import com.uc4.api.StatisticSearchItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter;
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

class StatisticsController {
	
	def index() { }
	
	String[] SupportedOperations=['search'];
	
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
			'optional_methods': []
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
				//if(FILTERS == null){FILTERS='';}
				DisplayFilters dispFilters = new DisplayFilters(FILTERS);
				
				GenericStatistics req = new GenericStatistics();
				req.setClient(200);
				//req.selectAllTypes();
				req.setTypeJOBF(true);
				// !!! status MUST following a very strict structure: comma seperated return codes OR range: "1850,1851" or "1800-1899"
//				if(dispFilters.doesKeyExistInFilter("status")){taskFilter.setStatus(dispFilters.getValueFromKey("status"));}
//				if(dispFilters.doesKeyExistInFilter("key1")){taskFilter.setArchiveKey1(dispFilters.getValueFromKey("key1"));}
//				if(dispFilters.doesKeyExistInFilter("type")){
//					String TYPE = dispFilters.getValueFromKey("type").toUpperCase();
//					println "Type: " + TYPE;
//					taskFilter.unselectAllObjects();
//					switch(TYPE) {
//						case 'API':taskFilter.setTypeAPI(true);break;
//						case 'C_HOSTG':taskFilter.setTypeC_HOSTG(true);break;
//						case 'C_PERIOD':taskFilter.setTypeC_PERIOD(true);break;
//						case 'CALL':taskFilter.setTypeCALL(true);break;
//						case 'CPIT':taskFilter.setTypeCPIT(true);break;
//						case 'EVNT':taskFilter.setTypeEVNT(true);break;
//						case 'HOSTG':taskFilter.setTypeHOSTG(true);break;
//						case 'JOBD':taskFilter.setTypeJOBD(true);break;
//						case 'JOBF':taskFilter.setTypeJOBF(true);break;
//						case 'JOBG':taskFilter.setTypeJOBG(true);break;
//						case 'JOBP':taskFilter.setTypeJOBP(true);break;
//						case 'JOBQ':taskFilter.setTypeJOBQ(true);break;
//						case 'JOBS':taskFilter.setTypeJOBS(true);break;
//						case 'JSCH':taskFilter.setTypeJSCH(true);break;
//						case 'PERIOD':taskFilter.setTypePERIOD(true);break;
//						case 'REPORT':taskFilter.setTypeREPORT(true);break;
//						case 'SCRI':taskFilter.setTypeSCRI(true);break;
//					}
//				}
				

				String Msg = CommonAERequests.sendSyncRequestWithMsgReturn(conn,req);

				if(Msg.contains("too many")){ 
					//Your selection results in too many statistics (count '46635'). Please define a more specific query (max. count '5000')
					def COUNT = (Msg =~ /[0-9]+/)[0];
					def MAX = (Msg =~ /[0-9]+/)[1];
					String json = '{"status":"error","message":"too many records", "count":'+COUNT+',"max":'+MAX+'}'
					render(text: json , contentType: "text/json", encoding: "UTF-8")
				}else{
					Iterator<StatisticSearchItem> myIt = req.resultIterator();
					List<StatisticSearchItem> myList = new ArrayList<StatisticSearchItem>();
					while(myIt.hasNext()){
						myList.add(myIt.next());
					}
					
					JsonBuilder json = CommonJSONRequests.getStatisticResultListAsJSONFormat(myList);
					render(text: json , contentType: "text/json", encoding: "UTF-8")
				}
				
			}
		}

	}
}