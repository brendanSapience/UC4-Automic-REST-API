package com.automic.ae.actions.post

import com.uc4.communication.Connection;
import groovy.json.JsonBuilder
import groovy.json.JsonException
import groovy.json.JsonSlurper
import com.automic.spec.*

class JobpPOSTActions {

	public static String JsonTemplateFolder = "./JsonPOSTSamples/"
	public static def create(String version, params,Connection conn,request, grailsattr){return "create${version}"(params,conn,request,grailsattr)}
	
	public static def createv1(params,Connection conn,request,grailsattr) {
		String METHOD = params.method;
		String COMMITSTR = params.commit;
		boolean COMMIT = false;
		if(COMMITSTR!=null && COMMITSTR.equalsIgnoreCase("Y")){COMMIT=true;}
		
		if(METHOD != null && METHOD.equals("usage")){
			def JsonFile = grailsattr.getApplicationContext().getResource(JsonTemplateFolder+"JobpPOSTActions"+"_createv1.json").getFile()
			def InputJSON = new JsonSlurper().parseText(JsonFile.text)
			return InputJSON
		}
		
		try{
			def jsonSlurper = new JsonSlurper()
			def json = jsonSlurper.parseText(request.reader.text)
			
			JsonBuilder jsonres = JOBPSpecCreate.CreateObject(conn, json, COMMIT)
			return jsonres
			
		}catch(JsonException j){
			JsonBuilder json = new JsonBuilder([status: "error", message: "JSON from POST Request has incorrect format."])
			return json
		}
	}
	
}
