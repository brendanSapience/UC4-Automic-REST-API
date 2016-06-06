package com.automic.utils

import groovy.json.JsonBuilder

class ActionClassUtils {

	ArrayList<String> UniqueListOfOperations;
	HashMap<String,String[]> allVersions = new HashMap<String, String[]>();
	
	ActionClassUtils (ArrayList<String> RawOperationsList){
		
		// only keeps methods that match a pattern of xxxxxxvnn (with xxxx:letters, nn: a number)
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
		
		//println allVersions
		
	}
		
	public JsonBuilder getOpsAndVersionsAsJSON(){
		return CommonJSONRequests.getHashMapAsJSONFormat(this.allVersions)
	}
	
	public String[] getUniqueListOfOperations(){
		return UniqueListOfOperations
	}
}