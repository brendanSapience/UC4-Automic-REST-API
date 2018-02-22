package com.automic.ae.actions.post

import com.uc4.api.TaskPromptSetName
import com.uc4.api.objects.PromptElement
import com.uc4.communication.Connection;
import com.uc4.communication.requests.AdoptTask
import com.uc4.communication.requests.SubmitPrompt
import com.uc4.communication.requests.TaskPromptSetContent
import com.uc4.communication.requests.TaskPromptSetNames
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
					'required_parameters': ['runid (format: runid= < integer >'],
					'optional_parameters': ['override (format: override=Y)'],
					'optional_filters': [],
					'required_methods': [],
					'optional_methods': ['usage']
					]
				
				String METHOD = params.method;
				String OVERRIDESTR = params.override;
				boolean OVERRIDE = false;

				
				if(OVERRIDESTR!=null && OVERRIDESTR.equalsIgnoreCase("Y")){OVERRIDE=true;}
				
				if(METHOD != null && METHOD.equals("usage")){
					def JsonFile = grailsattr.getApplicationContext().getResource(JsonTemplateFolder+"PromptsPOSTActions"+"_submitv1.json").getFile()
					def InputJSON = new JsonSlurper().parseText(JsonFile.text)
					return InputJSON
				}else{
				
					if(MiscUtils.checkParams(AllParamMap, params)){
						
						
						String RUNIDASSTR = params.runid;
						int RUNID = RUNIDASSTR.toInteger();
						
						
						
						def jsonSlurper = new JsonSlurper()
						def json = jsonSlurper.parseText(request.reader.text)
						

						def AllPrompts = json.prompts 
						AllPrompts.each{
							def PrptName = it.name;
							def AllElements = it.elements
							println "Debug: "+PrptName
							AllElements.each{
								println "Name:" + it.variable + " - Value:" + it.value
							}
						}
						// Turn the JSON into a HashMap of HashMaps
						HashMap<String, HashMap<String, String>> map = new HashMap<String, HashMap<String, String>>();
						if (OVERRIDE){
							
						}
						
						String Response = CommonAERequests.adoptTask(RUNID,conn);
						if(Response != null){
							return CommonJSONRequests.renderErrorAsJSON(Response);
						}

						TaskPromptSetNames prptNames = CommonAERequests.getTaskPromptsetNames(RUNID,conn)
						Iterator<TaskPromptSetName> it0 = prptNames.iterator();
						ArrayList<TaskPromptSetContent> AlLContents = new ArrayList<TaskPromptSetContent>();
						while(it0.hasNext()){
							TaskPromptSetName tName = it0.next();
							
							HashMap<String, String> AllVarsFromInput = map.get(tName.getName().getName());
							
							TaskPromptSetContent req = new TaskPromptSetContent(tName, RUNID);
							conn.sendRequestAndWait(req);
							
							Iterator<PromptElement> it1 = req.iterator();
								// for each element of the individual prompt..
							while(it1.hasNext()){
								// Get the values to update and update them.
								PromptElement elmt = it1.next();
								String VariableName = "&"+elmt.getVariable();
								String NewValue = "";
								if(OVERRIDE){NewValue = AllVarsFromInput.get(VariableName);}
								else{NewValue = elmt.getValue();}
								
								elmt.setValue(NewValue);
								
								}
							AlLContents.add(req);
						}
						
						TaskPromptSetContent[] ContentstockArr = new TaskPromptSetContent[AlLContents.size()];
						ContentstockArr = AlLContents.toArray(ContentstockArr);
						if(ContentstockArr.length>0){
							//System.out.println("\n\t %% Submitting Prompt(s) Now.");
							SubmitPrompt sumbit = new SubmitPrompt(prptNames, ContentstockArr);
							conn.sendRequestAndWait(sumbit);
							if(sumbit.getMessageBox() != null){
								return CommonJSONRequests.renderErrorAsJSON(Response);
							}else{
								return CommonJSONRequests.renderOKAsJSON("Submitted");
							}
						}
		
	
					}
					return CommonJSONRequests.renderErrorAsJSON("Debugging!");
				}
				
			}
		}
		

