package com.automic

import com.uc4.api.SearchResultItem
import com.uc4.api.Task
import groovy.json.JsonBuilder

class CommonJSONRequests {

	public static JsonBuilder getResultListAsJSONFormat(List<SearchResultItem> ObjList){
		def data = [
			success: true,
			count: ObjList.size(),
			data: ObjList.collect {[name: it.name, folder:it.folder, title:it.title, type:it.objectType, open:it.open]}
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