package com.automic.spec

import com.uc4.api.InvalidUC4NameException
import com.uc4.api.UC4HostName
import com.uc4.api.UC4ObjectName
import com.uc4.api.objects.CustomAttribute
import com.uc4.api.objects.Job;
import com.uc4.api.objects.JobPlan
import com.uc4.api.objects.JobPlanTask
import com.uc4.api.objects.UC4Object
import com.uc4.communication.Connection;
import com.automic.objects.CommonAERequests;
import com.automic.objects.AEFolderRequests;
import com.automic.utils.*

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

class JOBPSpecCreate {
	public static def CreateObject(Connection connection, JsonUpdates,boolean Commit){
		
		String NAME = JsonUpdates.name;
		String TEMPLATE = "JOBP";
		String FOLDER = JsonUpdates.folder;
		
		if(NAME == null || NAME.equals("")){return CommonJSONRequests.renderErrorAsJSON("template in JSON request body should contain a name")}
		//if(TEMPLATE == null || TEMPLATE.equals("")){return CommonJSONRequests.renderErrorAsJSON("template in JSON request body should contain a type")}
		if(FOLDER == null || FOLDER.equals("")){return CommonJSONRequests.renderErrorAsJSON("template in JSON request body should contain a folder")}
		
		if(CommonAERequests.convertStringToTemplate(TEMPLATE) == null){
			// return error on template passed
			return CommonJSONRequests.renderErrorAsJSON("template in JSON request body has an incorrect value")
		}
		
		if(AEFolderRequests.getFolderByFullPathName(FOLDER,connection)==null){
			// return error on folder name passed
			return CommonJSONRequests.renderErrorAsJSON("folder in JSON request body cannot be found on AE system")
		}
	
		// Creating a Blank JOBP object
		String res = CommonAERequests.createObject(NAME, TEMPLATE, FOLDER, connection)
		if(res != null){
			return CommonJSONRequests.renderErrorAsJSON(res)
		}
		
		UC4Object obj
		String r = CommonAERequests.reclaimObject(NAME,connection)
		obj = CommonAERequests.openObject(connection,NAME,false)
		
		if(obj == null){return CommonJSONRequests.renderErrorAsJSON("Object created but not updated (it couldnt be opened)")}
		
		JobPlan jplan = (JobPlan)obj;
		
		// Getting all options from JSON body
		String TITLE = JsonUpdates.title;
		String KEY1 = JsonUpdates.key1;
		String KEY2 = JsonUpdates.key2;
		String QUEUE = JsonUpdates.queue;
		String STATUS = JsonUpdates.status;
		boolean GENATRUNTIME = JsonUpdates.genatruntime;
		String PROCESS = JsonUpdates.process;
		
		// This array contains the list of all tasks
		JSONArray TaskList = JsonUpdates.tasks;

		// Adding all Tasks and Dependencies
		for(int i=0;i<TaskList.size();i++){
			
			// Each task has a name (of the original object, a position within the canvas (x,y) and a list of predecessors
			// each predecessor contains a task name, a task number and a status to be expected (ex: "ANY_OK")
			JSONObject taskJSONObj = (JSONObject) TaskList.get(i);
			String TASKNAME = taskJSONObj.name;
			int TASKX = taskJSONObj.x;
			int TASKY = taskJSONObj.y;
			// Now getting the list of predecessors for this task
			def stuff = taskJSONObj.predecessors;
			ArrayList<String> allPred = taskJSONObj.predecessors;
			JobPlanTask NewJobPlanTask;
			// If we are processing the END or START task as a main task.. we obviously need to add nothing...
			if(!TASKNAME.equalsIgnoreCase("END") && !TASKNAME.equalsIgnoreCase("START")){
				//println "Adding Task:" + TASKNAME +" at pos: ["+TASKX+","+TASKY+"]";
				// adding the task at a position on the canvas
				NewJobPlanTask = CommonAERequests.addTaskAtPosition(jplan, TASKNAME, TASKX, TASKY, connection);
				if(NewJobPlanTask == null){
					CommonAERequests.closeObject(jplan,connection)
					CommonAERequests.deleteObject(jplan.getName(), connection)
					return CommonJSONRequests.renderErrorAsJSON("Error: Task could not be found. ["+TASKNAME+"]")
				}
				// BUT we still need to move them to the right position if specified ;)
			}else{
			// Move either Start or End
				if(TASKNAME.equalsIgnoreCase("END")){
					//println "Moving End Task at pos: ["+TASKX+","+TASKY+"]";
					NewJobPlanTask = jplan.getEndTask();
					NewJobPlanTask.setX(TASKX);
					NewJobPlanTask.setY(TASKY);
				}
				if(TASKNAME.equalsIgnoreCase("START")){
					//println "Moving Start Task at pos: ["+TASKX+","+TASKY+"]";
					NewJobPlanTask = jplan.getStartTask();
					NewJobPlanTask.setX(TASKX);
					NewJobPlanTask.setY(TASKY);
				}
			}
			
			// Now, the task is on the canvas at the right position
			for(int j =0;j<allPred.size();j++){
				
				// For each predecessor..
				String Predecessor = allPred.get(j);
				JSONObject jsonbject = new JSONObject(Predecessor);
				String TASKPREDNAME = jsonbject.name;
				int TASKPREDNUM = jsonbject.tasknum;
				String TASKDEPSTATUS = jsonbject.status;
				
				// We add a dependency to the previously created
				boolean depRes = CommonAERequests.addDependency(jplan, NewJobPlanTask,TASKPREDNAME,TASKDEPSTATUS,TASKPREDNUM);
				if(!depRes){
					CommonAERequests.closeObject(jplan,connection)
					CommonAERequests.deleteObject(jplan.getName(), connection)
					return CommonJSONRequests.renderErrorAsJSON("Error: Dependency Task or Dependency Task Number could not be found. ["+TASKPREDNAME+"("+TASKPREDNUM+")"+"]")
				}
			}
		}
		
		// and now we update the rest..
		if(TITLE != null && !TITLE.equals("")){jplan.header().setTitle(TITLE)}
		if(KEY1 != null && !KEY1.equals("")){jplan.header().setArchiveKey1(KEY1)}
		if(KEY2 != null && !KEY2.equals("")){jplan.header().setArchiveKey2(KEY2)}
//		if(QUEUE != null && !QUEUE.equals("")){
//			try{jplan.attributes.setQueue(new UC4ObjectName(QUEUE))}catch(InvalidUC4NameException e){
//				CommonAERequests.closeObject(jplan,connection)
//				return CommonJSONRequests.renderErrorAsJSON(e.getMessage())}
//		}
		if(PROCESS != null && !PROCESS.equals("")){jplan.setProcess(PROCESS)}
		if(GENATRUNTIME != null){jplan.attributes().setGenerateAtRuntime(GENATRUNTIME)}
		if(STATUS != null && !STATUS.equals("")){
			if(STATUS.equals("active")){jplan.header().setActive(true)}
			if(STATUS.equals("inactive")){jplan.header().setActive(false)}
		}
		
		if(Commit){
			try{
				CommonAERequests.saveAndCloseObject(jplan,connection);
			}catch (InvalidObjectException e){
				//println "Error! " + e.getMessage()
				CommonAERequests.closeObject(jplan,connection)
				CommonAERequests.deleteObject(jplan.getName(), connection)
				return CommonJSONRequests.renderErrorAsJSON(e.getMessage())
			}
			
		}
		else{
			CommonAERequests.closeObject(jplan,connection)
			CommonAERequests.deleteObject(jplan.getName(), connection)
		}
		
		if(Commit){return CommonJSONRequests.renderOKAsJSON("Object created successfully",Commit)}
		if(!Commit){return CommonJSONRequests.renderOKAsJSON("Object created simulated successfully",Commit)}
	}
		
		
}
