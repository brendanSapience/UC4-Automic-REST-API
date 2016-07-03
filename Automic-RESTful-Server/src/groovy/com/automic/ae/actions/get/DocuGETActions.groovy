package com.automic.ae.actions.get

import com.automic.DisplayFilters
import com.uc4.api.DateTime
import com.uc4.api.SearchResultItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter
import com.uc4.api.UC4ObjectName
import com.uc4.api.UC4TimezoneName
import com.uc4.api.TaskFilter.TimeFrame
import com.uc4.api.objects.DocuContainer
import com.uc4.api.objects.TextDocumentation
import com.uc4.api.objects.XMLDocuNode
import com.uc4.api.objects.XMLDocumentation
import com.uc4.communication.Connection
import com.uc4.communication.requests.GetObjectDocu


import groovy.json.JsonBuilder

import com.automic.connection.AECredentials
import com.automic.connection.ConnectionManager
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests
import com.automic.utils.MiscUtils
import com.uc4.communication.requests.GetComments.Comment

class DocuGETActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def show(String version, params,Connection conn,request, grailsattr){return "show${version}"(params,conn)}

	
	/**
	 * @purpose search changes (audit trail) against filters
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def showv1(params,Connection conn){
	
	def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': [],
				'optional_parameters': ['name (format: name=<String>)'],
				'optional_filters': [
				],
				'required_methods': [],
				'optional_methods': ['usage']
				]
		
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String METHOD = params.method;
		String MAXRES = params.maxresults;
		
		JsonBuilder json;
		
		// Helper Methods
		if(METHOD == "usage"){
			json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			//render(text: json, contentType: "text/json", encoding: "UTF-8")
			return json
		}else{
			
			// check mandatory stuff here
			if(MiscUtils.checkParams(SupportedThings, params)){
				
				UC4ObjectName n = CommonAERequests.getUC4ObjectNameFromString(params.name, false);
				GetObjectDocu req = new GetObjectDocu(n);
				
				CommonAERequests.sendSyncRequest(conn, req, false)
				
				DocuContainer  container = req.docu();
				
				String[] TxtDocus = container.getTextDocuNames();
				String[] XMLDocus = container.getXmlDocuNames();
				
				ArrayList<TextDocumentation> TxtDocuArray = new ArrayList<TextDocumentation>();
				ArrayList<XMLDocumentation> XmlDocuArray = new ArrayList<XMLDocumentation>();
				def TxtDocuJSONContent = [];
				def XmlDocuJSONContent = [];
				
				if(TxtDocus.length>0){
					for(int i=0;i<TxtDocus.length;i++){
						TextDocumentation doctxt = container.textDocumentation(TxtDocus[i])
						TxtDocuArray.add(doctxt);
					}
					TxtDocuJSONContent = TxtDocuArray.collect {[
						name: it.name,
						type:it.type,
						content: it.content
						]}
				}
				
				if(XMLDocus.length>0){
					for(int i=0;i<XMLDocus.length;i++){
						println "DEBUG:" + XMLDocus[i]
						XMLDocumentation docxml = container.xmlDocumentation(XMLDocus[i])
						XmlDocuArray.add(docxml);
					}
					XmlDocuJSONContent = XmlDocuArray.collect {[
						name: it.name,
						type:it.type,
						rootname: it.root().name
						]}
				}
				
				json = new JsonBuilder(
					[
						status: "success",
						txtdocu: TxtDocuJSONContent,
						xmldocu: XmlDocuJSONContent
					  ]
				)

			return json
		}
		
		}
	}
	
	private static JsonBuilder getXMLDocuStructure(XMLDocuNode node){
		ArrayList<XMLDocuNode> childList = node.childNodes;
		
	}
}
