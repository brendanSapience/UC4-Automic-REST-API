package com.automic.actions.post

import com.uc4.api.SearchResultItem
import com.uc4.communication.Connection;
import com.uc4.communication.requests.SearchObject
import groovy.json.JsonBuilder
import groovy.json.JsonException
import groovy.json.JsonSlurper
import com.automic.utils.JsonFiltersUtils
import com.automic.utils.CommonJSONRequests
import com.automic.objects.CommonAERequests
import com.automic.spec.ALLSpecFilters

class AllPOSTActions {

	public static String JsonTemplateFolder = "./JsonPOSTSamples/"
	
	public static def delete(String version, params,Connection conn,request, grailsattr){return "delete${version}"(params,conn,request,grailsattr)}
	
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
			req.selectAllObjectTypes();
			List<SearchResultItem> ObjList = JsonFiltersUtils.getObjectListFromSearchAndStdFilters(conn, req, JsonStdFilters)
			ArrayList<SearchResultItem> SelectedObjects = new ArrayList<SearchResultItem>()
			if(!JsonSpecFilters.isEmpty()){
				ObjList.each {
					if(ALLSpecFilters.ObjectMatchesFilter(it,JsonSpecFilters)){
						SelectedObjects.add(it)
					}
				}
				
			}else{
				ObjList.each {
					SelectedObjects.add(it)
				}
			}
			// we now have a selected list of non instantiated objects
			def data = [
				success: true,
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
