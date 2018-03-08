package com.automic.ae.actions.post

import java.util.ArrayList;

import com.uc4.api.StatisticSearchItem;
import com.uc4.api.Task
import com.uc4.api.TaskPromptSetName
import com.uc4.api.objects.PromptElement
import com.uc4.communication.Connection;
import com.uc4.communication.requests.AdoptTask
import com.uc4.communication.requests.SubmitPrompt
import com.uc4.communication.requests.TaskPromptSetContent
import com.uc4.communication.requests.TaskPromptSetNames

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import com.automic.utils.MiscUtils
import com.automic.utils.CommonJSONRequests
import com.automic.objects.CommonAERequests

class PromptsPOSTActions {

	
	public static String JsonTemplateFolder = "./JsonPOSTSamples/"
	
	public static def submit(String version, params,Connection conn,request, grailsattr){return "submit${version}"(params,conn,request,grailsattr)}
	
	public static def submitv1(params,Connection conn,request,grailsattr) {
		
				def AllParamMap = [:]
				AllParamMap = [
					'required_parameters': ['runid (format: runid= < integer >','name (format: name= < string >'],
					'optional_parameters': ['override (format: override=Y)'],
					'optional_filters': [],
					'required_methods': [],
					'optional_methods': ['usage']
					]
				
				String METHOD = params.method;
				String OVERRIDESTR = params.override;
				boolean OVERRIDE = false;
				String RUNIDASSTR = params.runid;
				String OBJNAME = params.name;
				
				if(OVERRIDESTR!=null && OVERRIDESTR.equalsIgnoreCase("Y")){OVERRIDE=true;}
				
				if(METHOD != null && METHOD.equals("usage")){
					def JsonFile = grailsattr.getApplicationContext().getResource(JsonTemplateFolder+"PromptsPOSTActions"+"_submitv1.json").getFile()
					def InputJSON = new JsonSlurper().parseText(JsonFile.text)
					return InputJSON
				}else{
				
					if(MiscUtils.checkParams(AllParamMap, params)){
						String Body = request.reader.text
						if(Body == null || Body.empty){Body="{}";}
						int RUNID = -1
						try{
							RUNID = RUNIDASSTR.toInteger();
						}catch(NumberFormatException e){
							return CommonJSONRequests.renderErrorAsJSON("RUNID passed is in wrong format: "+RUNIDASSTR);
						}
						
						def json = new JsonSlurper().parseText(Body)
						
						// Turn the JSON into a HashMap of HashMaps
						HashMap<String, HashMap<String, String>> AllPromptsAndValues = new HashMap<String, HashMap<String, String>>();
						
						def AllPrompts = json.prompts 
						if(AllPrompts != null){
							AllPrompts.each{
								HashMap<String, String> AllValues = new HashMap<String,String>();
								def PrptName = it.name;
								def AllElements = it.elements
								//println "Debug: "+PrptName
								if(AllElements != null){
									AllElements.each{
										AllValues.put(it.variable, it.value)
										//println "Name:" + it.variable + " - Value:" + it.value
									}
								}
								AllPromptsAndValues.put(PrptName, AllValues)
							}
						}
						
						// We now have a HashMap of <String, HashMap<String, String>>, such as:
						// PromptSetName, HashMap<String,String>
						//					Prompt Variable Name1,Prompt Variable Value1
						//					Prompt Variable Name2,Prompt Variable Value2
						//					Prompt Variable Name3,Prompt Variable Value3
						
						ArrayList<Task> allStatsRes = CommonAERequests.getStatusFromRunid(conn,RUNID,OBJNAME);
						if(allStatsRes.size() == 0){
							return CommonJSONRequests.renderErrorAsJSON("The Task with Runid ["+RUNID+"] and name "+OBJNAME+" was not found.");
						}
						
						if(allStatsRes.get(0).getStatusCode() != 1301){
							return CommonJSONRequests.renderErrorAsJSON("The Task with Runid ["+RUNID+"] is not in 'Waiting for User Input' Status (Code 1301), current status: "+allStatsRes.get(0).getStatusCode());
						}
						
						String Response = CommonAERequests.adoptTask(RUNID,conn);
						if(Response != null){
							return CommonJSONRequests.renderErrorAsJSON(Response);
						}
						
						// Extracting the PromptSet Names from the RUNID first
						TaskPromptSetNames prptNames = CommonAERequests.getTaskPromptsetNames(RUNID,conn)
						Iterator<TaskPromptSetName> it0 = prptNames.iterator();
						ArrayList<TaskPromptSetContent> AlLContents = new ArrayList<TaskPromptSetContent>();
						
						// For Each PromptSet in the RunID
						while(it0.hasNext()){
							// Get the Prptset Name 
							TaskPromptSetName tName = it0.next();
							
							// Get the Values passed in the JSON Body corresponding to this Prompt
							HashMap<String, String> AllVarsFromInput = AllPromptsAndValues.get(tName.getName().getName());
							
							// Get the content of the promptset from the RUNID
							TaskPromptSetContent req = new TaskPromptSetContent(tName, RUNID);
							conn.sendRequestAndWait(req);

							// for each element of the individual prompt in the RUNID
							Iterator<PromptElement> it1 = req.iterator();
							while(it1.hasNext()){ 
								// Get the values to update and update them.
								PromptElement elmt = it1.next();
								String VariableName = "&"+elmt.getVariable();
								String NewValue = "";
								if(OVERRIDE && AllVarsFromInput != null && AllVarsFromInput.get(elmt.getVariable()) != null ){
									NewValue = AllVarsFromInput.get(elmt.getVariable());
									elmt.setValue(NewValue);
								}
							}
							AlLContents.add(req);
						}
						
						TaskPromptSetContent[] ContentstockArr = new TaskPromptSetContent[AlLContents.size()];
						ContentstockArr = AlLContents.toArray(ContentstockArr);
						if(ContentstockArr.length>0){
							SubmitPrompt sumbit = new SubmitPrompt(prptNames, ContentstockArr);
							conn.sendRequestAndWait(sumbit);
							if(sumbit.getMessageBox() != null){
								return CommonJSONRequests.renderErrorAsJSON(sumbit.getMessageBox().getText());
							}else{
								//return CommonJSONRequests.renderOKAsJSON("Prompt Submitted");
								return new JsonBuilder(
									[
										status: "success",
										
										data: AlLContents.collect {[
										entry:it
										]}
									]
									)
								//AlLContents
							}
						}
		
	
					}
					return CommonJSONRequests.renderErrorAsJSON("Debugging!");
				}
				
			}
		}
		

