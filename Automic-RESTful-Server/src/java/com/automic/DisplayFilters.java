package com.automic;

import java.util.HashMap;
import java.util.Iterator;

/**
 * 
 * @author bsp
 * @purpose process the filters URL parameter. 
 * @param String RawFilterParams: [name:.*F,filter2:1234,filter3:JOBP|JOBG]
 * it allows the quick retrieval of a filter value when given the corresponding key.
 */

public class DisplayFilters {

	public String RawFilterParams;
	public HashMap<String,String> FilterValues = new HashMap<String,String>();
	
	// ex: [name:.*F,filter2:1234,filter3:JOBP|JOBG]
	public DisplayFilters(String RawFilterParams){
		
		if(RawFilterParams == null){RawFilterParams="";}
		this.RawFilterParams = RawFilterParams;
		if(RawFilterParams.length()>0){
			String[] RawFilters = RawFilterParams.substring(1,RawFilterParams.length()-1).split(",");
			for(int i=0;i<RawFilters.length;i++){
				String IndividualVal = RawFilters[i];
				String HashVal = "";
				String HashKey = IndividualVal.split(":")[0].replaceAll("\"", "").toUpperCase();
				if(IndividualVal.split(":").length>1){
					HashVal = IndividualVal.split(":")[1].replaceAll("\"", "").toUpperCase(); 
				}else{
					HashVal = ""; 
				}
				FilterValues.put(HashKey, HashVal);
			}
			
//			Iterator<String> keys = FilterValues.keySet().iterator();
//			while(keys.hasNext()){
//				String KEY = keys.next();
//				String VALUE = FilterValues.get(KEY);
//				System.out.println("DEBUG: " + KEY + " : " + VALUE);
//			}
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
