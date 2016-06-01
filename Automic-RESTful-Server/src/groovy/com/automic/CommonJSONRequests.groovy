package com.automic

import com.uc4.api.SearchResultItem
import groovy.json.JsonBuilder

class CommonJSONRequests {

	public static JsonBuilder getResultListAsJSONFormat(List<SearchResultItem> ObjList){
		def data = [
			success: true,
			count: ObjList.size(),
			data: ObjList.collect {[name: it.name, folder:it.folder, title:it.title, type:it.objectType, open:it.open]}
		  ]
		
		//, folder:it.folder, modified:it.modified, type: it.title
		def json = new JsonBuilder(data)
		return json;
	}
	
	
}
