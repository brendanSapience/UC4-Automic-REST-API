package com.automic.spec

import com.uc4.api.InvalidUC4NameException
import com.uc4.api.UC4HostName
import com.uc4.api.UC4ObjectName
import com.uc4.api.VersionControlListItem
import com.uc4.api.objects.CustomAttribute
import com.uc4.api.objects.Job;
import com.uc4.api.objects.UC4Object
import com.uc4.communication.Connection;
import com.uc4.communication.requests.VersionControlList
import com.automic.objects.CommonAERequests;
import com.automic.objects.AEFolderRequests;
import com.automic.utils.*
import groovy.json.JsonBuilder

class JOBSSpecDisplay {
	public static def ShowObject(Connection conn,Job job){
		VersionControlList vcl = new VersionControlList(CommonAERequests.getUC4ObjectNameFromString(job.getName(),false));
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
				 name: job.name,
				 title: job.header.title,
				 key1 :  job.header().getArchiveKey1(),
				 key2 :  job.header().getArchiveKey2(),
				 type:job.type,
				 host: job.attributes().getHost(),
				 login: job.attributes().getLogin(),
				 queue: job.attributes().getQueue(),
				 genatruntime: job.attributes().isGenerateAtRuntime(),
				 access:job.access,
				 active:job.header.isActive(),
				 jobtype:job.jobtype,
				 EstimatedRT:job.runtime.estimatedRuntime.currentERT,
				 MaxRT:job.runtime.maximumRuntime.getFixedValue(),
				 MinRT:job.runtime.minimumRuntime.getFixedValue(),
				 variables:CommonJSONRequests.getObjectVariablesAsJSON(job.values()),
				 prompts: CommonJSONRequests.getObjectPromptsAsJSON(job.values()),
				 process: job.getProcess(),
				 preprocess:job.getPreProcess(),
				 postprocess: job.getPostProcess(),
				 versions: versionsData,
				 ]
		  ]

		def json = new JsonBuilder(data)
		return json;	
	}	
}
