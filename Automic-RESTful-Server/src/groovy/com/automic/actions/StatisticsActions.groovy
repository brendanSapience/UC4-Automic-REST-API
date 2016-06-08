package com.automic.actions

import com.uc4.communication.Connection;
import com.uc4.api.DateTime
import com.uc4.api.SearchResultItem
import com.uc4.api.StatisticSearchItem
import com.uc4.communication.Connection;
import com.uc4.communication.requests.GenericStatistics
import com.uc4.communication.requests.SearchObject

import groovy.json.JsonBuilder

import com.automic.DisplayFilters
import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests;
import com.automic.utils.MiscUtils;

class StatisticsActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def search(String version, params,Connection conn){return "search${version}"(params,conn)}

	
	/**
	 * @purpose search statistics (Period window) against filters
	 * @return JsonBuilder object
	 * @version v1
	 */
	private static def searchv1(params,Connection conn){
	
		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': [],
			'optional_parameters': [],
			'optional_filters': [
				'status (format: filters=[status:1900])',
				'key1 (format: filters=[key1:*.*]',
				'key2 (format: filters=[key2:*.*]',
				'type (format: filters=[type:JOBF|JOBS])',
				'alias (format: filters=[alias:*])',
				'client (format: filters=[client:200])',
				'name (format: filters=[name:*])',
				'queue (format: filters=[queue:*])',
				'runid (format: filters=[runid:*])',
				'status (format: filters=[status:1900])',
				'activation (format: filters=[activation:YYYYMMDDHHMM-YYYYMMDDHHMM]), or filters=[activation:LAST4DAYS] (DAYS can be substituted with: SECS, MINS, HOURS, DAYS, MONTHS, YEARS) ',
				'start (format: filters=[start:YYYYMMDDHHMM-YYYYMMDDHHMM]), or filters=[start:LAST4DAYS] (DAYS can be substituted with: SECS, MINS, HOURS, DAYS, MONTHS, YEARS) ',
				'end (format: filters=[end:YYYYMMDDHHMM-YYYYMMDDHHMM]), or filters=[end:LAST4DAYS] (DAYS can be substituted with: SECS, MINS, HOURS, DAYS, MONTHS, YEARS) ',
				'platform (format: filters=[platform:WIN|UNIX])',
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
			return json
		}else{
			
				DisplayFilters dispFilters = new DisplayFilters(FILTERS);
				
				GenericStatistics req = new GenericStatistics();
				req.selectAllPlatforms();
				req.selectAllTypes();
				req.setDateSelectionNone();
				
				if(dispFilters.doesKeyExistInFilter("key1")){req.setArchiveKey1(dispFilters.getValueFromKey("key1"));}
				if(dispFilters.doesKeyExistInFilter("key2")){req.setArchiveKey2(dispFilters.getValueFromKey("key2"));}
				if(dispFilters.doesKeyExistInFilter("alias")){req.setAlias(dispFilters.getValueFromKey("alias"));}
				if(dispFilters.doesKeyExistInFilter("client")){req.setClient(dispFilters.getValueFromKey("client"));}
				if(dispFilters.doesKeyExistInFilter("name")){req.setObjectName(dispFilters.getValueFromKey("name"));}
				if(dispFilters.doesKeyExistInFilter("queue")){req.setQueue(dispFilters.getValueFromKey("queue"));}
				if(dispFilters.doesKeyExistInFilter("runid")){req.setRunID(dispFilters.getValueFromKey("runid"));}
				if(dispFilters.doesKeyExistInFilter("status")){req.setStatus(dispFilters.getValueFromKey("status"));}
				
				if(dispFilters.doesKeyExistInFilter("activation")){
					String RawDate = dispFilters.getValueFromKey("activation");
					req.setDateSelectionActivation();
					//HandleDateFilter(req, RawDate);
					DateTime[] DTs = MiscUtils.HandleDateFilter(RawDate);
					req.setFromDate(DTs[0]);
					req.setToDate(DTs[1]);
				}
				
				if(dispFilters.doesKeyExistInFilter("end")){
					String RawDate = dispFilters.getValueFromKey("end");
					req.setDateSelectionEnd();
					//HandleDateFilter(req, RawDate);
					DateTime[] DTs = MiscUtils.HandleDateFilter(RawDate);
					req.setFromDate(DTs[0]);
					req.setToDate(DTs[1]);
				}
				
				if(dispFilters.doesKeyExistInFilter("start")){
					String RawDate = dispFilters.getValueFromKey("start");
					req.setDateSelectionStart();
					//HandleDateFilter(req, RawDate);
					DateTime[] DTs = MiscUtils.HandleDateFilter(RawDate);
					req.setFromDate(DTs[0]);
					req.setToDate(DTs[1]);
				}
				
				// filters=[platform:UNIX|WIN|CIT]
				if(dispFilters.doesKeyExistInFilter("platform")){
					req.unselectAllPlatforms();
					String AllPlatformSelected = dispFilters.getValueFromKey("platform");
					String[] AllPlatformsArray = AllPlatformSelected.split("\\|");
					
					AllPlatformsArray.each {
							switch(it.toUpperCase()){
								case ~/WIN|WINDOWS/:req.setPlatformWindows(true);break;
								case ~/UNX|UNIX|LINUX|REDHAT|UBUNTU|SOLARIS|UX/:req.setPlatformUNIX(true);break;
								case ~/BS2000|BS/:req.setPlatformBS2000(true);break;
								case ~/CIT|RA/:req.setPlatformCIT(true);break;
								case ~/GCO|GCOS8/:req.setPlatformGCOS8(true);break;
								case ~/JMX/:req.setPlatformJMX(true);break;
								case ~/MAIL/:req.setPlatformMAIL(true);break;
								case ~/MPE/:req.setPlatformMPE(true);break;
								case ~/MVS/:req.setPlatformMVS(true);break;
								case ~/NSK/:req.setPlatformNSK(true);break;
								case ~/OA/:req.setPlatformOA(true);break;
								case ~/OS400|AS400/:req.setPlatformOS400(true);break;
								case ~/PS/:req.setPlatformPS(true);break;
								case ~/SIEBEL|SEIBEL/:req.setPlatformSiebel(true);break;
								case ~/R3/:req.setPlatformR3(true);break;
								case ~/SQL/:req.setPlatformSQL(true);break;
								case ~/VMS/:req.setPlatformVMS(true);break;
								}
						}
					}
				// filters=[type:JOBS|JOBF]
				if(dispFilters.doesKeyExistInFilter("type")){
					req.unselectAllTypes();
					String AllTypesSelected = dispFilters.getValueFromKey("type");
					String[] AllTypesArray = AllTypesSelected.split("\\|");
					
					AllTypesArray.each {
							switch(it.toUpperCase()){
									case ~/API/:req.setTypeAPI(true);break;
									case ~/C_HOSTG/:req.setTypeC_HOSTG(true);break;
									case ~/C_PERIOD/:req.setTypeC_PERIOD(true);break;
									case ~/CALL|NOTIFICATION/:req.setTypeCALL(true);break;
									case ~/CPIT|COCKPIT/:req.setTypeCPIT(true);break;
									case ~/CLNT|CLIENT/:req.setTypeCLNT(true);break;
									case ~/EVNT|EVENT/:req.setTypeEVNT(true);break;
									case ~/EVNT_CHILD|EVENT_CHILD|EVNTCHILD|EVENTCHILD/:req.setTypeEVENT_CHILD(true);break;
									case ~/HOST|AGENT|NODE/:req.setTypeHOST(true);break;
									case ~/JOBD/:req.setTypeJOBD(true);break;
									case ~/JOBF|MFT|FILETRANSFER|TRANSFER/:req.setTypeJOBF(true);break;
									case ~/JOBG/:req.setTypeJOBG(true);break;
									case ~/JOBP|JOBPLAN|WORKFLOW|JOBFLOW/:req.setTypeJOBP(true);break;
									case ~/JOBQ/:req.setTypeJOBQ(true);break;
									case ~/JOBS|JOB/:req.setTypeJOBS(true);break;
									case ~/JSCH|SCHEDULE|JOBSCH|SCHED/:req.setTypeJSCH(true);break;
									case ~/REPORT|REP/:req.setTypeREPORT(true);break;
									case ~/SCRI|SCRIPT/:req.setTypeSCRI(true);break;
									case ~/SERV/:req.setTypeSERV(true);break;
									case ~/SYNC/:req.setTypeSYNC(true);break;
									case ~/USER/:req.setTypeUSER(true);break;
								}
						}
					}
				
				String Msg = CommonAERequests.sendSyncRequestWithMsgReturn(conn,req);

				if(Msg.contains("too many")){
					//Your selection results in too many statistics (count '46635'). Please define a more specific query (max. count '5000')
					def COUNT = (Msg =~ /[0-9]+/)[0];
					def MAX = (Msg =~ /[0-9]+/)[1];
					//String json = '{"status":"error","message":"too many records", "count":'+COUNT+',"max":'+MAX+'}'
					JsonBuilder json = new JsonBuilder([status: "error", message: "too many records", "count": COUNT, "max": MAX])
					return json
				}else{
					Iterator<StatisticSearchItem> myIt = req.resultIterator();
					if(myIt != null){
						List<StatisticSearchItem> myList = new ArrayList<StatisticSearchItem>();
						while(myIt.hasNext()){
							myList.add(myIt.next());
						}
						
						JsonBuilder json = CommonJSONRequests.getStatisticResultListAsJSONFormat(myList);
						return json
					}else{
						JsonBuilder json = new JsonBuilder([status: "error", message: "No Data Returned by AE"])
						return json
					}
		
				}
		}

	}
}
