package com.automic.utils

import com.uc4.api.SearchResultItem
import com.uc4.api.StatisticSearchItem
import com.uc4.api.Task
import com.uc4.api.objects.ObjectValues;
import com.uc4.api.objects.PromptSetDefinition
import com.uc4.communication.requests.GetChangeLog

import groovy.json.JsonBuilder

/**
 * 
 * @author bsp
 * @purpose returns JsonBuilder objects from various hash, lists, arrays..
 *
 */

class CommonJSONRequests {
	
	private static String OKSTATUS = "success"
	private static String NOKSTATUS = "error"
	
	
	public static JsonBuilder renderErrorAsJSON(String MessageBox){
		return  new JsonBuilder( 
			[
			status: NOKSTATUS,
			message: MessageBox
		  ])
	}
	public static JsonBuilder renderOKAsJSON(String MessageBox){
		return  new JsonBuilder(
			[
			status: OKSTATUS,
			message: MessageBox
		  ])
	}
	
	public static JsonBuilder getHashMapAsJSONFormat2(HashMap<String,String[]> ObjList, String HttpMethod){
		
		def data = [
			status: OKSTATUS,
			type: HttpMethod,
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
	
	public static JsonBuilder getHashMapAsJSONFormat(HashMap<String,String[]> ObjList){
		
		def data = [
			status: OKSTATUS,
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
			status: OKSTATUS,
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
			status: OKSTATUS,
			count: ObjList.size(),
			data: ObjList.collect {[name: it.name, folder: it.folder, title: it.title, type: it.objectType, open: it.open]}
		  ]
		
		//, folder:it.folder, modified:it.modified, type: it.title
		def json = new JsonBuilder(data)
		return json;
	}
	
	public static JsonBuilder getResultListAsJSONFormat(List<SearchResultItem> ObjList,boolean Commit){
		def data = [
			status: OKSTATUS,
			commit: Commit,
			simulate: !Commit,
			count: ObjList.size(),
			data: ObjList.collect {[name: it.name, folder: it.folder, title: it.title, type: it.objectType, open: it.open]}
		  ]
		
		//, folder:it.folder, modified:it.modified, type: it.title
		def json = new JsonBuilder(data)
		return json;
	}
	
	public static JsonBuilder getStringListAsJSONFormat(String OpName, String[] ObjList){
		def data = [
			status: OKSTATUS,
			count: ObjList.length,
			data: ObjList.collect {["$OpName": it]}
		  ]
		
		//, folder:it.folder, modified:it.modified, type: it.title
		def json = new JsonBuilder(data)
		return json;
	}
	
	public static JsonBuilder getSupportedThingsAsJSONFormat(ObjList){
		def data = [
			status: OKSTATUS,
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
	public static def getObjectVariablesAsJSON(ObjectValues objVals){
		Iterator<String> iterator = objVals.valueKeyIterator()
		
		def data = [
			size: objVals.map.entrySet().size(),
			 data: iterator.collect {[name: it, value:objVals.getValue(it)]}
		  ]
		return data
	}
	
	public static def getChangesAsJSON2(ArrayList<GetChangeLog.Entry> allentries){
		def data = [
			status: OKSTATUS,
			count: allentries.size(),
			 data: allentries.collect {
				 [
					 objectname:it.getObjectName(),
					 title:it.getTitle(),
					 type:it.getType(),
					 username:it.getUserName(),
					 timestamp:it.getTimestamp().toString(),
					 message: it.getMessage(),
					 firstname:it.getFirstName(),
					 lastname:it.getLastName(),
				 ]}
			  ]
		return data
	}
	
	public static def getObjectPromptsAsJSON(ObjectValues objVals){
		Iterator<PromptSetDefinition> iterator = objVals.promptSetIterator()
		def data = [
			status: OKSTATUS,
			count: objVals.promptSetSize(),
			data: iterator.collect {[name: it.getName().toString(), elements:it.elementIDs.length]}
		  ]
		return data
	}
	
	public static JsonBuilder getActivityListAsJSONFormat(List<Task> ObjList){
		def data = [
			status: OKSTATUS,
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
