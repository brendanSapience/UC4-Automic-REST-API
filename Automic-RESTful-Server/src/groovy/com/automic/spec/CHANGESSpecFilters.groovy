package com.automic.spec

import com.uc4.api.SearchResultItem
import com.uc4.api.objects.Job;

class CHANGESSpecFilters {
	
	public static def ObjectMatchesFilter(SearchResultItem obji,JsonSpecFilters){
		boolean SelectObject = false
		
		String AllTypesSelected = JsonSpecFilters.type;
		String[] AllTypessArray = AllTypesSelected.split("\\|");
		String ObjectType = obji.getObjectType();
		

		boolean Found = AllTypessArray.find { it.toUpperCase().equals(ObjectType.toUpperCase()) }
		println "Debug Object Type: " + ObjectType
		if(Found){SelectObject=true}
		
		return SelectObject
	}
}
