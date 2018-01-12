package com.automic.spec

import com.uc4.api.InvalidUC4NameException
import com.uc4.api.Template
import com.uc4.api.UC4HostName
import com.uc4.api.UC4ObjectName
import com.uc4.api.objects.AttributesSQL
import com.uc4.api.objects.CustomAttribute
import com.uc4.api.objects.Job;
import com.uc4.api.objects.UC4Object
import com.uc4.api.objects.OCVPanel.CITValue
import com.uc4.communication.Connection;
import com.automic.objects.CommonAERequests;
import com.automic.objects.AEFolderRequests;
import com.automic.utils.*
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

class JOBSSpecCreate {
	public static def CreateObject(Connection connection, JsonUpdates,boolean Commit){
		
		String NAME = JsonUpdates.name;
		String TEMPLATE = JsonUpdates.type;
		String FOLDER = JsonUpdates.folder;
		
		if(NAME == null || NAME.equals("")){return CommonJSONRequests.renderErrorAsJSON("template in JSON request body should contain a name")}
		if(TEMPLATE == null || TEMPLATE.equals("")){return CommonJSONRequests.renderErrorAsJSON("template in JSON request body should contain a type")}
		if(FOLDER == null || FOLDER.equals("")){return CommonJSONRequests.renderErrorAsJSON("template in JSON request body should contain a folder")}
		
		
		Template TemplateObj = CommonAERequests.getTemplateFromName(connection,TEMPLATE);
		if(TemplateObj == null){
			// return error on template passed
			return CommonJSONRequests.renderErrorAsJSON("template in JSON request body could not be found in target environment")
			
		}
		TEMPLATE = TemplateObj.getTemplateName();
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

		String r = CommonAERequests.reclaimObject(NAME,connection)
		obj = CommonAERequests.openObject(connection,NAME,false)

		
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
		// For SQL Jobs Specifically
		if(job.getType().equals("JOBS_SQL")){
			AttributesSQL attr = (AttributesSQL) job.hostAttributes();
			String CONNNAME = JsonUpdates.db_connname;
			String DBNAME = JsonUpdates.db_dbname;
			String SERVERNAME = JsonUpdates.db_servername;
			if(CONNNAME != null && !CONNNAME.equals("")){attr.setConnection(new UC4ObjectName(CONNNAME))}
			if(DBNAME != null && !DBNAME.equals("")){attr.setDatabaseName(DBNAME)}
			if(SERVERNAME != null && !SERVERNAME.equals("")){attr.setDatabaseServer(SERVERNAME)}
		}
		
		// For CIT/RA Jobs Specifically
		if(job.getType().equals("JOBS_CIT")){
			
			// subjobtype is in job.getRAJobType()
			HashMap<String, String> UpdateValuesHash = new HashMap<>();
			JSONArray ValueList = JsonUpdates.ra_values;
			for(int i=0;i<ValueList.size();i++){
				JSONObject valueJSONObj = (JSONObject) ValueList.get(i);
				String myKey = valueJSONObj.keys().next();
				String myVal = valueJSONObj.get(myKey);
				UpdateValuesHash.put(myKey,myVal);
			}
			
			Iterator<CITValue> ItValues  = job.ocvValues().iterator();
			while(ItValues.hasNext()){
				CITValue val = ItValues.next();
				if(UpdateValuesHash.containsKey(val.getXmlName())){
					val.setValue(UpdateValuesHash.get(val.getXmlName()));
				}
			}
			
		}
		
		if(Commit){
			CommonAERequests.saveAndCloseObject(job,connection)
		}
		else{
			CommonAERequests.closeObject(job,connection)
			CommonAERequests.deleteObject(job.getName(), connection)
		}
		
		if(Commit){return CommonJSONRequests.renderOKAsJSON("Object created successfully",Commit)}
		if(!Commit){return CommonJSONRequests.renderOKAsJSON("Object created simulated successfully",Commit)}
	}
		
		
}
