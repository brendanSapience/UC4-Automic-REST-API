package com.automic.objects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.uc4.api.SearchResultItem;
import com.uc4.api.Task;
import com.uc4.api.TaskFilter;
import com.uc4.communication.Connection;
import com.uc4.communication.IResponseHandler;
import com.uc4.communication.TimeoutException;
import com.uc4.communication.requests.ActivityList;
import com.uc4.communication.requests.GenericStatistics;
import com.uc4.communication.requests.SearchObject;
import com.uc4.communication.requests.XMLRequest;

/**
 * 
 * @author bsp
 * @purpose handle common XML Requests to AE
 *
 */

public class CommonAERequests {

	public static XMLRequest sendSyncRequest(Connection connection, XMLRequest req, boolean verbose) throws TimeoutException, IOException{
		
		connection.sendRequestAndWait(req);
		
		if (req.getMessageBox() != null) {
			if(verbose){System.out.println(" -- "+req.getMessageBox().getText().toString().replace("\n", ""));}
			return null;
		}
		return req;
	}
	
	public static String sendSyncRequestWithMsgReturn(Connection connection, GenericStatistics req) throws TimeoutException, IOException{
		
		connection.sendRequestAndWait(req);
		
		if (req.getMessageBox() != null) {
			return req.getMessageBox().getText().toString().replace("\n", "");
		}else{
			return "";
		}
	}
	
	public static XMLRequest sendAsyncRequest(Connection connection, XMLRequest req, IResponseHandler resHandler, boolean verbose) throws TimeoutException, IOException{
		
		connection.sendRequest(req, resHandler);
		
		if (req.getMessageBox() != null) {
			if(verbose){System.out.println(" -- "+req.getMessageBox().getText().toString().replace("\n", ""));}
			return null;
		}
		return req;
	}
	
	public static String sendAsyncRequestWithMsgReturn(Connection connection, XMLRequest req, IResponseHandler resHandler) throws TimeoutException, IOException{
		
		connection.sendRequest(req, resHandler);
		
		if (req.getMessageBox() != null) {
			return req.getMessageBox().getText().toString().replace("\n", "");
		}else{
			return "";
		}

	}
	
	// Deprecated?
	public static List<SearchResultItem> SearchJobs(Connection conn, String ObjName) throws IOException{
		SearchObject req = new SearchObject();
		req.unselectAllObjectTypes();
		req.setTypeJOBS(true);
		
		return GenericSearchObjects(conn, ObjName, req);
	}
	
	public static List<SearchResultItem> GenericSearchObjects(Connection conn, String ObjName, SearchObject req) throws IOException{
		req.setName(ObjName);
		CommonAERequests.sendSyncRequest(conn, req, false);
		Iterator<SearchResultItem> it =  req.resultIterator();
		List<SearchResultItem> results = new ArrayList<SearchResultItem>();
		while(it.hasNext()){
			SearchResultItem item = it.next();
			results.add(item);
		}
	return results;
	}
	
	public static List<Task> getActivityWindowContent(Connection conn, TaskFilter taskFilter) throws IOException {		
		ActivityList req = new ActivityList(taskFilter);
		CommonAERequests.sendSyncRequest(conn, req, false);
		
		List<Task> tasks = new ArrayList<Task>();
		for (Task t : req) {
			tasks.add(t);
		}
		return tasks;
	}
	
}
