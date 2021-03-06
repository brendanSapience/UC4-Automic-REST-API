package com.automic.objects;

import groovy.json.JsonSlurper;

import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.SAXException;

import com.uc4.api.DateTime;
import com.uc4.api.InvalidUC4NameException;
import com.uc4.api.QueueStatus;
import com.uc4.api.SearchResultItem;
import com.uc4.api.StatisticSearchItem;
import com.uc4.api.Task;
import com.uc4.api.TaskFilter;
import com.uc4.api.TaskPromptSetName;
import com.uc4.api.Template;
import com.uc4.api.UC4HostName;
import com.uc4.api.UC4ObjectName;
import com.uc4.api.UC4TimezoneName;
import com.uc4.api.UC4UserName;
import com.uc4.api.objects.ConsoleEvent;
import com.uc4.api.objects.DatabaseEvent;
import com.uc4.api.objects.EstimatedRuntime;
import com.uc4.api.objects.FileEvent;
import com.uc4.api.objects.IFolder;
import com.uc4.api.objects.JobPlan;
import com.uc4.api.objects.JobPlanTask;
import com.uc4.api.objects.Notification;
import com.uc4.api.objects.PromptElement;
import com.uc4.api.objects.Queue;
import com.uc4.api.objects.TaskState;
import com.uc4.api.objects.TimeEvent;
import com.uc4.api.objects.UC4Object;
import com.uc4.api.objects.UserRight.Type;
import com.uc4.api.systemoverview.AgentListItem;
import com.uc4.api.systemoverview.QueueListItem;
import com.uc4.communication.Connection;
import com.uc4.communication.IResponseHandler;
import com.uc4.communication.TimeoutException;
import com.uc4.communication.requests.ActivityList;
import com.uc4.communication.requests.AddJobPlanTask;
import com.uc4.communication.requests.AdoptTask;
import com.uc4.communication.requests.AgentList;
import com.uc4.communication.requests.CheckAuthorizations;
import com.uc4.communication.requests.CloseObject;
import com.uc4.communication.requests.CreateObject;
import com.uc4.communication.requests.DeleteObject;
import com.uc4.communication.requests.ExportObject;
import com.uc4.communication.requests.FolderTree;
import com.uc4.communication.requests.GenericStatistics;
import com.uc4.communication.requests.GetSessionTZ;
import com.uc4.communication.requests.ImportObject;
import com.uc4.communication.requests.ModifyQueue;
import com.uc4.communication.requests.ModifyQueueStatus;
import com.uc4.communication.requests.OpenObject;
import com.uc4.communication.requests.QueueList;
import com.uc4.communication.requests.ResetOpenFlag;
import com.uc4.communication.requests.SaveObject;
import com.uc4.communication.requests.SearchObject;
import com.uc4.communication.requests.TaskDetails;
import com.uc4.communication.requests.TaskPromptSetContent;
import com.uc4.communication.requests.TaskPromptSetNames;
import com.uc4.communication.requests.TemplateList;
import com.uc4.communication.requests.VersionControlList;
import com.uc4.communication.requests.XMLRequest;

/**
 * 
 * @author bsp
 * @purpose handle common XML Requests to AE
 *
 */

public class CommonAERequests {
	
	public static ArrayList<JobPlanTask> getTasksFromNameAndJobPlan(JobPlan jobPlan, String TaskName) throws IOException{
		Iterator<JobPlanTask> it = jobPlan.taskIterator();
		ArrayList<JobPlanTask> TaskLists = new ArrayList<JobPlanTask>();
		
		while(it.hasNext()){
			JobPlanTask jpt = it.next();
			if(jpt.getTaskName().equals(TaskName)){
				TaskLists.add(jpt);
			}
		}
		return TaskLists;
	}
	
	public static TaskPromptSetNames getTaskPromptsetNames(int RunID, Connection connection) throws TimeoutException, IOException{
		TaskPromptSetNames req = new TaskPromptSetNames(RunID);
		connection.sendRequestAndWait(req);
		return req;
		
	}
	
	public static String adoptTask(int RunID, Connection connection) throws TimeoutException, IOException{
		AdoptTask adopt = new AdoptTask(RunID);
		connection.sendRequestAndWait(adopt);
		if (adopt.getMessageBox() != null) {
			return adopt.getMessageBox().getText().toString().replace("\n", "");
		}
		return null;
	}
	public static HashMap<String,HashMap<String,String>> showPromptSetContent(TaskPromptSetNames prptNames, int RunID, Connection connection) throws TimeoutException, IOException{
		// getting all prompts
		// Holder of ALL values for all prompts
		HashMap<String,HashMap<String,String>> AllPrptsValues = new HashMap<String,HashMap<String,String>>();
		
		Iterator<TaskPromptSetName> it0 = prptNames.iterator();
		while(it0.hasNext()){
			HashMap<String,String> PrptValues = new HashMap<String,String>();
			TaskPromptSetName tName = it0.next();
			//System.out.println("\t %% Promptset Found:" + tName);
			
			TaskPromptSetContent req = new TaskPromptSetContent(tName, RunID);
			
			Iterator<PromptElement> it1 = req.iterator();
			while(it1.hasNext()){
				PromptElement elmt = it1.next();
				//System.out.println("\t %% [Variable Name | Variable Value]: "+" [ "+elmt.getVariable()+" | "+elmt.getValue()+" ]");
				PrptValues.put(elmt.getVariable(),elmt.getValue());
			}
			AllPrptsValues.put(tName.getName().getName(), PrptValues);
		}
		return AllPrptsValues;
		
	}
	public static boolean addDependency(JobPlan object, JobPlanTask jpTask, String TaskPred, String Status, int TaskpredNum) throws IOException{

		// Getting the task state.. no control here :(
		TaskState tState = new TaskState(Status);
		
		// handling the case where the Task Name is END (end of WF)
		//ArrayList<JobPlanTask> TaskList = new ArrayList<JobPlanTask>();
		//if(TaskName.equalsIgnoreCase("END")){
		//	TaskList.add(object.getEndTask());
		//}else{
		//	TaskList = getTasksFromNameAndJobPlan(object,TaskName);
		//}
		
		//if(TaskList.size()==0){
		//	return false;
		//}
		// handling the case where the Predecessor Task Name is START (start of WF)
		ArrayList<JobPlanTask> PredecessorTaskList = new ArrayList<JobPlanTask>();
		if(TaskPred.equalsIgnoreCase("START")){
			PredecessorTaskList.add(object.getStartTask());
		}else{
			PredecessorTaskList = getTasksFromNameAndJobPlan(object,TaskPred);
		}
		
		if(PredecessorTaskList.size()==0){
			return false;
		}
		
		// For each task found
		//for(int h=0;h<TaskList.size();h++){
			//JobPlanTask jptask = TaskList.get(h);
			// for each predecessor found
			boolean FoundAPredecessor = false;
			for(int k=0;k<PredecessorTaskList.size();k++){
				JobPlanTask jppredecessortask = PredecessorTaskList.get(k);
				// only create a dependency if: eithere there is no task number specified or.. if they match.
				if(TaskpredNum == -1 || jppredecessortask.getLnr() == TaskpredNum){
					//System.out.println("\t ++ UPDATE: Adding Dependency to: " + jptask.getTaskName()+"("+jptask.getLnr()+")" +" on: " + jppredecessortask.getTaskName()+"("+jppredecessortask.getLnr()+")" +" in Status: " +tState+ "" );
					FoundAPredecessor = true;
					jpTask.dependencies().addDependency(jppredecessortask,tState);
				}
			}
			return FoundAPredecessor;
			
		//}

	}
	
	public static JobPlanTask getTaskFromName(String name, Connection connection) throws IOException {
		//System.out.print("Add JobPlan task "+name+" ... "); 
		AddJobPlanTask add = new AddJobPlanTask(new UC4ObjectName(name));
		connection.sendRequestAndWait(add);
		if (add.getMessageBox() != null) {
			System.out.println(" -- "+add.getMessageBox().getText().toString().replace("\n", ""));
			return null;
		}
		return add.getJobPlanTask();
	}
	
	public static JobPlanTask addTaskAtPosition(JobPlan object, String TaskName, int x, int y, Connection connection) throws IOException{
		
		//System.out.println(" ++ Adding Task: "+TaskName+" at: [" + iPosX +","+iPosY+"]" );
		JobPlanTask jptask = getTaskFromName(TaskName, connection);
		if(jptask!=null){
			jptask.setX(x);
			jptask.setY(y);
			//System.out.println("\t ++ UPDATE: Add Task to JobPlan: [ " + jptask.getTaskName() +" | " + jptask.getType() + " | {" + jptask.getX()+","+jptask.getY()+"} ]" );
			object.addTask(jptask);
			return jptask;
		}else{
			//System.out.println("DEBUG IN JHERE!!");
			return null;
		}

	}
	
	//VersionControlList imp = new VersionControlList(getUC4ObjectNameFromString(ObjName,false);
	public static void checkUserRights(String ObjName, Connection connection) throws IOException, SAXException{
		
		CheckAuthorizations.Candidate check1 = new CheckAuthorizations.Candidate(new UC4ObjectName(ObjName), Type.JOBS, 'R');
	    //CheckAuthorizations.Candidate check2 = new CheckAuthorizations.Candidate(new UC4UserName(ObjName), Type.JOBS, 'C');

	    CheckAuthorizations checkAuth = new CheckAuthorizations(check1);
	    connection.sendRequestAndWait(checkAuth);

	    for (CheckAuthorizations.Candidate c : checkAuth) {
	        System.out.println("Access mode "+c.getAccess()+" for object "+c.getName()+ (c.getResult() ? " is allowed" : " has been denied"));                      
	    }
	    
	}
	
	public static AgentListItem getAgentListItemByName(String AgentName, Connection connection) throws IOException {
		AgentList list = new AgentList();
		connection.sendRequestAndWait(list);
		if (list.getMessageBox() != null) {
			return null;
		}
		for (AgentListItem item : list) {
			if(item.getName().toString().equals(AgentName)){
			return item;
			}
		}
		return null;
	}
	
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
	
	public static String checkUC4ObjectName(String name){
		UC4ObjectName objName = null;
		try{
				objName = new UC4ObjectName(name);
			
		} catch (InvalidUC4NameException e){
			return e.getMessage();
			
		}
		return "";
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
		// Fix: ability to create RA Objects
		//Template template = convertStringToTemplate(templateName);
		Template template = getTemplateFromName(connection, templateName);
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
		public static String saveAndCloseObject(UC4Object obj,Connection connection) throws IOException, InvalidObjectException {
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
		UC4ObjectName objName = null;
		if (name.indexOf('/') != -1) objName = new UC4UserName(name);
		else if (name.indexOf('-')  != -1);
		else objName = new UC4ObjectName(name);		

		// last boolean returns an OpenObject
		OpenObject open = new OpenObject(objName,readOnly,true);
		connection.sendRequestAndWait(open);
		if (open.getMessageBox() != null) {
			System.err.println(" -- "+open.getMessageBox().toString().replace("\n", ""));
			return null;
		}
		//open.getUC4Object().getName()
		return open.getUC4Object();
	}
	public static Queue getQueue(UC4Object obj){
		return (Queue) obj;
	}
	
	public static UC4TimezoneName getSessionTZ(Connection connection) throws TimeoutException, IOException{
		GetSessionTZ reqTZ = new GetSessionTZ();
		connection.sendRequestAndWait(reqTZ);

		if (reqTZ.getMessageBox() != null) {
			
			return null;
		}
		return reqTZ.getName();
	}
	public static XMLRequest sendSyncRequest(Connection connection, XMLRequest req, boolean verbose) throws TimeoutException, IOException{

		connection.sendRequestAndWait(req);

		
		if (req.getMessageBox() != null) {
			System.out.println(" -- "+req.getMessageBox().getText().replace("\n", ""));
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
			//if(verbose){System.out.println(" -- "+req.getMessageBox().getText().toString().replace("\n", ""));}
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
	
	public static ArrayList<QueueListItem> getQueueList(Connection conn) throws IOException {		
		QueueList req = new QueueList();
		CommonAERequests.sendSyncRequest(conn, req, false);
		
		Iterator<QueueListItem> it =  req.iterator();
		ArrayList<QueueListItem> results = new ArrayList<QueueListItem>();
		while(it.hasNext()){
			QueueListItem item = it.next();
			results.add(item);
		}
	
	return results;
	}
	
	public static ArrayList<Task> getStatusFromRunid(Connection conn, int Runid, String ObjName) throws TimeoutException, IOException{
		
		TaskFilter filter = new TaskFilter();
		filter.setObjectName(ObjName);
		ActivityList r = new ActivityList(filter);

		CommonAERequests.sendSyncRequest(conn, r, false);
		
		Iterator<Task> it = r.iterator();
		ArrayList<Task> allres = new ArrayList<Task>();
		while(it.hasNext()){
			Task item = it.next();
			if(item.getRunID() == Runid){
				allres.add(item);
			}
		}

		return allres;
	}
	
	public static String ChangeQueueStatus(Connection conn, String QueueName,QueueStatus status) throws TimeoutException, IOException{
		UC4ObjectName queueName;
		try{
			queueName = new UC4ObjectName(QueueName);
		}catch (InvalidUC4NameException e){
			return "Invalid Queue Name";
		}
		ModifyQueueStatus req = new ModifyQueueStatus(queueName,status);
		
		CommonAERequests.sendSyncRequest(conn, req, false);
		if (req.getMessageBox() != null){
			return req.getMessageBox().getText();
		}else{
			return null;
		}
	}
	public static QueueListItem getQueueItem2(Connection conn, String QueueName) throws TimeoutException, IOException{
		ArrayList<QueueListItem> queueList = getQueueList(conn);
		for(int i=0;i<queueList.size();i++){
			QueueListItem queue = queueList.get(i);
			if(queue.getName().equalsIgnoreCase(QueueName)){
				return queue;
			}
		}
		return null;
	}

	public static ArrayList<Task> getActivityWindowContent(Connection conn, TaskFilter taskFilter) throws IOException {		
		ActivityList req = new ActivityList(taskFilter);
		CommonAERequests.sendSyncRequest(conn, req, false);
		
		ArrayList<Task> tasks = new ArrayList<Task>();
		for (Task t : req) {
			tasks.add(t);
		}
		return tasks;
	}
	


	public static IFolder getRootFolder(Connection conn) throws IOException{
		FolderTree tree = new FolderTree();
		conn.sendRequestAndWait(tree);
		return tree.root();		
	}
	
	public static TemplateList getTemplateList(Connection conn) throws IOException{
		IFolder RootFolder = getRootFolder(conn);
		TemplateList req = new TemplateList(RootFolder);
		conn.sendRequestAndWait(req);
		return req;
	}
	
	public static EstimatedRuntime getERT(UC4Object obj) {
		
		if(obj.getType().equalsIgnoreCase("EVNT_DB")){return ((DatabaseEvent) obj).runtime().estimatedRuntime();}
		if(obj.getType().equalsIgnoreCase("EVNT_CONS")){return ((ConsoleEvent) obj).runtime().estimatedRuntime();}
		if(obj.getType().equalsIgnoreCase("EVNT_TIME")){return ((TimeEvent) obj).runtime().estimatedRuntime();}
		if(obj.getType().equalsIgnoreCase("EVNT_FILE")){return ((FileEvent) obj).runtime().estimatedRuntime();}
		
		if(obj.getType().startsWith("CALL")){return ((Notification) obj).runtime().estimatedRuntime();}
		
		if(obj.getType().equalsIgnoreCase("JOBF")){return ((com.uc4.api.objects.FileTransfer) obj).runtime().estimatedRuntime();}
		if(obj.getType().equalsIgnoreCase("JOBP")){
			if(obj.getClass().getSimpleName().equals("WorkflowLoop")){
				return ((com.uc4.api.objects.WorkflowLoop) obj).runtime().estimatedRuntime();
			}
			if(obj.getClass().getSimpleName().equals("WorkflowIF")){
				return ((com.uc4.api.objects.WorkflowIF) obj).runtime().estimatedRuntime();
			}
			if(obj.getClass().getSimpleName().equals("JobPlan")){
				return ((com.uc4.api.objects.JobPlan) obj).runtime().estimatedRuntime();
			}
		}
		
		if(obj.getType().startsWith("JOBS")){return ((com.uc4.api.objects.Job) obj).runtime().estimatedRuntime();}
		
		if(obj.getType().startsWith("JSCH")){return ((com.uc4.api.objects.Schedule) obj).runtime().estimatedRuntime();}
		if(obj.getType().startsWith("SCRI")){return ((com.uc4.api.objects.Script) obj).runtime().estimatedRuntime();}

		return null;
	}

	public static Template getTemplateFromName(Connection conn, String TemplateName) throws IOException{

		TemplateList list = getTemplateList(conn);
		Iterator<Template> it = list.iterator();
		while(it.hasNext()){
			Template t = it.next();
		}
		Template objTemplate = list.getTemplate(TemplateName);
		return objTemplate;
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
	
	public static ArrayList<Task> getActivityWindowContentSortedByStartTime(Connection conn, TaskFilter taskFilter, boolean ReverseOrder) throws IOException {		
		ArrayList<Task> ListOfdTasks = getActivityWindowContent(conn, taskFilter);
		
		//if(ReverseOrder){Collections.sort(ListOfdTasks, new SortByStartTime());}
		//if(!ReverseOrder){Collections.sort(ListOfdTasks, new SortByStartTime().reversed());}
		
		Collections.sort(ListOfdTasks, new SortByStartTime());
		if(ReverseOrder){Collections.reverse(ListOfdTasks);}
		
		return ListOfdTasks;
	}
	
	public static ArrayList<Task> getActivityWindowContentSortedByEndTime(Connection conn, TaskFilter taskFilter, boolean ReverseOrder) throws IOException {		
		ArrayList<Task> ListOfdTasks = getActivityWindowContent(conn, taskFilter);
		
		//if(ReverseOrder){Collections.sort(ListOfdTasks, new SortByEndTime());}
		//if(!ReverseOrder){Collections.sort(ListOfdTasks, new SortByEndTime());}
		Collections.sort(ListOfdTasks, new SortByEndTime());
		if(ReverseOrder){Collections.reverse(ListOfdTasks);}
		
		return ListOfdTasks;
	}
	
	public static ArrayList<Task> getActivityWindowContentSortedByActivationTime(Connection conn, TaskFilter taskFilter, boolean ReverseOrder) throws IOException {		
		ArrayList<Task> ListOfdTasks = getActivityWindowContent(conn, taskFilter);

		Collections.sort(ListOfdTasks, new SortByActivationTime());
		if(ReverseOrder){Collections.reverse(ListOfdTasks);}
		
		
		return ListOfdTasks;
	}
}

class SortByStartTime implements Comparator<Task>{
    public int compare(Task a, Task b){
    	if(a.getStartTime() == null || b.getStartTime() == null) {
    		System.out.println("DEBUG Start Time Null!");
    	}
    	if(a.getStartTime()== null && b.getStartTime() == null){return 0;}
    	if(a.getStartTime()!= null && b.getStartTime() == null){return -1;}
    	if(a.getStartTime()== null && b.getStartTime() != null){return 1;}
    	if(a.getStartTime()!= null && b.getStartTime() != null){return a.getStartTime().compareTo(b.getStartTime());}
    	return 0;
    }
}

class SortByEndTime implements Comparator<Task>{
    public int compare(Task a, Task b){
    	if(a.getEndTime() == null || b.getEndTime() == null) {
    		System.out.println("DEBUG End Time Null!");
    	}
    	if(a.getEndTime()==null && b.getEndTime() == null){return 0;}
    	if(a.getEndTime()!=null && b.getEndTime() == null){return -1;}
    	if(a.getEndTime()==null && b.getEndTime() != null){return 1;}
    	if(a.getEndTime()!=null && b.getEndTime() != null){return a.getEndTime().compareTo(b.getEndTime());}
    	return 0;
    }
}

class SortByActivationTime implements Comparator<Task>{
    public int compare(Task a, Task b){
    	// 0 is equal
    	// -1 is a is before b
    	// +1 is b is before a
    	if(a.getActivationTime() == null || b.getActivationTime() == null) {
    		System.out.println("DEBUG Act Time Null!");
    	}
    	if(a.getActivationTime()==null && b.getActivationTime() == null){return 0;}
    	if(a.getActivationTime()!=null && b.getActivationTime() == null){return -1;}
    	if(a.getActivationTime()==null && b.getActivationTime() != null){return 1;}
    	if(a.getActivationTime()!=null && b.getActivationTime() != null){return a.getActivationTime().compareTo(b.getActivationTime());}
    	return 0;
    }
}






