package com.automic.actions.post

import com.uc4.api.objects.Job
import com.uc4.api.SearchResultItem
import com.uc4.api.objects.UC4Object
import com.uc4.communication.Connection;
import com.uc4.communication.requests.SearchObject
import groovy.json.JsonBuilder
import groovy.json.JsonException
import groovy.json.JsonSlurper
import com.automic.utils.JsonFiltersUtils
import com.automic.utils.CommonJSONRequests
import com.automic.objects.CommonAERequests
import com.automic.spec.*

class JobsPOSTActions {

	public static String JsonTemplateFolder = "./JsonPOSTSamples/"
	public static def update(String version, params,Connection conn,request, grailsattr){return "update${version}"(params,conn,request,grailsattr)}
	public static def delete(String version, params,Connection conn,request, grailsattr){return "delete${version}"(params,conn,request,grailsattr)}
	
	public static def updatev1(params,Connection conn,request,grailsattr) {
		
		String METHOD = params.method;
		String COMMITSTR = params.commit;
		boolean COMMIT = false;
		if(COMMITSTR!=null && COMMITSTR.equalsIgnoreCase("Y")){COMMIT=true;}
		
		if(METHOD != null && METHOD.equals("usage")){
			def JsonFile = grailsattr.getApplicationContext().getResource(JsonTemplateFolder+"JobsPOSTActions"+"_updatev1.json").getFile()
			def InputJSON = new JsonSlurper().parseText(JsonFile.text)
			return InputJSON
		}
		
		try{
			def jsonSlurper = new JsonSlurper()
			def json = jsonSlurper.parseText(request.reader.text)
			
			// Getting JSON structures
			def JsonStdFilters = json.std_filters // Stuff that a simple search will return
			def JsonSpecFilters = json.spec_filters // stuff that requires instantiation of objects
			def JsonUpdates = json.updates
			
			// Simple Filters
			SearchObject req = new SearchObject();
			req.unselectAllObjectTypes();
			req.setTypeJOBS(true);
			List<SearchResultItem> ObjList = JsonFiltersUtils.getObjectListFromSearchAndStdFilters(conn, req, JsonStdFilters)
			ArrayList<Job> SelectedObjects = new ArrayList<Job>()
			if(!JsonSpecFilters.isEmpty()){
				ObjList.each {
					UC4Object obj
					if(COMMIT){
						String r = CommonAERequests.reclaimObject(it.getName(),conn)
						obj = CommonAERequests.openObject(conn,it.getName(),false)
					}else{
						obj = CommonAERequests.openObject(conn,it.getName(),true)
					}
					
					Job obji = (Job)obj;
					if(JOBSSpecFilters.ObjectMatchesFilter(obji,JsonSpecFilters)){
						SelectedObjects.add(obji)
					}
				}
			}else{
			ObjList.each {
				if(COMMIT){
					String r = CommonAERequests.reclaimObject(it.getName(),conn)
					SelectedObjects.add((Job)CommonAERequests.openObject(conn,it.getName(),false))
				}else{
					SelectedObjects.add((Job)CommonAERequests.openObject(conn,it.getName(),true))
				}
				
			}
		}
			// we now have a selected list of instantiated objects
			SelectedObjects.each {
				JOBSSpecUpdates.UpdateObject(it,JsonUpdates,COMMIT)
				if(COMMIT){
					//println "Saving Object: " + it.getName()
					CommonAERequests.saveAndCloseObject(it,conn)}
				else{CommonAERequests.closeObject(it,conn)}
				//println "Selected:"+it.getName()
			}
			JsonBuilder jsonresp = JOBSSpecUpdates.getJSONStructure(SelectedObjects,COMMIT)
			return jsonresp //CommonJSONRequests.getResultListAsJSONFormat(ObjList)
			
		}catch(JsonException j){
			JsonBuilder json = new JsonBuilder([status: "error", message: "JSON from POST Request has incorrect format."])
			return json
		}
	}
	
	public static def deletev1(params,Connection conn,request,grailsattr) {
		
		String METHOD = params.method;
		String COMMITSTR = params.commit;
		boolean COMMIT = false;
		if(COMMITSTR!=null && COMMITSTR.equalsIgnoreCase("Y")){COMMIT=true;}
		
		if(METHOD != null && METHOD.equals("usage")){
			def JsonFile = grailsattr.getApplicationContext().getResource(JsonTemplateFolder+"JobsPOSTActions"+"_deletev1.json").getFile()
			def InputJSON = new JsonSlurper().parseText(JsonFile.text)
			return InputJSON
		}
		
		try{
			def jsonSlurper = new JsonSlurper()
			def json = jsonSlurper.parseText(request.reader.text)
			
			// Getting JSON structures
			def JsonStdFilters = json.std_filters // Stuff that a simple search will return
			def JsonSpecFilters = json.spec_filters // stuff that requires instantiation of objects
			
			// Simple Filters
			SearchObject req = new SearchObject();
			req.unselectAllObjectTypes();
			req.setTypeJOBS(true);
			List<SearchResultItem> ObjList = JsonFiltersUtils.getObjectListFromSearchAndStdFilters(conn, req, JsonStdFilters)
			ArrayList<Job> SelectedObjects = new ArrayList<Job>()
			if(!JsonSpecFilters.isEmpty()){
				ObjList.each {
					UC4Object obj

					String r = CommonAERequests.reclaimObject(it.getName(),conn)
					obj = CommonAERequests.openObject(conn,it.getName(),true)
					Job obji = (Job)obj;
					
					if(JOBSSpecFilters.ObjectMatchesFilter(obji,JsonSpecFilters)){
						SelectedObjects.add(obji)
					}
				}
			}else{
				ObjList.each {
					String r = CommonAERequests.reclaimObject(it.getName(),conn)
					SelectedObjects.add((Job)CommonAERequests.openObject(conn,it.getName(),true))
				}
			}
			// we now have a selected list of instantiated objects
			def data = [
				status: "success",
				commit: COMMIT,
				simulate:!COMMIT,
				count: SelectedObjects.size(),
				delete: SelectedObjects.collect {[name: it.name, type:it.type]}
				
			 ]
			
			def jsonRESP = new JsonBuilder(data)
			
			SelectedObjects.each {
				
				if(COMMIT){CommonAERequests.deleteObject(it.getName(),conn)}
	
			}
			//JsonBuilder jsonresp = JOBSSpecUpdates.getJSONStructure(SelectedObjects,COMMIT)
			return jsonRESP //CommonJSONRequests.getResultListAsJSONFormat(ObjList)
			
		}catch(JsonException j){
			JsonBuilder json = new JsonBuilder([status: "error", message: "JSON from POST Request has incorrect format."])
			return json
		}
	}
}
