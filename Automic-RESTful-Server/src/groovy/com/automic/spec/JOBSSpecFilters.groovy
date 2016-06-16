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
		//println "Checking obj:" + obji.getName() +" : is : " + SelectObject
		return SelectObject
	}
}
