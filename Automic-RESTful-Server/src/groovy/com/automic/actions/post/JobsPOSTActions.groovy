package com.automic.actions.post

import com.uc4.communication.Connection;
import groovy.json.JsonBuilder
import groovy.json.JsonException
import groovy.json.JsonSlurper

class JobsPOSTActions {

	public static def update(String version, params,Connection conn,request){return "update${version}"(params,conn,request)}
	
	public static def updatev1(params,Connection conn,request) {
		
		try{
			def jsonSlurper = new JsonSlurper()
			def json = jsonSlurper.parseText(request.reader.text)
			return json
		}catch(JsonException j){
			JsonBuilder json = new JsonBuilder([status: "error", message: "JSON from POST Request has incorrect format."])
			return json
		}
	}
}
