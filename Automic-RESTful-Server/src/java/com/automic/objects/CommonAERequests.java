package com.automic.objects;

import groovy.json.JsonSlurper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;




import com.uc4.ara.feature.rm.CreateDeployPackage;

import org.xml.sax.SAXException;

import com.uc4.api.InvalidUC4NameException;
import com.uc4.api.SearchResultItem;
import com.uc4.api.Task;
import com.uc4.api.TaskFilter;
import com.uc4.api.Template;
import com.uc4.api.UC4HostName;
import com.uc4.api.UC4ObjectName;
import com.uc4.api.UC4TimezoneName;
import com.uc4.api.UC4UserName;
import com.uc4.api.objects.IFolder;
import com.uc4.api.objects.UC4Object;
import com.uc4.communication.Connection;
import com.uc4.communication.IResponseHandler;
import com.uc4.communication.TimeoutException;
import com.uc4.communication.requests.ActivityList;
import com.uc4.communication.requests.CloseObject;
import com.uc4.communication.requests.CreateObject;
import com.uc4.communication.requests.DeleteObject;
import com.uc4.communication.requests.ExportObject;
import com.uc4.communication.requests.FolderTree;
import com.uc4.communication.requests.GenericStatistics;
import com.uc4.communication.requests.GetSessionTZ;
import com.uc4.communication.requests.ImportObject;
import com.uc4.communication.requests.OpenObject;
import com.uc4.communication.requests.ResetOpenFlag;
import com.uc4.communication.requests.SaveObject;
import com.uc4.communication.requests.SearchObject;
import com.uc4.communication.requests.TaskDetails;
import com.uc4.communication.requests.VersionControlList;
import com.uc4.communication.requests.XMLRequest;

/**
 * 
 * @author bsp
 * @purpose handle common XML Requests to AE
 *
 */

public class CommonAERequests {

//	public static TaskDetails getPreviousVersions(String ObjName, Connection connection) throws IOException, SAXException{
//		
//		connection.sendRequestAndWait(imp);
//		if (imp.getMessageBox() != null) {
//			System.out.println(imp.getMessageBox().getText());
//			return null;
//		}else{
//			return imp;
//		}
//	}
	
	//VersionControlList imp = new VersionControlList(getUC4ObjectNameFromString(ObjName,false);
	public static TaskDetails getTaskDetails(int runid, Connection connection) throws IOException, SAXException{
		TaskDetails imp = new TaskDetails(runid);
		connection.sendRequestAndWait(imp);
		if (imp.getMessageBox() != null) {
			System.out.println(imp.getMessageBox().getText());
			return null;
		}else{
			return imp;
		}
	}
	
	public static UC4ObjectName getUC4ObjectNameFromString(String name,boolean isTZ){
		UC4ObjectName objName = null;
		try{
			if (name.indexOf('/') != -1){
				objName = new UC4UserName(name);
			}
			else if (isTZ) {
				objName = new UC4TimezoneName(name);
			}
			else {
				objName = new UC4ObjectName(name);
			}		
		} catch (InvalidUC4NameException e){
			return null;
		}
		return objName;
	}
	
	public static String importObjects(String FilePathForImport, IFolder folder, boolean overwriteObject, boolean overwriteFolderLinks,Connection connection) throws IOException, SAXException{
		File file = new File(FilePathForImport);
		ImportObject imp = new ImportObject(file, folder, overwriteObject, overwriteFolderLinks);
		connection.sendRequestAndWait(imp);
		if (imp.getMessageBox() != null) {
			return imp.getMessageBox().getText();
		}else{
			return null;
		}
	}
	
	public static String exportObjects(File file, UC4ObjectName[] objectNames,Connection connection) throws IOException{
		//File file = new File(FilePathForExport);
		//System.out.println("DEBUG:"+objectNames.length);
		ExportObject exp = new ExportObject(objectNames,file);
		connection.sendRequestAndWait(exp);
		if (exp.getMessageBox() != null) {
			return exp.getMessageBox().toString();
		}else{
			return null;
		}
	}
	
	public static String createObject(String name, String templateName, String FolderName,Connection connection) throws IOException{

		Template template = convertStringToTemplate(templateName);
		if ( template == null){
			return " -- Error! Template Name " + templateName +" Does Not Seem To Match Any Existing Template..";
		}else{
		IFolder fold = AEFolderRequests.getFolderByFullPathName(FolderName, connection);
		return createObject(name, template, fold,connection);
		}
	}
	// Create an empty Automic Object (of any kind)
	public static String createObject(String name, Template template, IFolder fold,Connection connection) throws IOException {
		
		UC4ObjectName objName = null;
		try{
			if (name.indexOf('/') != -1){
				objName = new UC4UserName(name);
			}
			else if (template.isTimezone()) {
				objName = new UC4TimezoneName(name);
			}
			else {
				objName = new UC4ObjectName(name);
			}		
		} catch (InvalidUC4NameException e){
			return "Invalid object name given: "+e.getMessage();
		}
		CreateObject create = new CreateObject(objName,template,fold);
		connection.sendRequestAndWait(create);
		if (create.getMessageBox() != null) {
			return create.getMessageBox().getText().toString();
		}else{
			return null;
		}
	}
	
	public static String deleteObject(String name,Connection connection) throws IOException {

		UC4ObjectName objName = null;
		if (name.indexOf('/') != -1) objName = new UC4UserName(name);
		else objName = new UC4ObjectName(name);

		DeleteObject delete = new DeleteObject(objName);
		connection.sendRequestAndWait(delete);	
		if (delete.getMessageBox() != null) {
			return delete.getMessageBox().getText();
		}else{		
			return null;
		}
	}
	
	// Save an Automic Object (of any kind)
	public static String saveObject(UC4Object obj,Connection connection) throws IOException {
		//Say(" \t ++ Saving object: "+obj.getName()+"(Type: "+obj.getType()+")");
		SaveObject save = new SaveObject(obj);
		connection.sendRequestAndWait(save);
		if (save.getMessageBox() != null) {
			return "\t -- "+save.getMessageBox().getText().toString().replace("\n", "");
		}
		return null;
		
	}

	public static String reclaimObject(String ObjectName, Connection connection) throws TimeoutException, IOException{
		if(ObjectName.contains("/")){
			UC4UserName objName = new UC4UserName(ObjectName);
			ResetOpenFlag req = new ResetOpenFlag(objName);
			connection.sendRequestAndWait(req);	
			if (req.getMessageBox() != null) {
				return " -- "+req.getMessageBox().getText().toString().replace("\n", "");
			}else{
				return null;
			}
		}else{
			UC4ObjectName objName = new UC4ObjectName(ObjectName);
			ResetOpenFlag req = new ResetOpenFlag(objName);
			connection.sendRequestAndWait(req);	
			if (req.getMessageBox() != null) {
				return " -- "+req.getMessageBox().getText().toString().replace("\n", "");
			}else{
				//Say(" \t ++ Object: "+objName+" Successfully reclaimed");
				return null;
			}
		}
		
	}
	
	// close an Automic Object (of any kind)
	public static String closeObject(UC4Object obj,Connection connection) throws IOException {
		CloseObject close = new CloseObject(obj);
		connection.sendRequestAndWait(close);	
		if (close.getMessageBox() != null) {
			return " -- "+close.getMessageBox().getText().toString().replace("\n", "");
		}else{
			return null;
		}
	}	
	
	// close an Automic Object (of any kind)
		public static String saveAndCloseObject(UC4Object obj,Connection connection) throws IOException {
			SaveObject save = new SaveObject(obj);
			connection.sendRequestAndWait(save);
			if (save.getMessageBox() != null) {
			return " -- "+save.getMessageBox().getText().toString().replace("\n", "");
			}else{
				CloseObject close = new CloseObject(obj);
				connection.sendRequestAndWait(close);	
				if (close.getMessageBox() != null) {
					return "\t -- "+close.getMessageBox().getText().toString().replace("\n", "");
				}else{
					return null;
				}
			}
		}	
	public static UC4Object openObject(Connection connection, String name, boolean readOnly) throws IOException {
		//Say(" \t ++ Opening object: "+name);
		UC4ObjectName objName = null;
		if (name.indexOf('/') != -1) objName = new UC4UserName(name);
		else if (name.indexOf('-')  != -1) objName = new UC4HostName(name);
		else objName = new UC4ObjectName(name);		

		// last boolean returns an OpenObject
		OpenObject open = new OpenObject(objName,readOnly,true);
		connection.sendRequestAndWait(open);
		if (open.getMessageBox() != null) {
			System.err.println(" -- "+open.getMessageBox().toString().replace("\n", ""));
			return null;
		}
		return open.getUC4Object();
	}
	
	public static String getSessionTZ(Connection connection) throws TimeoutException, IOException{
		GetSessionTZ reqTZ = new GetSessionTZ();
		connection.sendRequestAndWait(reqTZ);

		if (reqTZ.getMessageBox() != null) {
			
			return null;
		}
		return reqTZ.getTimeZone().getDisplayName();
	}
	public static XMLRequest sendSyncRequest(Connection connection, XMLRequest req, boolean verbose) throws TimeoutException, IOException{

		connection.sendRequestAndWait(req);

		if (req.getMessageBox() != null) {
			if(verbose){System.out.println(" -- "+req.getMessageBox().getText().toString().replace("\n", ""));}
			return null;
		}
		return req;
	}
	
	public static String sendSyncRequestWithMsgReturn(Connection connection, GenericStatistics req) throws TimeoutException, IOException{
		
		connection.sendRequestAndWait(req);
		
		if (req.getMessageBox() != null) {
			return req.getMessageBox().getText().toString().replace("\n", "");
		}else{
			return "";
		}
	}
	
	public static XMLRequest sendAsyncRequest(Connection connection, XMLRequest req, IResponseHandler resHandler, boolean verbose) throws TimeoutException, IOException{
		
		connection.sendRequest(req, resHandler);
		
		if (req.getMessageBox() != null) {
			if(verbose){System.out.println(" -- "+req.getMessageBox().getText().toString().replace("\n", ""));}
			return null;
		}
		return req;
	}
	
	public static String sendAsyncRequestWithMsgReturn(Connection connection, XMLRequest req, IResponseHandler resHandler) throws TimeoutException, IOException{
		
		connection.sendRequest(req, resHandler);
		
		if (req.getMessageBox() != null) {
			return req.getMessageBox().getText().toString().replace("\n", "");
		}else{
			return "";
		}

	}
	
	// Deprecated?
	public static List<SearchResultItem> SearchJobs(Connection conn, String ObjName) throws IOException{
		SearchObject req = new SearchObject();
		req.unselectAllObjectTypes();
		req.setTypeJOBS(true);
		
		return GenericSearchObjects(conn, ObjName, req);
	}
	
	public static List<SearchResultItem> GenericSearchObjects(Connection conn, String ObjName, SearchObject req) throws IOException{
		req.setName(ObjName);
		CommonAERequests.sendSyncRequest(conn, req, false);
		
		Iterator<SearchResultItem> it =  req.resultIterator();
		List<SearchResultItem> results = new ArrayList<SearchResultItem>();
		while(it.hasNext()){
			SearchResultItem item = it.next();
			results.add(item);
		}
	return results;
	}
	
	public static List<Task> getActivityWindowContent(Connection conn, TaskFilter taskFilter) throws IOException {		
		ActivityList req = new ActivityList(taskFilter);
		CommonAERequests.sendSyncRequest(conn, req, false);
		
		List<Task> tasks = new ArrayList<Task>();
		for (Task t : req) {
			tasks.add(t);
		}
		return tasks;
	}

	// List is incomplete.. add as necessary!!
	public static Template convertStringToTemplate(String s){
		
		  if(s.equals("CALE")){return Template.CALE;}
		  if(s.equals("CALL")){return Template.CALL;}
		  if(s.equals("CALL_STANDARD")){return Template.CALL_STANDARD;}
		  if(s.equals("CALL_MAIL")){return Template.CALL_MAIL;}
		  if(s.equals("CONN_SAP")){return Template.CONN_SAP;}
		  if(s.equals("CONN_SQL")){return Template.CONN_SQL;}
		  if(s.equals("CALL_ALARM")){return Template.CALL_ALARM;}
		  if(s.equals("CODE")){return Template.CODE;}
		  if(s.equals("CPIT")){return Template.CPIT;}
		  if(s.equals("DOCU")){return Template.DOCU;}
		  if(s.equals("EVNT_CONS")){return Template.EVNT_CONS;}
		  if(s.equals("EVNT_FILE")){return Template.EVNT_FILE;}
		  if(s.equals("EVNT_TIME")){return Template.EVNT_TIME;}
		  if(s.equals("EVNT_DB")){return Template.EVNT_DB;}
		  if(s.equals("FILTER_OUTPUT")){return Template.FILTER_OUTPUT;}
		  if(s.equals("FOLD")){return Template.FOLD;}
		  if(s.equals("HSTA")){return Template.HSTA;}
		  if(s.equals("JOBF")){return Template.JOBF;}
		  if(s.equals("JOBG")){return Template.JOBG;}
		  if(s.equals("JOBI")){return Template.JOBI;}
		  if(s.equals("JOBP")){return Template.JOBP;}
		  if(s.equals("JOBQ_PS_PROCESSREQUEST")){return Template.JOBQ_PS_PROCESSREQUEST;}
		  if(s.equals("JOBQ_R3_ALL_JOBS")){return Template.JOBQ_R3_ALL_JOBS;}
		  if(s.equals("JOBQ_R3_INTERCEPTED_JOBS")){return Template.JOBQ_R3_INTERCEPTED_JOBS;}
		  if(s.equals("JOBQ_R3_JAVA_JOBS")){return Template.JOBQ_R3_JAVA_JOBS;}
		  if(s.equals("JOBS_BS2000")){return Template.JOBS_BS2000;}
		  if(s.equals("JOBS_GCOS8")){return Template.JOBS_GCOS8;}
		  if(s.equals("JOBS_JMX")){return Template.JOBS_JMX;}
		  if(s.equals("JOBS_MPE")){return Template.JOBS_MPE;}
		  if(s.equals("JOBS_NSK")){return Template.JOBS_NSK;}
		  if(s.equals("JOBS_OA")){return Template.JOBS_OA;}
		  if(s.equals("JOBS_OS390")){return Template.JOBS_OS390;}
		  if(s.equals("JOBS_OS400")){return Template.JOBS_OS400;}
		  if(s.equals("JOBS_PS")){return Template.JOBS_PS;}
		  if(s.equals("JOBS_SAP_ABAP")){return Template.JOBS_SAP_ABAP;}
		  if(s.equals("JOBS_SAP_JAVA")){return Template.JOBS_SAP_JAVA;}
		  if(s.equals("JOBS_SAP_PI")){return Template.JOBS_SAP_PI;}
		  if(s.equals("JOBS_SIEBEL")){return Template.JOBS_SIEBEL;}
		  if(s.equals("JOBS_SQL")){return Template.JOBS_SQL;}
		  if(s.equals("JOBS_UNIX")){return Template.JOBS_UNIX;}
		  if(s.equals("JOBS_VMS")){return Template.JOBS_VMS;}
		  if(s.equals("JOBS_WIN")){return Template.JOBS_WIN;}
		  if(s.equals("JOBS_GENERIC")){return Template.JOBS_GENERIC;}
		  if(s.equals("JSCH")){return Template.JSCH;}
		  if(s.equals("LOGIN")){return Template.LOGIN;}
		  if(s.equals("SCRI")){return Template.SCRI;}
		  if(s.equals("STORE")){return Template.STORE;}
		  if(s.equals("SYNC")){return Template.SYNC;}
		  if(s.equals("TZ")){return Template.TZ;}
		  if(s.equals("TZ_CET")){return Template.TZ_CET;}
		  if(s.equals("TZ_CST")){return Template.TZ_CST;}
		  if(s.equals("TZ_EST")){return Template.TZ_EST;}
		  if(s.equals("TZ_GMT")){return Template.TZ_GMT;}
		  if(s.equals("TZ_PST")){return Template.TZ_PST;}
		  if(s.equals("TZ_SYD")){return Template.TZ_SYD;}
		  if(s.equals("USER")){return Template.USER;}
		  if(s.equals("USRG")){return Template.USRG;}
		  if(s.equals("VARA")){return Template.VARA;}
		  if(s.equals("HOSTG")){return Template.HOSTG;}
		  if(s.equals("XSL")){return Template.XSL;}
		  if(s.equals("QUEUE")){return Template.QUEUE;}
		  if(s.equals("CONN_DB")){return Template.CONN_DB;}
		  if(s.equals("PROMPT_SET")){return Template.PROMPT_SET;}
		  if(s.equals("VARA_FILELIST")){return Template.VARA_FILELIST;}
		  if(s.equals("VARA_MULTI")){return Template.VARA_MULTI;}
		  if(s.equals("VARA_SQL")){return Template.VARA_SQL;}
		  if(s.equals("VARA_SQLI")){return Template.VARA_SQLI;}
		  if(s.equals("VARA_SEC_SQL")){return Template.VARA_SEC_SQL;}
		  if(s.equals("VARA_SEC_SQLI")){return Template.VARA_SEC_SQLI;}
		  if(s.equals("VARA_BACKEND")){return Template.VARA_BACKEND;}
		  if(s.equals("VARA_EXEC")){return Template.VARA_EXEC;}
		  if(s.equals("JOBP_IF")){return Template.JOBP_IF;}
		  if(s.equals("JOBP_FOREACH")){return Template.JOBP_FOREACH;}
		  if(s.equals("DASH")){return Template.DASH;}
		  if(s.equals("LOCA")){return Template.LOCA;}
		  if(s.equals("VARA_XML")){return Template.VARA_XML;}
		  if(s.equals("PERIOD")){return Template.PERIOD;}
		
		return null;
	}
	
}
