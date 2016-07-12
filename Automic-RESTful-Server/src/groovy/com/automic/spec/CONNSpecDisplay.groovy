package com.automic.spec

import com.uc4.api.InvalidUC4NameException
import com.uc4.api.UC4HostName
import com.uc4.api.UC4ObjectName
import com.uc4.api.VersionControlListItem
import com.uc4.api.objects.CustomAttribute
import com.uc4.api.objects.DatabaseConnection
import com.uc4.api.objects.FileTransfer
import com.uc4.api.objects.Job;
import com.uc4.api.objects.JobPlan
import com.uc4.api.objects.RAConnection
import com.uc4.api.objects.SAPConnection
import com.uc4.api.objects.UC4Object
import com.uc4.communication.Connection;
import com.uc4.communication.requests.VersionControlList
import com.automic.objects.CommonAERequests;
import com.automic.objects.AEFolderRequests;
import com.automic.utils.*
import groovy.json.JsonBuilder

class CONNSpecDisplay {
	public static def ShowObject(Connection conn,UC4Object obj){
		VersionControlList vcl = new VersionControlList(CommonAERequests.getUC4ObjectNameFromString(obj.getName(),false));
		CommonAERequests.sendSyncRequest(conn,vcl,false);
		Iterator<VersionControlListItem> iterator = vcl.iterator()
		
		def versionsData = [
			 data: iterator.collect {[
				 name:it.getSavedName().getName(),
				 version: it.getVersionNumber()
				 ]}
		  ]

		def ConnObject
		if(obj.type.equalsIgnoreCase("CONN_CIT")){
			ConnObject = (RAConnection) obj;
			
			def data = [
				status: "success",
				count: 1,
				data: [
					 name: ConnObject.name,
					 title: ConnObject.header.title,
					 key1 :  ConnObject.header().getArchiveKey1(),
					 key2 :  ConnObject.header().getArchiveKey2(),
					 access:ConnObject.access,
					 type:ConnObject.type,
					 connname:ConnObject.connectionName,
					 solname:ConnObject.getSolutionName(),
					 values:ConnObject.ocv.iterator().toList(),
					 versions: versionsData,
					 ]
			  ]
	
			def json = new JsonBuilder(data)
			return json;
			
		}
		
		if(obj.type.equalsIgnoreCase("CONN_SQL")){
			ConnObject = (DatabaseConnection) obj;
			
			def data = [
				status: "success",
				count: 1,
				data: [
					 name: ConnObject.name,
					 title: ConnObject.header.title,
					 key1 :  ConnObject.header().getArchiveKey1(),
					 key2 :  ConnObject.header().getArchiveKey2(),
					 access: ConnObject.access,
					 type: ConnObject.type,
					 altlogin: ConnObject.alternativeLogin,
					 connprop: ConnObject.connectProperties,
					 connval: ConnObject.connectStringValues,
					 dbname: ConnObject.dbName,
					 dbport: ConnObject.dbPort,
					 dbtype: ConnObject.getDBType(),
					 dbserver: ConnObject.dbServer,
					 password: ConnObject.password,
					 versions: versionsData,
					 ]
			  ]
	
			def json = new JsonBuilder(data)
			return json;
		}
		
		if(obj.type.equalsIgnoreCase("CONN_R3")){
			ConnObject = (SAPConnection) obj;
			
			def data = [
				status: "success",
				count: 1,
				data: [
					 name: ConnObject.name,
					 title: ConnObject.header.title,
					 key1 :  ConnObject.header().getArchiveKey1(),
					 key2 :  ConnObject.header().getArchiveKey2(),
					 access: ConnObject.access,
					 type: ConnObject.type,
					 conntype: ConnObject.connectionType,
					 http: ConnObject.http,
					 rfc: ConnObject.rfc,
					 versions: versionsData,
					 ]
			  ]
	
			def json = new JsonBuilder(data)
			return json;
		}
		
	
	}	
}
