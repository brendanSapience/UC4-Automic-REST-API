package com.automic.ae.actions.post

import com.uc4.communication.Connection;
import com.uc4.communication.requests.AddComment
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import com.automic.objects.CommonAERequests
import com.automic.utils.MiscUtils

class ActivitiesPOSTActions {

	
	public static String JsonTemplateFolder = "./JsonPOSTSamples/"
	
	public static def comment(String version, params,Connection conn,request, grailsattr){return "comment${version}"(params,conn,request,grailsattr)}
	
	
	public static def commentv1(params,Connection conn,request,grailsattr) {
		
		
		def AllParamMap = [:]
		AllParamMap = [
			'required_parameters': ['runid (format: runid= < integer >'],
			'optional_parameters': ['commit (format: commit=Y'],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage']
			]
		
		String RUNIDASSTR = params.runid;
		String METHOD = params.method;
		String COMMITSTR = params.commit;
		boolean COMMIT = false;
		
		int RUNID = -1;
		if(RUNIDASSTR.isInteger()){RUNID = RUNIDASSTR.toInteger()}
		
		if(COMMITSTR!=null && COMMITSTR.equalsIgnoreCase("Y")){COMMIT=true;}
		
		if(METHOD != null && METHOD.equals("usage")){
			def JsonFile = grailsattr.getApplicationContext().getResource(JsonTemplateFolder+"ActivitiesPOSTActions"+"_commentv1.json").getFile()
			def InputJSON = new JsonSlurper().parseText(JsonFile.text)
			return InputJSON
		}else{
			if(MiscUtils.checkParams(AllParamMap, params)){
				def jsonSlurper = new JsonSlurper()
				def json = jsonSlurper.parseText(request.reader.text)
				
				String NEWCOMMENT = json.comment;
				
				AddComment req = new AddComment(RUNID,NEWCOMMENT) ;
				
				CommonAERequests.sendSyncRequest(conn, req, false);
				if(req.getMessageBox()!=null){
					return new JsonBuilder([status: "error", message: req.getMessageBox().getText()])
				}else{
					return new JsonBuilder([status: "success", message: "comment added."])
				}
			}
		}
	}
	
}
