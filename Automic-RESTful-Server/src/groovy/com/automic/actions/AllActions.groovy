package com.automic.actions

import com.uc4.communication.Connection;
import com.uc4.api.DateTime
import com.uc4.api.SearchResultItem
import com.uc4.communication.Connection;
import com.uc4.communication.requests.GenericStatistics;
import com.uc4.communication.requests.SearchObject

import groovy.json.JsonBuilder

import com.automic.DisplayFilters
import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests;
import com.automic.utils.MiscUtils;

class AllActions {
	
	public static def search(String version, params,Connection conn){return "search${version}"(params,conn)}
	
	public static def searchv1(params,Connection conn) {
		
			def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name= < UC4RegEx > )'],
				'optional_parameters': ['search_usage (format: search_usage=Y)'],
				'optional_filters': [],
				'required_methods': [],
				'optional_methods': ['usage']
				]
			
			String FILTERS = params.filters;
			String TOKEN = params.token;
			String METHOD = params.method;
			String SEARCHUSAGE = params.search_usage;
			
			if(METHOD == "usage"){
				JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
				return json
			}else{
					// check mandatory stuff here
					if(MiscUtils.checkParams(SupportedThings, params)){
						
						//DisplayFilters dispFilters = new DisplayFilters(FILTERS);
						
						SearchObject req = new SearchObject();
						req.selectAllObjectTypes();

						if(SEARCHUSAGE!=null && SEARCHUSAGE.toUpperCase() =~/Y|YES|OK|O/){req.setSearchUseOfObjects(true);}
						
						List<SearchResultItem> JobList = CommonAERequests.GenericSearchObjects(conn, params.name, req);
						
						JsonBuilder json = CommonJSONRequests.getResultListAsJSONFormat(JobList);
						return json
						
					}else{
						JsonBuilder json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
						
						return json
					}
				
			}
		}
	
	public static def searchv2(params,Connection conn) {
		
			def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['name (format: name= < UC4RegEx > )'],
				'optional_parameters': [
					'search_usage (format: search_usage=Y)',
					'text (format: text=< UC4RegEx > )',
					'search_text_in_process (format: search_text_in_process) -> searches for pattern specified by text= in object process tabs (pre/process/post)',
					'search_text_in_title (format: search_text_in_title) -> searches for pattern specified by text= in object title',
					'search_text_in_keys (format: search_text_in_keys) -> searches for pattern specified by text= in object archive keys 1 & 2',
					'search_text_in_docu (format: search_text_in_docu) -> searches for pattern specified by text= in object DOCU tab',
					],
				'optional_filters': [
					'type (format: filters=[type:JOBF|JOBS]) Available Types are: CALE,PERIOD,CALL,CITC,CLNT,CODE,CONN,CPIT,DOCU,DASH,EVNT,FILTER,FOLD,HOST,HOSTG,HSTA,JOBI,LOGIN,PERIOD,PRPT,QUEUE,SERV,STORE,SYNC,TZ,USER,USRG,VARA,XSL',
					'reversetype (format: filters=[type:JOBS,reversetype]) => reverse selection of types (= "all but types selected")',
					'created (format: filters=[created:YYYYMMDDHHMM-YYYYMMDDHHMM], or filters=[created:LAST4DAYS] (DAYS can be substituted with: SECS, MINS, HOURS, DAYS, MONTHS, YEARS) ',
					'modified (format: filters=[modified:YYYYMMDDHHMM-YYYYMMDDHHMM], or filters=[created:LAST4DAYS] (DAYS can be substituted with: SECS, MINS, HOURS, DAYS, MONTHS, YEARS) ',
					'used (format: filters=[used:YYYYMMDDHHMM-YYYYMMDDHHMM], or filters=[created:LAST4DAYS] (DAYS can be substituted with: SECS, MINS, HOURS, DAYS, MONTHS, YEARS) '
					
				],
				'required_methods': [],
				'optional_methods': ['usage']
				]
			
			String FILTERS = params.filters;
			String TOKEN = params.token;
			String METHOD = params.method;
			String SEARCHUSAGE = params.search_usage;
			
			String SEARCHTEXT = params.text;
			String TEXTPROCESS = params.search_text_in_process;
			String TEXTTITLE = params.search_text_in_title;
			String TEXTKEYS = params.search_text_in_keys;
			String TEXTDOCU = params.search_text_in_docu;
			
			if(METHOD == "usage"){
				JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
				return json
			}else{
					// check mandatory stuff here
					if(MiscUtils.checkParams(SupportedThings, params)){

						DisplayFilters dispFilters = new DisplayFilters(FILTERS);
						
						SearchObject req = new SearchObject();
						req.selectAllObjectTypes();
						req.setIncludeLinks(true)
						
						if(SEARCHTEXT != null && !SEARCHTEXT.equals("")){
							boolean titlesearch = false;
							boolean keysearch = false;
							boolean processsearch = false;
							boolean docusearch = false;
							
							if(TEXTTITLE!=null){titlesearch = true}
							if(TEXTKEYS!=null){keysearch = true}
							if(TEXTPROCESS!=null){processsearch = true}
							if(TEXTDOCU!=null){docusearch = true}
							
							//setTextSearch(java.lang.String pattern, boolean process, boolean documentation, boolean objectTitle, boolean archiveKeys)
							req.setTextSearch(SEARCHTEXT, processsearch, docusearch, titlesearch, keysearch)
						}
						
						if(dispFilters.doesKeyExistInFilter("created")){
							String RawDate = dispFilters.getValueFromKey("created");
							DateTime[] RESULTS = HandleDateFilter(req, RawDate);
							req.setDateSelectionCreated(RESULTS[0], RESULTS[1]);
						}
						
						if(dispFilters.doesKeyExistInFilter("modified")){
							String RawDate = dispFilters.getValueFromKey("modified");
							DateTime[] RESULTS = HandleDateFilter(req, RawDate);
							req.setDateSelectionModified(RESULTS[0], RESULTS[1]);
						}
						
						if(dispFilters.doesKeyExistInFilter("used")){
							String RawDate = dispFilters.getValueFromKey("used");
							DateTime[] RESULTS = HandleDateFilter(req, RawDate);
							req.setDateSelectionUsed(RESULTS[0], RESULTS[1]);
						}
						
						if(dispFilters.doesKeyExistInFilter("type")){
		
							// if type os a filter.. by default we select nothing and only add what is in the filter..
							boolean SelectIsStandard = true;
							req.unselectAllObjectTypes();
							
							// UNLESS the reversetype filter is found, in which case we select everything and only unselect what is found in filter
							if(dispFilters.doesKeyExistInFilter("reversetype")){
								SelectIsStandard = false;
								req.selectAllObjectTypes();
							}

							String AllTypesSelected = dispFilters.getValueFromKey("type");
							String[] AllTypessArray = AllTypesSelected.split("\\|");
							AllTypessArray.each{
								switch(it.toUpperCase()) {
									case ~/CALE|CALENDAR/:req.setTypeCALE(SelectIsStandard);break;
									case ~/PERIOD/:req.setTypePERIOD(SelectIsStandard);break;
									case ~/CALL/:req.setTypeCALL(SelectIsStandard);break;	
									case ~/CITC|CIT|RA/:req.setTypeCITC(SelectIsStandard);break;
									case ~/CLNT|CLIENT/:req.setTypeCLNT(SelectIsStandard);break;
									case ~/CODE|CODE/:req.setTypeCODE(SelectIsStandard);break;
									case ~/CONN|CONNECTION/:req.setTypeCONN(SelectIsStandard);break;
									case ~/CPIT|COCKPIT/:req.setTypeCPIT(SelectIsStandard);break;									
									case ~/DOCU|DOCUMENT/:req.setTypeDOCU(SelectIsStandard);break;
									case ~/DASH|DASHBOARD/:req.setTypeDASH(SelectIsStandard);break;
									case ~/EVNT|EVENT/:req.setTypeEVNT(SelectIsStandard);break;
									case ~/FILTER|FILT/:req.setTypeFILTER(SelectIsStandard);break;
									case ~/FOLDER|FOLD/:req.setTypeFOLD(SelectIsStandard);break;
									case ~/HOST|AGENT|NODE/:req.setTypeHOST(SelectIsStandard);break;
									case ~/HOSTG|AGENTG|NODEG|AGTGRP|AGENTGROUP|HOSTGROUP|HOSTGRP/:req.setTypeHOSTG(SelectIsStandard);break;
									case ~/HSTA/:req.setTypeHSTA(SelectIsStandard);break;
									case ~/JOBI/:req.setTypeJOBI(SelectIsStandard);break;
									case ~/LOGIN/:req.setTypeLOGIN(SelectIsStandard);break;
									case ~/PERIOD/:req.setTypePERIOD(SelectIsStandard);break;
									case ~/PRPT|PROMPT|PRPTSET|PROMPSET|PROMPTSETS/:req.setTypePRPT(SelectIsStandard);break;
									case ~/QUEUE|QUEUES/:req.setTypeQUEUE(SelectIsStandard);break;
									case ~/SERV/:req.setTypeSERV(SelectIsStandard);break;
									case ~/STORE|STOR|STORES/:req.setTypeSTORE(SelectIsStandard);break;
									case ~/SYNC/:req.setTypeSYNC(SelectIsStandard);break;
									case ~/TZ|TIMEZONE|TIMEZONES/:req.setTypeTZ(SelectIsStandard);break;
									case ~/USER|USR/:req.setTypeUSER(SelectIsStandard);break;
									case ~/USRG|USERG|USERGROUP|USERGRP|USERGROUPS/:req.setTypeUSRG(SelectIsStandard);break;
									case ~/VARA|VARAS/:req.setTypeVARA(SelectIsStandard);break;
									case ~/XSL/:req.setTypeXSL(SelectIsStandard);break;
									case ~/JOBF|MFT|FILETRANSFER|TRANSFER/:req.setTypeJOBF(SelectIsStandard);break;
									case ~/JOBG/:req.setTypeJOBG(true);break;
									case ~/JOBP|JOBPLAN|WORKFLOW|JOBFLOW/:req.setTypeJOBP(SelectIsStandard);break;
									case ~/JOBQ/:req.setTypeJOBQ(SelectIsStandard);break;
									case ~/JOBS|JOB/:req.setTypeJOBS(SelectIsStandard);break;
									case ~/JSCH|SCHEDULE|JOBSCH|SCHED/:req.setTypeJSCH(SelectIsStandard);break;
									case ~/SCRI|SCRIPT/:req.setTypeSCRI(SelectIsStandard);break;
									case ~/EXECUTABLE/:if (SelectIsStandard) {req.setTypeExecuteable()};break; // this one should be used by itself.. need to be the last check
								}
							}
						}
						
						if(SEARCHUSAGE!=null && SEARCHUSAGE.toUpperCase() =~/Y|YES|OK|O/){req.setSearchUseOfObjects(true);}
						
						List<SearchResultItem> JobList = CommonAERequests.GenericSearchObjects(conn, params.name, req);
						
						JsonBuilder json = CommonJSONRequests.getResultListAsJSONFormat(JobList);
						return json
						
					}else{
						JsonBuilder json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
						
						return json
					}
				
			}
		}
	
	private static def DateTime[] HandleDateFilter(SearchObject req, String RawDate){
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

			//req.setDateSelectionCreated(BeginDate, EndDate)
//			def RespArr = new DateTime[2]
//			RespArr[0] = BeginDate
//			RespArr[1] = EndDate
//			return RespArr
			return [BeginDate,EndDate]
		}else{ //LASTNHOURS / LASTNMINUTES / LASTNDAYS |  type
			
		if(RawDate.toUpperCase() =~ /LAST[0-9]+(YEARS|YEAR|YR|Y|MONTHS|MONTH|MTH|DAYS|DAY|D|HOURS|HR|HOUR|H|MIN|MINUTE|MINUTES|SECONDS|SECOND|SEC|S|)/){
			
			String NumberOfUnits = RawDate.findAll( /\d+/ )[0]

			int Number = NumberOfUnits.toInteger()
			String Type = RawDate.split(NumberOfUnits)[1]
			
			DateTime NOW = DateTime.now()
			DateTime BEGINNING = DateTime.now()
			if(Type.toUpperCase() =~/YEARS|YEAR|YR/){
				BEGINNING.addYears(-Number)
			}
			if(Type.toUpperCase() =~/MONTHS|MONTH|MTH/){
				BEGINNING.addMonth(-Number)
			}
			if(Type.toUpperCase() =~/DAY|DAYS/){
				BEGINNING.addDays(-Number)
			}
			if(Type.toUpperCase() =~/HOURS|HOUR|HR/){
				BEGINNING.addMinutes(-60*Number)
			}
			if(Type.toUpperCase() =~/MIN|MINUTE|MINUTES/){
				BEGINNING.addMinutes(-Number)
			}
			if(Type.toUpperCase() =~/SECONDS|SECOND|SEC/){
				BEGINNING.addSeconds(-Number)
			}

			return [BEGINNING,NOW]
//			def RespArr = new DateTime[2]
//			RespArr[0] = BEGINNING
//			RespArr[1] = NOW
//			return RespArr
		}

	}
	}
}
