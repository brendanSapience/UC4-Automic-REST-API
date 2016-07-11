package com.automic.ae.actions.post

import com.uc4.communication.Connection;
import groovy.json.JsonSlurper
import com.uc4.api.objects.Job
import com.uc4.api.SearchResultItem
import com.uc4.api.objects.UC4Object
import com.uc4.communication.Connection;
import com.uc4.communication.requests.SearchObject

import groovy.json.JsonBuilder
import groovy.json.JsonException
import groovy.json.JsonSlurper

import com.automic.utils.JsonFiltersUtils
import com.automic.utils.CommonJSONRequests
import com.automic.objects.CommonAERequests
import com.automic.spec.*


class ClientPOSTActions {
	
	public static String JsonTemplateFolder = "./JsonPOSTSamples/"
	
	// create client isnt ready yet
	//public static def create(String version, params,Connection conn,request, grailsattr){return "create${version}"(params,conn,request,grailsattr)}
	
	public static def createv1(params,Connection conn,request,grailsattr) {

	}
}
