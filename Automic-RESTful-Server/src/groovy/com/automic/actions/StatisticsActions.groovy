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

	
	String[] SearchAvailableVersions = ['v1']
	public static def search(String version, params,Connection conn){return "search${version}"(params,conn)}

	// Each function is versioned..
	private static def searchv1(params,Connection conn){
	
		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': [],
			'optional_parameters': [],
			'optional_filters': ['status (format: filters=[status:1900])','key1 (format: filters=[key1:*.*]','key2 (format: filters=[key2:*.*]','type (format: filters=[type:JOBF|JOBS])',
				'alias (format: filters=[alias:*])','client (format: filters=[client:200])','name (format: filters=[name:*])','queue (format: filters=[queue:*])',
				'runid (format: filters=[runid:*])','status (format: filters=[status:1900])','activation (format: filters=[activation:YYYYMMDDHHMM-YYYYMMDDHHMM])',
				'start (format: filters=[start:YYYYMMDDHHMM-YYYYMMDDHHMM])','end (format: filters=[end:YYYYMMDDHHMM-YYYYMMDDHHMM])',
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
					HandleDateFilter(req, RawDate);
				}
				
				if(dispFilters.doesKeyExistInFilter("end")){
					String RawDate = dispFilters.getValueFromKey("end");
					req.setDateSelectionEnd();
					HandleDateFilter(req, RawDate);
				}
				
				if(dispFilters.doesKeyExistInFilter("start")){
					String RawDate = dispFilters.getValueFromKey("start");
					req.setDateSelectionStart();
					HandleDateFilter(req, RawDate);
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
	
	private static def HandleDateFilter(GenericStatistics req, String RawDate){
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
			req.setFromDate(BeginDate);
			req.setToDate(EndDate);

			
			
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

			req.setFromDate(BEGINNING);
			req.setToDate(NOW);
		}

	}
	}
}