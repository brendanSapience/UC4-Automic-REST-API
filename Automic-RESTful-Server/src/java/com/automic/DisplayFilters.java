package com.automic;

import java.util.HashMap;

public class DisplayFilters {

	private String RawFilterParams;
	public HashMap<String,String> FilterValues = new HashMap<String,String>();
	
	//[name:"NOVA.*"]
	public DisplayFilters(String RawFilterParams){
		this.RawFilterParams = RawFilterParams;
		
		String[] RawFilters = RawFilterParams.substring(1,RawFilterParams.length()-1).split(",");
		for(int i=0;i<RawFilters.length;i++){
			String IndividualVal = RawFilters[i];
			
			String HashKey = IndividualVal.split(":")[0].replaceAll("\"", "").toUpperCase();
			String HashVal = IndividualVal.split(":")[1].replaceAll("\"", "").toUpperCase(); 
			
			FilterValues.put(HashKey, HashVal);
		}
		
	}
	
	public String getValueFromKey(String Key){
		return this.FilterValues.get(Key.toUpperCase());
	}
	
	public boolean doesKeyExistInFilter(String Key){
		if(this.FilterValues.get(Key.toUpperCase()) == null){
			return false;
		}
		else{
			return true;
		}
	}

}
