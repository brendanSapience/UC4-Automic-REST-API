package com.automic.spec

import com.uc4.api.objects.Job
import groovy.json.JsonBuilder

class JOBSSpecUpdates {

	public static def UpdateObject(Job obj,JsonUpdates,boolean Commit){
		
		if(JsonUpdates.status != null && JsonUpdates.status.equalsIgnoreCase("active")){obj.header().setActive(true);}
		if(JsonUpdates.status != null && JsonUpdates.status.equalsIgnoreCase("inactive")){obj.header().setActive(false);}
		
		if(JsonUpdates.genatruntime != null && JsonUpdates.genatruntime == true){obj.attributes().setGenerateAtRuntime(true);}
		if(JsonUpdates.genatruntime != null && JsonUpdates.genatruntime == false){obj.attributes().setGenerateAtRuntime(false);}
		
	}
	
	public static def getJSONStructure(ArrayList<Job> SelectedObjects,boolean commit){
		def data = [
			success: true,
			commit: commit,
			count: SelectedObjects.size(),
			data: SelectedObjects.collect {[name: it.name, type:it.type, access:it.access, active:it.header.isActive(),
				 jobtype:it.jobtype]}
			//properties:it.getProperties().toMapString()
		  ]

		def json = new JsonBuilder(data)
		return json;
	}
}
