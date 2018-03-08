package com.automic.ae.actions.get

import java.util.HashMap;

import com.automic.DisplayFilters
import com.uc4.api.AutoForecastItem
import com.uc4.api.DateTime
import com.uc4.api.DetailGroup
import com.uc4.api.SearchResultItem
import com.uc4.api.StatisticSearchItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter
import com.uc4.api.UC4ObjectName
import com.uc4.api.UC4TimezoneName
import com.uc4.api.TaskFilter.TimeFrame
import com.uc4.api.objects.CustomAttribute
import com.uc4.api.objects.CustomAttributeFilter
import com.uc4.communication.Connection
import com.uc4.communication.requests.ActivatorStatistics;
import com.uc4.communication.requests.ActivityList
import com.uc4.communication.requests.AdoptTask
import com.uc4.communication.requests.AutoForecastRange
import com.uc4.communication.requests.CancelTask
import com.uc4.communication.requests.ChildStatistics
import com.uc4.communication.requests.DeactivateTask
import com.uc4.communication.requests.ExecuteObject
import com.uc4.communication.requests.GenericStatistics
import com.uc4.communication.requests.GetComments
import com.uc4.communication.requests.GetSessionTZ
import com.uc4.communication.requests.ModifyTaskState
import com.uc4.communication.requests.QueryAutoforecast
import com.uc4.communication.requests.QuitTask
import com.uc4.communication.requests.RecalculateAutoForecast
import com.uc4.communication.requests.RemoveJobPlanBreakPoint
import com.uc4.communication.requests.Report
import com.uc4.communication.requests.RestartTask
import com.uc4.communication.requests.ResumeTask
import com.uc4.communication.requests.RollbackTask
import com.uc4.communication.requests.SearchObject
import com.uc4.communication.requests.SuspendTask
import com.uc4.communication.requests.TaskDetails
import com.uc4.communication.requests.TaskPromptSetNames
import com.uc4.communication.requests.UnblockJobPlanTask
import com.uc4.communication.requests.UnblockWorkflow
import com.uc4.communication.requests.XMLRequest

import groovy.json.JsonBuilder

import com.automic.connection.AECredentials
import com.automic.connection.ConnectionManager
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests
import com.automic.utils.MiscUtils
import com.uc4.communication.requests.GetComments.Comment

class ActivitiesGETActions {

	
	// perhaps use GroupingType?
	
	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def search(String version, params,Connection conn,request, grailsattr){return "search${version}"(params,conn)}
	public static def deactivate(String version, params,Connection conn,request, grailsattr){return "deactivate${version}"(params,conn)}
	public static def rerun(String version, params,Connection conn,request, grailsattr){return "rerun${version}"(params,conn)}
	public static def quit(String version, params,Connection conn,request, grailsattr){return "quit${version}"(params,conn)}
	public static def unblock(String version, params,Connection conn,request, grailsattr){return "unblock${version}"(params,conn)}
	public static def cancel(String version, params,Connection conn,request, grailsattr){return "cancel${version}"(params,conn)}
	public static def resume(String version, params,Connection conn,request, grailsattr){return "resume${version}"(params,conn)}
	public static def suspend(String version, params,Connection conn,request, grailsattr){return "suspend${version}"(params,conn)}
	public static def rollback(String version, params,Connection conn,request, grailsattr){return "rollback${version}"(params,conn)}
	public static def show(String version, params,Connection conn,request, grailsattr){return "show${version}"(params,conn)}
	public static def run(String version, params,Connection conn,request, grailsattr){return "run${version}"(params,conn)}
	public static def forecast(String version, params,Connection conn,request, grailsattr){return "forecast${version}"(params,conn)}
	public static def childstats(String version, params,Connection conn,request, grailsattr){return "childstats${version}"(params,conn)}
	public static def activatorstats(String version, params,Connection conn,request, grailsattr){return "activatorstats${version}"(params,conn)}
	public static def addbreak(String version, params,Connection conn,request, grailsattr){return "changetaskstate${version}"(params,conn,1700,1562,"Task breakpoint added.")}
	// Fix - March 2017
	//public static def delbreak(String version, params,Connection conn,request, grailsattr){return "changetaskstate${version}"(params,conn,1562,1700,"Task breakpoint removed.")}
	public static def delbreak(String version, params,Connection conn,request, grailsattr){return "delbreak${version}"(params,conn)}
	public static def addskip(String version, params,Connection conn,request, grailsattr){return "changetaskstate${version}"(params,conn,1700,1922,"Task deactivated / skipped.")}
	public static def delskip(String version, params,Connection conn,request, grailsattr){return "changetaskstate${version}"(params,conn,1922,1700,"Task reactivated / unskipped.")}
	public static def go(String version, params,Connection conn,request, grailsattr){return "changetaskstate${version}"(params,conn,1700,1545,"Task Go Now Ok.")}
	public static def adopt(String version, params,Connection conn,request, grailsattr){return "adopt${version}"(params,conn)}
	

	/**
	 * @purpose del breakpoint
	 * @return JsonBuilder object
	 * @version v1
	 */
	 public static def delbreakv1(params,Connection conn) {
 
		 def AllParamMap = [:]
		 AllParamMap = [
			 'required_parameters': ['runid (format: runid= < integer >'],
			 'optional_parameters': [],
			 'optional_filters': [],
			 'required_methods': [],
			 'optional_methods': ['usage']
			 ]
 
		 String FILTERS = params.filters;
		 String METHOD = params.method;
		 
		 // Helper Methods
		 if(METHOD == "usage"){
			 JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			 return json
			 //render(text: json, contentType: "text/json", encoding: "UTF-8")
		 }else{
 
				 if(MiscUtils.checkParams(AllParamMap, params)){
					 
					 String RUNIDASSTR = params.runid;
					 int RUNID = RUNIDASSTR.toInteger();
 
					 RemoveJobPlanBreakPoint req = new RemoveJobPlanBreakPoint(RUNID);
			 
					 XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);
					 boolean ERRORFOUND=false;
					 if(req.getMessageBox()!=null && req.getMessageBox().getText().contains("error")){
						ERRORFOUND = true; 
					 }
					 if(res == null && ERRORFOUND){
						 JsonBuilder json = new JsonBuilder(
							 [
								 status: "error",
								 message: req.getMessageBox().getText(),
								 
								 msgnumber:  req.getMessageBox().getNumber().toString()
							 ])
						 return json
					 }else if(res == null && !ERRORFOUND){
						 JsonBuilder json = new JsonBuilder(
							 [
								 status: "success",
								 message: req.getMessageBox().getText(),
								 
								 msgnumber:  req.getMessageBox().getNumber().toString()
							 ])
						 return json
					 }
					 else{
						 JsonBuilder json = new JsonBuilder([status: "success", message: "Breakpoint Removed."])
						 return json
					 }
				 }else{
					  return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				  }
		 }
	 }
	 
	/**
	 * @purpose change task state
	 * @return JsonBuilder object
	 * @version v1
	 */
	 public static def changetaskstatev1(params,Connection conn, int intitialState, int targetState, String Message) {
 
		 def AllParamMap = [:]
		 AllParamMap = [
			 'required_parameters': ['runid (format: runid= < integer >'],
			 'optional_parameters': [],
			 'optional_filters': [],
			 'required_methods': [],
			 'optional_methods': ['usage']
			 ]
 
		 String FILTERS = params.filters;
		 String METHOD = params.method;
		 
		 // Helper Methods
		 if(METHOD == "usage"){
			 JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			 return json
			 //render(text: json, contentType: "text/json", encoding: "UTF-8")
		 }else{
 
				 if(MiscUtils.checkParams(AllParamMap, params)){
					 
					 String RUNIDASSTR = params.runid;
					 int RUNID = RUNIDASSTR.toInteger();
 
					 ModifyTaskState req = new ModifyTaskState(RUNID,intitialState,targetState);
			 
					 XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);
					 if(res == null){
						 JsonBuilder json = new JsonBuilder(
							 [
								 status: "error",
								 message: req.getMessageBox().getText(),
								 
								 msgnumber:  req.getMessageBox().getNumber().toString()
							 ])
						 return json
					 }else{
						 JsonBuilder json = new JsonBuilder([status: "success", message: Message])
						 return json
					 }
				 }else{
					  return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				  }
		 }
	 }
	
	/**
	 * @purpose run a given object by name
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def forecastv1(params,Connection conn){
		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': [],
			'optional_parameters': [],
			'optional_filters': [
				'name (format: filters=[name:*])',
				'login (format: filters=[login:*])',
				'host (format: filters=[host:*])',
				'srclogin (format: filters=[srclogin:*])',
				'srchost (format: filters=[srchost:*])',
				'type (format: filters=[type:JOBF|JOBS])',
				'date (format: filters=[date:YYYYMMDDHHMM-YYYYMMDDHHMM])) ',
				'platform (format: filters=[platform:WIN|UNIX])',],
			'required_methods': ['usage','recalculate', 'query'],
			'optional_methods': []
			]

		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;		
		
		DisplayFilters dispFilters = new DisplayFilters(FILTERS);
		
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
				if(MiscUtils.checkParams(AllParamMap, params)){
					
					if(METHOD != null && METHOD.equalsIgnoreCase("recalculate")){
						RecalculateAutoForecast req = new RecalculateAutoForecast();
						CommonAERequests.sendSyncRequest(conn, req, false);
						if(req.getMessageBox()!=null){
							return CommonJSONRequests.renderErrorAsJSON(req.getMessageBox().getText());
						}else{
							return CommonJSONRequests.renderOKAsJSON("AutoForecast recalculation started.");
						}
					}
	
					if(METHOD != null && METHOD.equalsIgnoreCase("query")){
											
						AutoForecastRange rangereq = new AutoForecastRange();
						CommonAERequests.sendSyncRequest(conn, rangereq, false);
							
						
						QueryAutoforecast req = new QueryAutoforecast(rangereq);
						req.setName("*")
						req.setHost("*")
						req.setSourceHost("*")
						req.setSourceLogin("*")
						req.selectPlatform(true)
						req.selectTypes(true)
						req.selectAllObjectTypes()
						req.selectAllPlatforms()
						req.setDateFrom(DateTime.now().addDays(-1))
						req.setDateTo(DateTime.now().addDays(1))
						
						if(dispFilters.doesKeyExistInFilter("name")){req.setName(dispFilters.getValueFromKey("name"));}
						if(dispFilters.doesKeyExistInFilter("login")){req.setLogin(dispFilters.getValueFromKey("login"));}
						if(dispFilters.doesKeyExistInFilter("host")){req.setHost(dispFilters.getValueFromKey("host"));}
						if(dispFilters.doesKeyExistInFilter("srclogin")){req.setSourceLogin(dispFilters.getValueFromKey("srclogin"));}
						if(dispFilters.doesKeyExistInFilter("srchost")){req.setSourceHost(dispFilters.getValueFromKey("srchost"));}
						
						if(dispFilters.doesKeyExistInFilter("date")){
							String RawDate = dispFilters.getValueFromKey("date");
							DateTime[] DTs = MiscUtils.HandleDateFilter(RawDate);
							req.setDateFrom(DTs[0]);
							req.setDateTo(DTs[1]);
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
							req.deselectAllObjectTypes()
							String AllTypesSelected = dispFilters.getValueFromKey("type");
							String[] AllTypesArray = AllTypesSelected.split("\\|");
							
							AllTypesArray.each {
									switch(it.toUpperCase()){
											case ~/C_PERIOD/:req.setTypeC_PERIOD(true);break;
											case ~/CALL|NOTIFICATION/:req.setTypeCALL(true);break;
											case ~/EVNT|EVENT/:req.setTypeEVNT(true);break;
											case ~/JOBF|MFT|FILETRANSFER|TRANSFER/:req.setTypeJOBF(true);break;
											case ~/JOBG/:req.setTypeJOBG(true);break;
											case ~/JOBP|JOBPLAN|WORKFLOW|JOBFLOW/:req.setTypeJOBP(true);break;
											case ~/JOBQ/:req.setTypeJOBQ(true);break;
											case ~/JOBS|JOB/:req.setTypeJOBS(true);break;
											case ~/JSCH|SCHEDULE|JOBSCH|SCHED/:req.setTypeJSCH(true);break;
											case ~/SCRI|SCRIPT/:req.setTypeSCRI(true);break;
										}
								}
							}

						// set res filters and parameters here
						CommonAERequests.sendSyncRequest(conn, req, false);
						
						if(req.getMessageBox()!=null){
							return CommonJSONRequests.renderErrorAsJSON(req.getMessageBox().getText());
						}else{
							// display autoforecastitems in JSON here.. depending on filters
							ArrayList<AutoForecastItem> reqList = req.iterator().toList()
		
							return new JsonBuilder(
								[
									status: "success",
									rangefrom: rangereq.calculatedFrom.toString(),
									rangeto: rangereq.calculatedTo.toString(), 
									count: reqList.size(),
									data: reqList.collect {[
										name:it.name,
										agent:it.agent,
										logicalstart:it.logicalStart.toString(),
										login:it.login,
										type:it.objectType,
										runtime:it.runtime,
										shost:it.sourceHost,
										slogin:it.sourceLogin,
										state:it.state,
										estimatedend:it.estimatedEnd.toString(),
										]}
								  ]
							)
						}
					}else{
						return CommonJSONRequests.renderErrorAsJSON("a method must be passed.");
					}
					
				}else{
					 return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				 }
		}
	}
	
	/**
	 * @purpose run a given object by name
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def runv1(params,Connection conn){
		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': ['name (format: name= < object name >'],
			'optional_parameters': [
				'start (format: start=< date-time >, date-time can be: YYYYMMDDhhmm or simply hhmm (same day))',
				'logical (format: start=< date-time >, date-time is be: YYYYMMDD)',
				'tz (format: tz=<timezone name>)',
				'manualrelease (format: manualrelease=Y)',
				'alias (format: alias=<text>)'

				],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]

		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		
		String STARTDATE = params.start;
		String LOGDATE = params.logical;
		String TZ = params.tz;
		String MANUALRELEASE = params.manual;
		String ALIAS = params.alias;
		
		
		// Helper Methods
		if(METHOD == "usage"){
			JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
				if(MiscUtils.checkParams(AllParamMap, params)){
					String OBJNAME = params.name;
					
					// Check that the parameter passed is a valid UC4ObjectName
					String IsUC4ObjectNameOk = CommonAERequests.checkUC4ObjectName(OBJNAME);
					if(!IsUC4ObjectNameOk.equals("")){
						return CommonJSONRequests.renderErrorAsJSON("Object Name is Invalid: "+IsUC4ObjectNameOk);
					}
					
					ExecuteObject req = new ExecuteObject(new UC4ObjectName(OBJNAME));
					req.allowImmediateStart(true);
					
					DateTime StartDate = DateTime.now();
					DateTime LogicalDate = DateTime.now();
					boolean ManualRelease = false;
					String Alias = null;
					
					UC4TimezoneName Tz= CommonAERequests.getSessionTZ(conn);
					boolean IsDateSpecified = false;
					
					if(STARTDATE!= null && !STARTDATE.equals("")){IsDateSpecified = true; StartDate = MiscUtils.HandleDateText(STARTDATE);}
					if(LOGDATE!= null && !LOGDATE.equals("")){LogicalDate = MiscUtils.HandleDateText(LOGDATE);}
					if(MANUALRELEASE!= null && MANUALRELEASE.toUpperCase() =~/YES|Y|O|/){ManualRelease = true;}
					if(ALIAS!= null && !ALIAS.equals("")){Alias = ALIAS;}
					if(TZ != null && !TZ.equals("")){Tz=new UC4TimezoneName(TZ);}
					
					try{
						if(IsDateSpecified){req.executeOnce(StartDate, LogicalDate, Tz, ManualRelease, null);}
						if(!IsDateSpecified){req.executeOnce(null, LogicalDate, Tz, ManualRelease, null);}
						
					}catch (IllegalArgumentException e){
						return CommonJSONRequests.renderErrorAsJSON(e.message);
					}
					
					XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);

					if(res == null){
						JsonBuilder json = new JsonBuilder(
							[
								status: "error",
								message: req.getMessageBox().getText(),
								msgnumber:  req.getMessageBox().getNumber().toString()
							])
						return json
					}else{
						int RUNID = req.getRunID();
						String MSGNUMBER = "NA";
						String MSGINSERT = "NA";
						if(req.getMessageBox() != null){
							MSGNUMBER = req.getMessageBox().getNumber().toString()
							MSGINSERT = req.getMessageBox().getInsert()
						}
						JsonBuilder json = new JsonBuilder(
							[status: "success", 
							message: "task started", 
							runid: RUNID,
							msgnumber: MSGNUMBER,
							msginsert: MSGINSERT
							])
						return json
					}
				}else{
				 	return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				 }
		}
	}
	
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
				'exname (format: filters=[exname])','exuser (format: filters=[exuser])','custom (format: filters=[custom:AttrName-AttrValue])'
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
				if(dispFilters.doesKeyExistInFilter("custom")){
					String RAWCUSTOMATTRFILTER  = dispFilters.getValueFromKey("custom");
					
					if(RAWCUSTOMATTRFILTER.contains("-") & RAWCUSTOMATTRFILTER.split("-").length == 2){
						
						String CUSTOMATTRNAME = RAWCUSTOMATTRFILTER.split("-")[0];
						String CUSTOMATTRVALUE = RAWCUSTOMATTRFILTER.split("-")[1];
//						String CUSTOMATTRVALUE="HR";
//						String CUSTOMATTRNAME = "&BusinessUnit#";

						taskFilter.removeAllCustomAttributeFilter();
						CustomAttribute custattr = new CustomAttribute(CUSTOMATTRNAME,CUSTOMATTRVALUE);
						
						CustomAttributeFilter customattrfilter =  new CustomAttributeFilter(custattr);
						//customattrfilter.addSelectedValue(CUSTOMATTRVALUE);
						//customattrfilter.setFilter(CUSTOMATTRVALUE);
						taskFilter.addCustomAttributeFilter(customattrfilter);
						
					}
				}
				
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
//				TaskDetails details = CommonAERequests.getTaskDetails(1357217,conn);
//				details.setArchiveDetail();
//				Iterator<DetailGroup> it = details.groupIterator();
//				while(it.hasNext()){
//					DetailGroup grp = it.next();
//					LinkedHashMap<String,String> hash = grp.getDetails();
//					for(int i=0;i<hash.keySet().size();i++){
//		
//						println "DEBUG:"+ hash.keySet()[i]+":"+hash.get(hash.keySet()[i]);
//		
//					}
//		
//				}
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
				
				JsonBuilder json = CommonJSONRequests.getActivityListAsJSONFormat(conn,TaskList);
				return json
		}

	}
	
	public static def activatorstatsv1(params,Connection conn) {
		
				def AllParamMap = [:]
				AllParamMap = [
					'required_parameters': ['runid (format: runid= < integer >'],
					'optional_parameters': [],
					'optional_filters': [],
					'required_methods': [],
					'optional_methods': ['usage']
					]
		
				String FILTERS = params.filters;
				String METHOD = params.method;
				
				// Helper Methods
				if(METHOD == "usage"){
					JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
					return json
					//render(text: json, contentType: "text/json", encoding: "UTF-8")
				}else{
						if(MiscUtils.checkParams(AllParamMap, params)){
							
							String RUNIDASSTR = params.runid;
							int RUNID = RUNIDASSTR.toInteger();
							ActivatorStatistics req = new ActivatorStatistics(RUNID);
							try{
								conn.sendRequestAndWait(req);
							}catch (IllegalStateException e){
								return CommonJSONRequests.renderErrorAsJSON(e.message);
							}
							
							StatisticSearchItem item = req.result;
							
							List<StatisticSearchItem> myList = new ArrayList<StatisticSearchItem>();
							myList.add(item);
							JsonBuilder json = CommonJSONRequests.getStatisticResultListAsJSONFormat(myList);
							return json
							
						}else{
							return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
						}
				}
			}
	
	public static def childstatsv1(params,Connection conn) {
		
				def AllParamMap = [:]
				AllParamMap = [
					'required_parameters': ['runid (format: runid= < integer >'],
					'optional_parameters': [],
					'optional_filters': [],
					'required_methods': [],
					'optional_methods': ['usage']
					]
		
				String FILTERS = params.filters;
				String METHOD = params.method;
				
				// Helper Methods
				if(METHOD == "usage"){
					JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
					return json
					//render(text: json, contentType: "text/json", encoding: "UTF-8")
				}else{
						if(MiscUtils.checkParams(AllParamMap, params)){
							
							String RUNIDASSTR = params.runid;
							int RUNID = RUNIDASSTR.toInteger();
							ChildStatistics req = new ChildStatistics(RUNID);
							try{
								conn.sendRequestAndWait(req);
							}catch (IllegalStateException e){
								return CommonJSONRequests.renderErrorAsJSON(e.message);
							}
							
							ArrayList<StatisticSearchItem>  myarr = req.result;

							JsonBuilder json = CommonJSONRequests.getStatisticResultListAsJSONFormat(myarr);
							return json
							
						}else{
							return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
						}
				}
			}
	 public static def showv1(params,Connection conn) {
 
		 def AllParamMap = [:]
		 AllParamMap = [
			 'required_parameters': ['runid (format: runid= < integer >'],
			 'optional_parameters': ['type (format: type= <Type of Report>) (available types: ACT, LOG, REP, LST, SREP, POST, REV0, REV1, REV2, CLNT, OBJ,SLOG, COMMENTS)'],
			 'optional_filters': [],
			 'required_methods': [],
			 'optional_methods': ['usage']
			 ]
 
		 String FILTERS = params.filters;
		 String METHOD = params.method;
		 String TYPE = params.type;
		 
		 // Helper Methods
		 if(METHOD == "usage"){
			 JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(AllParamMap);
			 return json
			 //render(text: json, contentType: "text/json", encoding: "UTF-8")
		 }else{
				 if(MiscUtils.checkParams(AllParamMap, params)){
					 
					 String RUNIDASSTR = params.runid;
					 int RUNID = RUNIDASSTR.toInteger();
					 //ACT, LOG, REP, LST, SREP, POST, REV0, REV1, REV2, CLNT, OBJ,SLOG
					 // realistically: ACT / LOG / REP / POST
					 ArrayList<String> ValidReportTypes = ["ACT", "LOG", "REP", "LST", "SREP", "POST", "REV0", "REV1", "REV2", "CLNT", "OBJ","SLOG"]
					 
					 if(TYPE != null && ValidReportTypes.contains(TYPE.toUpperCase())){
							return getReportAsJSON(TYPE.toUpperCase(),conn, RUNID);
					 }
					 else if(TYPE != null && TYPE.toUpperCase() =~ /COMMENTS|COMMENT|COMM|CMTS|CMT/){
					 	GetComments req = new GetComments(RUNID);
						 try{
							 conn.sendRequestAndWait(req);
						 }catch (IllegalStateException e){
							 return CommonJSONRequests.renderErrorAsJSON(e.message);
						 }
						
						if(req.getMessageBox()!=null){
							JsonBuilder json = new JsonBuilder(
								[
									status: "error",
									message: req.getMessageBox().getText(),
									msgnumber:  req.getMessageBox().getNumber().toString()
								])
						}else{
							ArrayList<Comment> reqList = req.iterator().toList();
							return new JsonBuilder(
								[
									status: "success",
									count: reqList.size(),
									data: reqList.collect {[
										text:it.text.toString(),
										timestamp:it.timestamp.toString(),
										user:it.user.toString()
										]}
								  ]
							)
						}

					 
					 }else{
					 	
					 return getReportAsJSON("REP",conn, RUNID);
					 }
				 }else{
				 	return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				 }
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
					if(FORCE !=null && FORCE.toUpperCase() =~/Y|YES|OK|TRUE/){
						ForceDeactivate=true;
					}
					DeactivateTask req = new DeactivateTask(RUNID,ForceDeactivate);
			
					XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);
					if(res == null){
						JsonBuilder json = new JsonBuilder(
							[
								status: "error",
								message: req.getMessageBox().getText(),
								
								msgnumber:  req.getMessageBox().getNumber().toString()
							])
						return json
					}else{
						JsonBuilder json = new JsonBuilder([status: "success", message: "task deactivated"])
						return json
					}					
				}else{
				 	return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
				 }
		}
	}
	
	/**
	 * @purpose adopt a given activity by runid
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def adoptv1(params,Connection conn){
		
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
	
				AdoptTask req = new AdoptTask(RUNID);
				XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);
				if(res == null){
					JsonBuilder json = new JsonBuilder(
						[
							status: "error",
							message: req.getMessageBox().getText(),
							
							msgnumber:  req.getMessageBox().getNumber().toString()
						])
					return json
				}else{
					JsonBuilder json = new JsonBuilder([status: "success", message: "task adopted"])
					return json
				}
			}else{
				 	return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
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
					JsonBuilder json = new JsonBuilder(
						[
							status: "error",
							message: req.getMessageBox().getText(),
							
							msgnumber:  req.getMessageBox().getNumber().toString()
						])
					return json
				}else{
					JsonBuilder json = new JsonBuilder([status: "success", message: "task rolled back"])
					return json
				}
			}else{
				 	return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
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
					JsonBuilder json = new JsonBuilder(
						[
							status: "error",
							message: req.getMessageBox().getText(),
							
							msgnumber:  req.getMessageBox().getNumber().toString()
						])
					return json
				}else{
					JsonBuilder json = new JsonBuilder([status: "success", message: "task suspended"])
					return json
				}
			}else{
				 	return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
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
					JsonBuilder json = new JsonBuilder(
						[
							status: "error",
							message: req.getMessageBox().getText(),
							
							msgnumber:  req.getMessageBox().getNumber().toString()
						])
				}else{
					JsonBuilder json = new JsonBuilder([status: "success", message: "task resumed"])
					return json
				}
			}else{
				 	return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
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
					JsonBuilder json = new JsonBuilder(
						[
							status: "error",
							message: req.getMessageBox().getText(),
							msgnumber:  req.getMessageBox().getNumber().toString()
						])
					return json
				}else{
					JsonBuilder json = new JsonBuilder([status: "success", message: "task cancelled"])
					return json
				}
			}else{
				 	return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
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
					JsonBuilder json = new JsonBuilder(
						[
							status: "error", 
							message: req.getMessageBox().getText(),
							msgnumber:  req.getMessageBox().getNumber().toString()
						])
					return json
				}else{
					JsonBuilder json = new JsonBuilder([status: "success", message: "task quit done"])
					return json
				}		
			}else{
				 	return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
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
			'optional_parameters': [
				'keepstartmode (format: keepstartmode=Y): Keep Start Mode',
				'restartaborts (format: restartaborts=Y): Only Restart Aborted Tasks / Children',
				'manualrelease (format: manualrelease=Y): Wait for Manual Release',
				'restartpoint (format: restartpoint=<String>): Restart from certain point'
				],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]

		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		
		String KEEPSTARTMODE = params.keepstartmode;
		String RESTARTABORTCHILDRENONLY = params.restartaborts;
		String MANUALRELEASE = params.manualrelease;
		String RESTARTPOINT = params.restartpoint;
		
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
					
					if(KEEPSTARTMODE != null && KEEPSTARTMODE.toUpperCase() =~/Y|T|TRUE|YES|O|ON|/){req.setKeepStartType(true);}
					if(RESTARTABORTCHILDRENONLY != null && RESTARTABORTCHILDRENONLY.toUpperCase() =~/Y|T|TRUE|YES|O|ON|/){req.setRestartAbortedChildrenOnly(true);}
					if(MANUALRELEASE != null && MANUALRELEASE.toUpperCase() =~/Y|T|TRUE|YES|O|ON|/){req.setWaitForManualRelease(true);}
					if(RESTARTPOINT != null && RESTARTPOINT.equals("")){req.setRestartPoint(RESTARTPOINT);}
					
					XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);
					
					if(res == null){
						JsonBuilder json = new JsonBuilder(
							[
								status: "error", 
								message: req.getMessageBox().getText(),
								msgnumber:  req.getMessageBox().getNumber().toString()
							])
						return json
					}else{
						JsonBuilder json = new JsonBuilder(
							[
								status: "success", 
								message: "task restarted",
								runid: req.getRestartedRunID(),
								refrunid: req.getReferenceRunID()
							])
						return json
					}
				}else{
				 	return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
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
							JsonBuilder json = new JsonBuilder(
								[
									status: "error", 
									message: req.getMessageBox().getText(),
									msgnumber:  req.getMessageBox().getNumber().toString()
								])
							return json
						}else{
							JsonBuilder json = new JsonBuilder([status: "success", message: "task unblocked"])
							return json
						}
				}
			}else{
				 	return CommonJSONRequests.renderErrorAsJSON("mandatory parameters missing.");
			}
		}
	}
	
	/**
	 * @purpose build JsonBuilder from Report content for an activity
	 * @return JsonBuilder object
	 * @version N/A
	 */
	private static def getReportAsJSON(String ReportType, Connection conn, int RUNID){
		Report req = new Report(RUNID, ReportType);
		XMLRequest res = CommonAERequests.sendSyncRequest(conn,req,false);
		
		if(req.getMessageBox()!= null){
			println "Error! " + req.getMessageBox().getText()
		}
		
		int pgnum = req.getNumberOfPages();
		
		ArrayList<String> POSTReports = new ArrayList<String>();

		int initValue = req.getCurrentPage();
		for(int i = initValue;i<=pgnum;i++){
		   POSTReports.add(req.getReport());
		   req.nextPage(i);
		   CommonAERequests.sendSyncRequest(conn,req,false);
		}

		return new JsonBuilder(
			[
				status: "success",
				data:[
					type: ReportType,
					numberofpages: req.getNumberOfPages(),
					report: POSTReports.collect {[
							"page":POSTReports.indexOf(it)+1,
							"content":it
							
						]}
				]
		   ]
		)
	}
}
