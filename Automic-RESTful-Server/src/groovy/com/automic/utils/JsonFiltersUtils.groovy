package com.automic.utils

import groovy.json.JsonSlurper;

import com.uc4.api.SearchResultItem
import com.uc4.communication.Connection;
import com.uc4.communication.requests.SearchObject;
import com.automic.objects.CommonAERequests;

class JsonFiltersUtils {

	public static def List<SearchResultItem> getObjectListFromSearchAndStdFilters(Connection conn,SearchObject req, JsonStdFilters){

		String NAME = JsonStdFilters.name
		boolean INCLUDELINKS = JsonStdFilters.include_links
		boolean SEARCHFORUSAGE = JsonStdFilters.search_for_usage
		
		String TEXT = JsonStdFilters.text
		boolean SEARCHTEXT = JsonStdFilters.search_text
		boolean SEARCHTEXTTITLE = JsonStdFilters.search_text_title
		boolean SEARCHTEXTKEY = JsonStdFilters.search_text_key
		boolean SEARCHTEXTPROCESS = JsonStdFilters.search_text_process
		boolean SEARCHTEXTDOCU = JsonStdFilters.search_text_docu
		
		boolean SEARCHDATE = JsonStdFilters.date_search
		String SEARCHDATETYPE = JsonStdFilters.date_search_type
		String SEARCHDATEFROM = JsonStdFilters.date_from
		String SEARCHDATETO = JsonStdFilters.date_to
		
		if(NAME == null ||NAME.equals("")){NAME = "*"}
		if(INCLUDELINKS == null){INCLUDELINKS=false}
		if(SEARCHFORUSAGE == null){SEARCHFORUSAGE=false}
		
		if(TEXT == null || TEXT.equals("")){TEXT="*"}
		if(SEARCHTEXT == null){SEARCHTEXT=false}
		if(SEARCHTEXTTITLE == null){SEARCHTEXTTITLE=false}
		if(SEARCHTEXTKEY == null){SEARCHTEXTKEY=false}
		if(SEARCHTEXTPROCESS == null){SEARCHTEXTPROCESS=false}
		if(SEARCHTEXTDOCU == null){SEARCHTEXTDOCU=false}
		
		if(SEARCHDATE == null){SEARCHDATE=false}
		if(SEARCHDATETYPE != null && SEARCHDATETYPE.toLowerCase().equals("created") && SEARCHDATETYPE.toLowerCase().equals("modified") &&SEARCHDATETYPE.toLowerCase().equals("used")){
			SEARCHDATETYPE = "created"}
		if(SEARCHDATEFROM == null || SEARCHDATEFROM.equals("")){SEARCHDATEFROM="201601010000"}
		if(SEARCHDATETO == null || SEARCHDATETO.equals("")){SEARCHDATETO="210001010000"};
		
		req.setIncludeLinks(INCLUDELINKS)
		req.setSearchUseOfObjects(SEARCHFORUSAGE)
		req.setTextSearch(TEXT, SEARCHTEXTTITLE, SEARCHTEXTKEY, SEARCHTEXTPROCESS, SEARCHTEXTDOCU)
		req.setNoDateSelection()
		if(SEARCHDATETYPE.equals("created")){req.setDateSelectionCreated(MiscUtils.HandleDateText(SEARCHDATEFROM), MiscUtils.HandleDateText(SEARCHDATETO))}
		if(SEARCHDATETYPE.equals("modified")){req.setDateSelectionCreated(MiscUtils.HandleDateText(SEARCHDATEFROM), MiscUtils.HandleDateText(SEARCHDATETO))}
		if(SEARCHDATETYPE.equals("used")){req.setDateSelectionCreated(MiscUtils.HandleDateText(SEARCHDATEFROM), MiscUtils.HandleDateText(SEARCHDATETO))}
		
		List<SearchResultItem> JobList = CommonAERequests.GenericSearchObjects(conn, NAME, req);
		
		return JobList;
	}
	
}
