package com.automic.actions.get

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

class AllGETActions {
	
	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def search(String version, params,Connection conn,request){return "search${version}"(params,conn)}
	
	/**
	 * @purpose search any objects (search window) against filters
	 * @return JsonBuilder object
	 * @version v1
	 */
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
	
	/**
	 * @purpose search any objects (search window) against filters
	 * @return JsonBuilder object
	 * @version v2 (vs v1: more filters)
	 */
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
							DateTime[] RESULTS = MiscUtils.HandleDateFilter(RawDate);
							req.setDateSelectionCreated(RESULTS[0], RESULTS[1]);
						}
						
						if(dispFilters.doesKeyExistInFilter("modified")){
							String RawDate = dispFilters.getValueFromKey("modified");
							DateTime[] RESULTS = MiscUtils.HandleDateFilter(RawDate);
							req.setDateSelectionModified(RESULTS[0], RESULTS[1]);
						}
						
						if(dispFilters.doesKeyExistInFilter("used")){
							String RawDate = dispFilters.getValueFromKey("used");
							DateTime[] RESULTS = MiscUtils.HandleDateFilter(RawDate);
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
}
