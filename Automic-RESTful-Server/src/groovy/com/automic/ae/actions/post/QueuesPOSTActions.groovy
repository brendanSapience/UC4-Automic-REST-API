package com.automic.ae.actions.post

import com.uc4.api.objects.UC4Object
import com.uc4.communication.Connection
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import com.automic.objects.CommonAERequests
import com.automic.utils.MiscUtils
import com.automic.spec.QUEUESSpecDisplay

class QueuesPOSTActions {
	
	public static String JsonTemplateFolder = "./JsonPOSTSamples/"
	
	public static def update(String version, params,Connection conn,request, grailsattr){return "update${version}"(params,conn,request,grailsattr)}
	
	public static def updatev1(params,Connection conn,request,grailsattr) {

		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': ['name (format: name=<String> Process Name(ex: QUEUE.TST.1))'],
			'optional_parameters': ['commit (format: commit=Y'],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
		
		String OBJNAMEASSTR = params.name;
		String METHOD = params.method;
		String COMMITSTR = params.commit;
		boolean COMMIT = false;
		
		if(COMMITSTR!=null && COMMITSTR.equalsIgnoreCase("Y")){COMMIT=true;}
		
		if(METHOD != null && METHOD.equals("usage")){
			def JsonFile = grailsattr.getApplicationContext().getResource(JsonTemplateFolder+"QueuesPOSTActions"+"_updatev1.json").getFile()
			def InputJSON = new JsonSlurper().parseText(JsonFile.text)
			return InputJSON
		}else{
			if(MiscUtils.checkParams(AllParamMap, params)){
				def jsonSlurper = new JsonSlurper()
				def json = jsonSlurper.parseText(request.reader.text)
				
				String NEWPRIORITYASSTR = json.priority;
				String NEWMAXSLOTSASSTR = json.maxslots;
				boolean NEWCONSIDERERTASBOOL = json.considerERT;
				String NewTitle = json.title;
				String NewArch1 = json.arch1;
				String NewArch2 = json.arch2;
				
				int NEWPRIORITY = -1;
				int NEWMAXSLOTS = -1;
				boolean UpdateERT = false;
				
				if(NEWCONSIDERERTASBOOL != null){
					UpdateERT = true;
				}
				if(NEWPRIORITYASSTR != null && NEWPRIORITYASSTR.isInteger()){NEWPRIORITY = NEWPRIORITYASSTR.toInteger()}
				if(NEWMAXSLOTSASSTR != null && NEWMAXSLOTSASSTR.isInteger()){NEWMAXSLOTS = NEWMAXSLOTSASSTR.toInteger()}

				UC4Object obj
				if(COMMIT){

					String r = CommonAERequests.reclaimObject(OBJNAMEASSTR,conn)
					obj = CommonAERequests.openObject(conn,OBJNAMEASSTR,false)
				}else{

					obj = CommonAERequests.openObject(conn,OBJNAMEASSTR,true)
				}
				if(obj==null){
					return new JsonBuilder([status: "error", message: "Could not find queue "+OBJNAMEASSTR])
				}
				
				com.uc4.api.objects.Queue myQueue = (com.uc4.api.objects.Queue)obj;
				
				if(NewArch1 != null){
					myQueue.header().setArchiveKey1(NewArch1);
				}
				
				if(NewArch2 != null){
					myQueue.header().setArchiveKey2(NewArch2);
					
				}
				
				if(NewTitle != null){
					myQueue.header().setTitle(NewTitle);
				}
				
				if(UpdateERT){
					myQueue.queue().setConsiderERT(NEWCONSIDERERTASBOOL)
				}
				
				if(NEWMAXSLOTS != -1){
					myQueue.queue().setMaxSlots(NEWMAXSLOTS);	
				}
				
				if(NEWPRIORITY != -1){
					myQueue.queue().setPriority(NEWPRIORITY);
				}
				
				if(COMMIT){
					CommonAERequests.saveObject(myQueue, conn);
				}

				QUEUESSpecDisplay.ShowObject(conn,myQueue);
				
			}
		}
	}
}
