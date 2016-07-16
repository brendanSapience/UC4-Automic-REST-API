package automic.restful.server

import com.automic.DisplayFilters
import com.uc4.api.DateTime
import com.uc4.api.SearchResultItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter;
import com.uc4.api.UC4ObjectName
import com.uc4.api.TaskFilter.TimeFrame;
import com.uc4.communication.requests.ActivityList
import com.uc4.communication.requests.DeactivateTask
import com.uc4.communication.requests.GenericStatistics;
import com.uc4.communication.requests.QuitTask
import com.uc4.communication.requests.RestartTask
import com.uc4.communication.requests.SearchObject
import com.uc4.communication.requests.UnblockJobPlanTask
import com.uc4.communication.requests.UnblockWorkflow
import com.uc4.communication.requests.XMLRequest

import grails.util.Environment
import groovy.json.JsonBuilder

import com.automic.ae.actions.get.ActivitiesGETActions;
import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.objects.CommonAERequests
import com.automic.utils.ActionClassUtils
import com.automic.utils.CommonJSONRequests;
import com.automic.utils.MiscUtils;

class GenericController {
	

	def showinfo = {
		
//		def grailsApplication
//		def Version = grailsAttributes.getGrailsApplication().
		
		def data = [
			name: "Rest API for Automic Platform (Workload & Release Automation)",
			version: "0.3",
			tag: "Beta",
			awaurl: "/api/awa/objects",
			araurl: "/api/ara/objects",
		  ]

		def jsonres = new JsonBuilder(data)
		render(text: jsonres, contentType: "text/json", encoding: "UTF-8")
		
	}
}
