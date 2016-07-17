package com.automic.spec

import com.uc4.api.InvalidUC4NameException
import com.uc4.api.UC4HostName
import com.uc4.api.UC4ObjectName
import com.uc4.api.VersionControlListItem
import com.uc4.api.objects.CustomAttribute
import com.uc4.api.objects.FileTransfer
import com.uc4.api.objects.Job;
import com.uc4.api.objects.JobPlan
import com.uc4.api.objects.UC4Object
import com.uc4.communication.Connection;
import com.uc4.communication.requests.VersionControlList
import com.automic.objects.CommonAERequests;
import com.automic.objects.AEFolderRequests;
import com.automic.utils.*
import groovy.json.JsonBuilder

class JOBFSpecDisplay {
	public static def ShowObject(Connection conn,FileTransfer obj){
		VersionControlList vcl = new VersionControlList(CommonAERequests.getUC4ObjectNameFromString(obj.getName(),false));
		CommonAERequests.sendSyncRequest(conn,vcl,false);
		Iterator<VersionControlListItem> iterator = vcl.iterator()
		
		def versionsData = 
			 iterator.collect {[
				 name:it.getSavedName().getName(),
				 version: it.getVersionNumber()
				 ]}
		  

		def data = [
			status: "success",
			count: 1,
			data: [
				 name: obj.name,
				 access:obj.access,
				 title: obj.header.title,
				 key1 :  obj.header().getArchiveKey1(),
				 key2 :  obj.header().getArchiveKey2(),
				 type:obj.type,
				 queue: obj.attributes().getQueue(),
				 genatruntime: obj.attributes().isGenerateAtRuntime(),
				 access:obj.access,
				 active:obj.header.isActive(),
				 EstimatedRT:obj.runtime.estimatedRuntime.currentERT,
				 MaxRT:obj.runtime.maximumRuntime.getFixedValue(),
				 MinRT:obj.runtime.minimumRuntime.getFixedValue(),
				 variables:CommonJSONRequests.getObjectVariablesAsJSON(obj.values()),
				 prompts: CommonJSONRequests.getObjectPromptsAsJSON(obj.values()),
				 process: obj.getProcess(),
				 settings:obj.settings,
				 values:obj.values,
				 versions: versionsData,
				 ]
		  ]

		def json = new JsonBuilder(data)
		return json;	
	}	
}
