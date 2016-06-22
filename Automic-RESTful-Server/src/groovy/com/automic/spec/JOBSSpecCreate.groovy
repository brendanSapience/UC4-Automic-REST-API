package com.automic.spec

import com.uc4.api.InvalidUC4NameException
import com.uc4.api.UC4HostName
import com.uc4.api.UC4ObjectName
import com.uc4.api.objects.Job;
import com.uc4.api.objects.UC4Object
import com.uc4.communication.Connection;
import com.automic.objects.CommonAERequests;
import com.automic.objects.AEFolderRequests;
import com.automic.utils.*

class JOBSSpecCreate {
	public static def CreateObject(Connection connection, JsonUpdates,boolean Commit){
		
		String NAME = JsonUpdates.name;
		String TEMPLATE = JsonUpdates.type;
		String FOLDER = JsonUpdates.folder;
		
		if(NAME == null || NAME.equals("")){return CommonJSONRequests.renderErrorAsJSON("template in JSON request body should contain a name")}
		if(TEMPLATE == null || TEMPLATE.equals("")){return CommonJSONRequests.renderErrorAsJSON("template in JSON request body should contain a type")}
		if(FOLDER == null || FOLDER.equals("")){return CommonJSONRequests.renderErrorAsJSON("template in JSON request body should contain a folder")}
		
		if(CommonAERequests.convertStringToTemplate(TEMPLATE) == null){
			// return error on template passed
			return CommonJSONRequests.renderErrorAsJSON("template in JSON request body has an incorrect value")
			
		}
		if(AEFolderRequests.getFolderByFullPathName(FOLDER,connection)==null){
			// return error on folder name passed
			return CommonJSONRequests.renderErrorAsJSON("folder in JSON request body cannot be found on AE system")
			
		}
	
		String res = CommonAERequests.createObject(NAME, TEMPLATE, FOLDER, connection)
		 // return error on creation if res != null
		if(res != null){
			return CommonJSONRequests.renderErrorAsJSON(res)
		}
		
		UC4Object obj
	//	if(Commit){
			String r = CommonAERequests.reclaimObject(NAME,connection)
			obj = CommonAERequests.openObject(connection,NAME,false)
//		}else{
//			obj = CommonAERequests.openObject(connection,NAME,true)
//		}
		
		if(obj == null){return CommonJSONRequests.renderErrorAsJSON("Object created but not updated (it couldnt be opened)")}
		
		Job job = (Job)obj;
		
		String TITLE = JsonUpdates.title;
		String KEY1 = JsonUpdates.key1;
		String KEY2 = JsonUpdates.key2;
		String HOST = JsonUpdates.host;
		String LOGIN = JsonUpdates.login;
		String QUEUE = JsonUpdates.queue;
		String STATUS = JsonUpdates.status;
		boolean GENATRUNTIME = JsonUpdates.genatruntime;
		String PROCESS = JsonUpdates.process;
		String PREPROCESS = JsonUpdates.preprocess;
		String POSTPROCESS = JsonUpdates.postprocess;

		if(TITLE != null && !TITLE.equals("")){job.header().setTitle(TITLE)}
		if(KEY1 != null && !KEY1.equals("")){job.header().setArchiveKey1(KEY1)}
		if(KEY2 != null && !KEY2.equals("")){job.header().setArchiveKey2(KEY2)}
		if(HOST != null && !HOST.equals("")){
			try{job.attributes.setHost(new UC4HostName(HOST))}catch(InvalidUC4NameException e){
				CommonAERequests.closeObject(job,connection)
				return CommonJSONRequests.renderErrorAsJSON(e.getMessage())}
		}
		if(LOGIN != null && !LOGIN.equals("")){
			try{job.attributes.setLogin(new UC4ObjectName(LOGIN))}catch(InvalidUC4NameException e){
				CommonAERequests.closeObject(job,connection)
				return CommonJSONRequests.renderErrorAsJSON(e.getMessage())}	
		}
		if(QUEUE != null && !QUEUE.equals("")){
			try{job.attributes.setQueue(new UC4ObjectName(QUEUE))}catch(InvalidUC4NameException e){
				CommonAERequests.closeObject(job,connection)
				return CommonJSONRequests.renderErrorAsJSON(e.getMessage())}
		}
		if(PROCESS != null && !PROCESS.equals("")){job.setProcess(PROCESS)}
		if(PREPROCESS != null && !PREPROCESS.equals("")){job.setPreProcess(PREPROCESS)}
		if(POSTPROCESS != null && !POSTPROCESS.equals("")){job.setPostProcess(POSTPROCESS)}
		if(GENATRUNTIME != null){job.attributes().setGenerateAtRuntime(GENATRUNTIME)}
		if(STATUS != null && !STATUS.equals("")){
			if(STATUS.equals("active")){job.header().setActive(true)}
			if(STATUS.equals("inactive")){job.header().setActive(false)}
		}
		
		//if(Commit){
			//println "Saving Object: " + it.getName()
			CommonAERequests.saveAndCloseObject(job,connection)
			//}
	//	else{CommonAERequests.closeObject(job,connection)}
		
		return CommonJSONRequests.renderOKAsJSON("Object created successfully")

	}
		
		
}
