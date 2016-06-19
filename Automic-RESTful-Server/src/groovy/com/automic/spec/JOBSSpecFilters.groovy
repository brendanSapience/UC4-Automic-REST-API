package com.automic.spec

import com.uc4.api.objects.Job

class JOBSSpecFilters {

	public static def ObjectMatchesFilter(Job obji,JsonSpecFilters){
		boolean SelectObject = true
		
		boolean SELECTINACTIVE = JsonSpecFilters.inactive
		boolean SELECTACTIVE = JsonSpecFilters.active

		if(SELECTINACTIVE == null){SELECTINACTIVE = true}
		if(SELECTACTIVE == null){SELECTACTIVE = true}
		
		if(!SELECTACTIVE && obji.header().isActive()){SelectObject = false}
		if(!SELECTINACTIVE && !obji.header().isActive()){SelectObject = false}
		
		if(JsonSpecFilters.process != null && !JsonSpecFilters.process.equals("")){
			if(!obji.getProcess().matches(JsonSpecFilters.process)){SelectObject=false}
		}	
		
		if(JsonSpecFilters.preprocess != null && !JsonSpecFilters.preprocess.equals("")){
			if(!obji.getPreProcess().matches(JsonSpecFilters.preprocess)){SelectObject=false}
		}
		
		if(JsonSpecFilters.postprocess != null && !JsonSpecFilters.postprocess.equals("")){
			if(!obji.getPostProcess().matches(JsonSpecFilters.postprocess)){SelectObject=false}
		}
		
		return SelectObject
	}
}
