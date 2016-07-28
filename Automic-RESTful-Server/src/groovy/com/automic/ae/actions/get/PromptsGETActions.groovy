package com.automic.ae.actions.get

import com.automic.DisplayFilters
import com.uc4.api.DateTime
import com.uc4.api.SearchResultItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter
import com.uc4.api.TaskPromptSetName
import com.uc4.api.UC4ObjectName
import com.uc4.api.UC4TimezoneName
import com.uc4.api.UC4UserName
import com.uc4.api.VersionControlListItem
import com.uc4.api.TaskFilter.TimeFrame
import com.uc4.api.objects.PromptElement
import com.uc4.api.objects.UC4Object
import com.uc4.api.prompt.CheckGroupElement
import com.uc4.api.prompt.ComboElement
import com.uc4.api.prompt.DateElement
import com.uc4.api.prompt.LabelElement
import com.uc4.api.prompt.NumberElement
import com.uc4.api.prompt.RadioGroupElement
import com.uc4.api.prompt.TextElement
import com.uc4.api.prompt.TimeElement
import com.uc4.api.prompt.TimeStampElement
import com.uc4.api.systemoverview.UserListItem
import com.uc4.communication.Connection
import com.uc4.communication.requests.ActivityList
import com.uc4.communication.requests.AdoptTask
import com.uc4.communication.requests.CalendarList
import com.uc4.communication.requests.CancelTask
import com.uc4.communication.requests.CheckAuthorizations
import com.uc4.communication.requests.CheckUserPrivileges
import com.uc4.communication.requests.DeactivateTask
import com.uc4.communication.requests.DisconnectUser
import com.uc4.communication.requests.ExecuteObject
import com.uc4.communication.requests.GenericStatistics
import com.uc4.communication.requests.GetChangeLog
import com.uc4.communication.requests.GetComments
import com.uc4.communication.requests.GetSessionTZ
import com.uc4.communication.requests.QuitTask
import com.uc4.communication.requests.Report
import com.uc4.communication.requests.RestartTask
import com.uc4.communication.requests.ResumeTask
import com.uc4.communication.requests.RollbackTask
import com.uc4.communication.requests.SearchObject
import com.uc4.communication.requests.SubmitPrompt
import com.uc4.communication.requests.SuspendTask
import com.uc4.communication.requests.TaskPromptSetContent
import com.uc4.communication.requests.TaskPromptSetNames
import com.uc4.communication.requests.UnblockJobPlanTask
import com.uc4.communication.requests.UnblockWorkflow
import com.uc4.communication.requests.UserList
import com.uc4.communication.requests.VersionControlList
import com.uc4.communication.requests.XMLRequest
import com.automic.spec.CALESpecDisplay
import groovy.json.JsonBuilder

import com.automic.connection.AECredentials
import com.automic.connection.ConnectionManager
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests
import com.automic.utils.MiscUtils
import com.uc4.communication.requests.CalendarList.CalendarListItem
import com.uc4.communication.requests.GetComments.Comment

class PromptsGETActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def check(String version, params,Connection conn,request, grailsattr){return "check${version}"(params,conn)}

	/**
	 * @purpose check if there are prompts waiting for info (based on a runid). Display info if there is.
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def checkv1(params,Connection conn) {
		
			def SupportedThings = [:]
			SupportedThings = [
				'required_parameters': ['runid (format: runid= < integer >'],
				'optional_parameters': [],
				'optional_filters': [],
				'required_methods': [],
				'optional_methods': ['usage'],
				'developer_comment': ''
				]
			
			//int runID = 1370078;
			String FILTERS = params.filters;
			String TOKEN = params.token;
			String METHOD = params.method ?: ''

			String RUNIDASSTR = params.runid;
			int RUNID = RUNIDASSTR.toInteger();
			
			if(METHOD == "usage"){
				JsonBuilder json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
				return json
			}else{
					// check mandatory stuff here
					boolean IsCurrentVersion = true;
					int VERSION = -1;
					if(MiscUtils.checkParams(SupportedThings, params)){
	
						// Take over a task first
						AdoptTask req = new AdoptTask(RUNID);
						conn.sendRequestAndWait(req);

						// Get a list of all Active Promptsets for the task
						TaskPromptSetNames AllTaskPromptNames = new TaskPromptSetNames(RUNID); 
						conn.sendRequestAndWait(AllTaskPromptNames); 
						Iterator<TaskPromptSetName> iteratorPrompt = AllTaskPromptNames.iterator(); 
						ArrayList<TaskPromptSetName> array = iteratorPrompt.toList();
						int SIZE = array.size();

						return new JsonBuilder(
							[
								status: "success",
								count: SIZE,
								data: array.collect {[
								name:it.getName().getName(),
								type: it.type.toString(),
								width:it.getWidth(),
								elements: getPromptContent(RUNID, it, conn),
								
								]}
							]
							)
					}else{
						JsonBuilder json = new JsonBuilder([status: "error", message: "missing mandatory parameters"])
						return json
					}
			}
		}
	
	private static def getPromptContent(int RUNID, TaskPromptSetName promptName, Connection conn){
		// We first retrieve the content of the promptset
		TaskPromptSetContent prompt = new TaskPromptSetContent(promptName, RUNID);
		conn.sendRequestAndWait(prompt);
		// Then we get the list of all prompt elements within that promptset
		ArrayList<PromptElement> listEl = prompt.promptsElements;
		
		return listEl.collect {[
			type:it.getTagName(),
			variable: it.getVariable(),
			//customfield:it.getCustomField(),
			//tooltip:it.getTooltip(),
			value:it.getValue(),
			values:it.getValues(),
			//mode:it.getMode(),
			data:getElementContent(it),
			]}
		
	}
	private static def getElementContent(PromptElement elem){
		
		if(elem.getTagName().equals("dyncheckgroup")){
			CheckGroupElement el = (CheckGroupElement)elem;
			return [
				caption:el.getCaption(),
				required:el.isValueRequired(),
				//dataref:el.getDataReference().getName(),
				//quotechar:el.getQuoteCharacter(),
				//xmlkey:el.getXmlKey(),
				//xpath:el.getXPath(),
				locked:el.isLocked(),
				possiblevalues:el.getItemList(),
				//separator:el.getMultiSelectSeparator(),
				]
		}
		
		// add CheckListElement (already covered above?!)
		
		if(elem.getTagName().equals("combo")){
			ComboElement el = (ComboElement)elem;
			return [
				caption:el.getCaption(),
				//dataref:el.dataReference.getName(),
				//quotechar:el.getQuoteCharacter(),
				//xmlkey:el.getXmlKey(),
				//xpath:el.getXPath(),
				dynamic:el.isDynamic(),
				locked:el.isLocked(),
				possiblevalues:el.getItemList(),
			]
		}
		
		// Add DateElement: Completed!
		if(elem.getTagName().equals("datefield")){
			DateElement el = (DateElement)elem;
			return [
				caption:el.getCaption(),
				//dataref:el.dataReference.getName(),
				calendarname:el.getCalendarCondition().name.getName(),
				calendarkeyword:el.getCalendarCondition().getKeyword().getName(),
				//maxvalue:el.getMaximumValue().toString(),
				//minvalue:el.getMinimumValue().toString(),
				outputformat:el.getOutputFormat(),
			]
		}
		
		// Add LabelElement: Completed!
		if(elem.getTagName().equals("label")){
			LabelElement el = (LabelElement)elem;
			return [
				caption:el.getCaption(),
			]
		}
		
		if(elem.getTagName().equals("integer")){
			NumberElement el = (NumberElement)elem;
			return [
				caption:el.getCaption(),
				//dataref:el.dataReference.getName(),
				//quotechar:el.getQuoteCharacter(),
				locked:el.isLocked(),
				maxvalue:el.getMaxValue(),
				minvalue:el.getMinValue(),
				]
		}
		
		// Add OnChangeResetElement: no need
		
		// Add RadioGroupElement: Completed!
		if(elem.getTagName().equals("dynradiogroup")){
			RadioGroupElement el = (RadioGroupElement)elem;
			return [
				caption:el.getCaption(),
				//dataref:el.dataReference.getName(),
				//quotechar:el.getQuoteCharacter(),
				locked:el.isLocked(),
				possiblevalues:el.getItemList(),
				]
		}
		
		if(elem.getTagName().equals("text")){
			TextElement el = (TextElement)elem;
			return [
				caption:el.getCaption(),
				required:el.isValueRequired(),
				//dataref:el.dataReference.getName(),
				//quotechar:el.getQuoteCharacter(),
				//xmlkey:el.getXmlKey(),
				//xpath:el.getXPath(),
				locked:el.isLocked(),
				
				maxlength:el.getMaxLength(),
				//separator:el.getMultiSelectSeparator(),
				//regexp:el.getRegExp(),
				//ismultiline:el.isMultiline(),
				ispassword:el.isShowAsPassword(),
				//isupper:el.isUpper(),
				required:el.isValueRequired(),
				]
		}
		
		// Add TimeElement: DONE
		if(elem.getTagName().equals("time")){
			TimeElement el = (TimeElement)elem;
			return [
				caption:el.getCaption(),
				//dataref:el.dataReference.getName(),
				maxvalue:el.getMaximumValue().toString(),
				minvalue:el.getMinimumValue().toString(),
				]
		}
		// Add TimeStampElement: DONE
		if(elem.getTagName().equals("timestamp")){
			TimeStampElement el = (TimeStampElement)elem;
			return [
				caption:el.getCaption(),
				//dataref:el.dataReference.getName(),
				calendarname:el.getCalendarCondition().name.getName(),
				calendarkeyword:el.getCalendarCondition().getKeyword().getName(),
				//maxvalue:el.getMaximumValue().toString(),
				//minvalue:el.getMinimumValue().toString(),
				outputformat:el.getOutputFormat(),
				]
		}

		return [elem.getTagName()];

	}
	
}
