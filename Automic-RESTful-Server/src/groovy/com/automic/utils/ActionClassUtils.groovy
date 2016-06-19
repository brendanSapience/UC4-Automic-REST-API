package com.automic.utils

import groovy.json.JsonBuilder

/**
 * 
 * @author bsp
 * @purpose dynamically calculate the list of operations (ex: search, unblock, login etc.) from any *Actions class.
 * @param Array of public methods from the *Actions class
 * @returns a HashMap with structure: key = unique operation name (ex: search) value = list of versions for the unique operation name (ex: ['searchv1','searchv2','searchv3']) 
 * 
 */

class ActionClassUtils {

	private ArrayList<String> UniqueListOfOperations;
	public HashMap<String,String[]> allVersions = new HashMap<String, String[]>();
	public String HttpMethod='';
	
	ActionClassUtils (ArrayList<String> RawOperationsList, String method){
		this.HttpMethod = method
		setup(RawOperationsList)
	}
	
	ActionClassUtils (ArrayList<String> RawOperationsList){
		setup(RawOperationsList)
	}

	private setup(ArrayList<String> RawOperationsList){
		
		// only keeps methods that match a pattern of xxxxxxvnn (with xxxx:letters, nn: a number) ex: searchv4 or unblockv10
		def ListOfAllVersionnedOperations = RawOperationsList.findAll { it =~/[a-zA-Z]+v\d+/ }

		// takes the filtered list of methods, removes the version from the names and uniques them
		def NonUniqueListOfOperations = ListOfAllVersionnedOperations.collect{ op ->
			op = op.split(/v\d+/)[0]
		}
		this.UniqueListOfOperations = NonUniqueListOfOperations.unique()

		// finally, we build a hash where the keys are the unique names (ex: search) and the values are ArrayList<String> and contain the list of available version for the given key
		// ex: [search:[searchv1, searchv2], delete:[deletev1]]
		this.UniqueListOfOperations.each { Op ->
			ArrayList<String> TempArrayWithVersions = ListOfAllVersionnedOperations.findAll {it =~/^$Op/}
			this.allVersions.put(Op, TempArrayWithVersions)
		}
	}
	
	public JsonBuilder getOpsAndVersionsAsJSON(){
		return CommonJSONRequests.getHashMapAsJSONFormat(this.allVersions)
	}
	
	public JsonBuilder getOpsAndVersionsAsJSON2(){
		return CommonJSONRequests.getHashMapAsJSONFormat2(this.allVersions,this.HttpMethod)
	}
	
	public String[] getUniqueListOfOperations(){
		return UniqueListOfOperations
	}
}
