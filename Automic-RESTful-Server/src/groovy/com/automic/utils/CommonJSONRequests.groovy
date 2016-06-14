package com.automic.utils

import com.uc4.api.SearchResultItem
import com.uc4.api.StatisticSearchItem
import com.uc4.api.Task
import groovy.json.JsonBuilder

/**
 * 
 * @author bsp
 * @purpose returns JsonBuilder objects from various hash, lists, arrays..
 *
 */

class CommonJSONRequests {
	
	public static JsonBuilder renderErrorAsJSON(String MessageBox){
		return  new JsonBuilder( 
			[
			success: false,
			message: MessageBox
		  ])
	}
	public static JsonBuilder renderOKAsJSON(String MessageBox){
		return  new JsonBuilder(
			[
			success: true,
			message: MessageBox
		  ])
	}
	
	public static JsonBuilder getHashMapAsJSONFormat(HashMap<String,String[]> ObjList){
		
		def data = [
			success: true,
			count: ObjList.size(),
			data: ObjList.collect {k,v ->
				//println "key is: " + k
				//println "values is: " + v
				["operation": k, "versions":v]
			}
			//properties:it.getProperties().toMapString()
		  ]

		def json = new JsonBuilder(data)
		return json;
	}
	
	public static JsonBuilder getStatisticResultListAsJSONFormat(List<StatisticSearchItem> ObjList){
		
		def data = [
			success: true,
			count: ObjList.size(),
			data: ObjList.collect {[name: it.name, type:it.type, desc:it.periodDescription,
				 platform:it.platform, runid:it.runID, parent:it.parentRunID, user:it.userName, 
				 starttime:it.startTime.toString(), status:it.status,
				 statuscode:it.statusCode, statusdesc:it.statusText,
				 archive1:it.archive1, archive2:it.archive2, endtime:it.endTime.toString(), 
				 runtime:it.runtime, 
				 host:it.host,desc:it.periodDescription, 
				 queue:it.queue,starttype:it.startType, activation_time:it.activationTime.toString(), client:it.client, comment:it.comment.toString(),
				 message:it.message, version:it.getVersion()]}
			//properties:it.getProperties().toMapString()
		  ]

		def json = new JsonBuilder(data)
		return json;
	}
	
	public static JsonBuilder getResultListAsJSONFormat(List<SearchResultItem> ObjList){
		def data = [
			success: true,
			count: ObjList.size(),
			data: ObjList.collect {[name: it.name, folder: it.folder, title: it.title, type: it.objectType, open: it.open]}
		  ]
		
		//, folder:it.folder, modified:it.modified, type: it.title
		def json = new JsonBuilder(data)
		return json;
	}
	
	public static JsonBuilder getStringListAsJSONFormat(String OpName, String[] ObjList){
		def data = [
			success: true,
			count: ObjList.length,
			data: ObjList.collect {["$OpName": it]}
		  ]
		
		//, folder:it.folder, modified:it.modified, type: it.title
		def json = new JsonBuilder(data)
		return json;
	}
	
	public static JsonBuilder getSupportedThingsAsJSONFormat(ObjList){
		def data = [
			success: true,
			required_parameters: ObjList.required_parameters,
			optional_parameters: ObjList.optional_parameters,
			optional_filters: ObjList.optional_filters,
			required_methods: ObjList.required_methods,
			optional_methods: ObjList.optional_methods,
			developer_comment: ObjList.developer_comment
			
		  ]
		
		//, folder:it.folder, modified:it.modified, type: it.title
		def json = new JsonBuilder(data)
		return json;
	}
	
	public static JsonBuilder getActivityListAsJSONFormat(List<Task> ObjList){
		def data = [
			success: true,
			count: ObjList.size(),
			data: ObjList.collect {[name: it.name, type:it.type, desc:it.periodDescription,
				 platform:it.platform, runid:it.runID, parent:it.parentRunID, user:it.userName, 
				 starttime:it.startTime.toString(), priority:it.priority, status:it.status,
				 statuscode:it.statusCode, statusdesc:it.statusText,
				 archive1:it.archive1, archive2:it.archive2, endtime:it.endTime.toString(), 
				 runtime:it.runtime,consumption:it.consumption, cputime:it.cpuTime, 
				 host:it.host, modification:it.modificationFlag,desc:it.periodDescription, 
				 queue:it.queue,starttype:it.startType,statuswithinparent:it.statusWithinParentText]}
			
		 ]
		
		def json = new JsonBuilder(data)
		return json;
	}
}
