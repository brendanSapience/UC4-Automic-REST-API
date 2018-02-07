package com.automic.spec

import com.uc4.api.InvalidUC4NameException
import com.uc4.api.UC4HostName
import com.uc4.api.UC4ObjectName
import com.uc4.api.VersionControlListItem
import com.uc4.api.objects.CustomAttribute
import com.uc4.api.objects.Job;
import com.uc4.api.objects.UC4Object
import com.uc4.api.objects.OCVPanel.CITValue
import com.uc4.communication.Connection;
import com.uc4.communication.requests.VersionControlList
import com.automic.objects.CommonAERequests;
import com.automic.objects.AEFolderRequests;
import com.automic.utils.*
import groovy.json.JsonBuilder

class QUEUESSpecDisplay {
	public static def ShowObject(Connection conn,com.uc4.api.objects.Queue obj){
		VersionControlList vcl = new VersionControlList(CommonAERequests.getUC4ObjectNameFromString(obj.getName(),false));
		CommonAERequests.sendSyncRequest(conn,vcl,false);
		Iterator<VersionControlListItem> iterator = vcl.iterator()
		def additionalInfo = {}
		
		def versionsData = 
			 iterator.collect {[
				 name:it.getSavedName().getName(),
				 version: it.getVersionNumber()
				 ]}
		  
		
		def data = [
			status: "success",
			count: 1,
			data: [
				 name: obj.getName(),
				 maxslots: obj.queue().getMaxSlots(),
				 ERT: obj.queue().isConsiderERT(),
				 priority: obj.queue().getPriority()
				 ]
		  ]

		def json = new JsonBuilder(data)
		return json;	
	}	
}
