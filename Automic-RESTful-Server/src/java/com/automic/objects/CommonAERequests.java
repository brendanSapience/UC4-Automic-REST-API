package com.automic.objects;

import groovy.json.JsonSlurper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.uc4.api.SearchResultItem;
import com.uc4.api.Task;
import com.uc4.api.TaskFilter;
import com.uc4.api.UC4HostName;
import com.uc4.api.UC4ObjectName;
import com.uc4.api.UC4UserName;
import com.uc4.api.objects.UC4Object;
import com.uc4.communication.Connection;
import com.uc4.communication.IResponseHandler;
import com.uc4.communication.TimeoutException;
import com.uc4.communication.requests.ActivityList;
import com.uc4.communication.requests.CloseObject;
import com.uc4.communication.requests.DeleteObject;
import com.uc4.communication.requests.GenericStatistics;
import com.uc4.communication.requests.GetSessionTZ;
import com.uc4.communication.requests.OpenObject;
import com.uc4.communication.requests.ResetOpenFlag;
import com.uc4.communication.requests.SaveObject;
import com.uc4.communication.requests.SearchObject;
import com.uc4.communication.requests.XMLRequest;

/**
 * 
 * @author bsp
 * @purpose handle common XML Requests to AE
 *
 */

public class CommonAERequests {

	public static String deleteObject(String name,Connection connection) throws IOException {

		UC4ObjectName objName = null;
		if (name.indexOf('/') != -1) objName = new UC4UserName(name);
		else objName = new UC4ObjectName(name);

		DeleteObject delete = new DeleteObject(objName);
		connection.sendRequestAndWait(delete);	
		if (delete.getMessageBox() != null) {
			return delete.getMessageBox().getText();
		}else{		
			return null;
		}
	}
	
	// Save an Automic Object (of any kind)
	public static String saveObject(UC4Object obj,Connection connection) throws IOException {
		//Say(" \t ++ Saving object: "+obj.getName()+"(Type: "+obj.getType()+")");
		SaveObject save = new SaveObject(obj);
		connection.sendRequestAndWait(save);
		if (save.getMessageBox() != null) {
			return "\t -- "+save.getMessageBox().getText().toString().replace("\n", "");
		}
		return null;
		
	}

	public static String reclaimObject(String ObjectName, Connection connection) throws TimeoutException, IOException{
		if(ObjectName.contains("/")){
			UC4UserName objName = new UC4UserName(ObjectName);
			ResetOpenFlag req = new ResetOpenFlag(objName);
			connection.sendRequestAndWait(req);	
			if (req.getMessageBox() != null) {
				return " -- "+req.getMessageBox().getText().toString().replace("\n", "");
			}else{
				return null;
			}
		}else{
			UC4ObjectName objName = new UC4ObjectName(ObjectName);
			ResetOpenFlag req = new ResetOpenFlag(objName);
			connection.sendRequestAndWait(req);	
			if (req.getMessageBox() != null) {
				return " -- "+req.getMessageBox().getText().toString().replace("\n", "");
			}else{
				//Say(" \t ++ Object: "+objName+" Successfully reclaimed");
				return null;
			}
		}
		
	}
	
	// close an Automic Object (of any kind)
	public static String closeObject(UC4Object obj,Connection connection) throws IOException {
		CloseObject close = new CloseObject(obj);
		connection.sendRequestAndWait(close);	
		if (close.getMessageBox() != null) {
			return " -- "+close.getMessageBox().getText().toString().replace("\n", "");
		}else{
			return null;
		}
	}	
	
	// close an Automic Object (of any kind)
		public static String saveAndCloseObject(UC4Object obj,Connection connection) throws IOException {
			SaveObject save = new SaveObject(obj);
			connection.sendRequestAndWait(save);
			if (save.getMessageBox() != null) {
			return " -- "+save.getMessageBox().getText().toString().replace("\n", "");
			}else{
				CloseObject close = new CloseObject(obj);
				connection.sendRequestAndWait(close);	
				if (close.getMessageBox() != null) {
					return "\t -- "+close.getMessageBox().getText().toString().replace("\n", "");
				}else{
					return null;
				}
			}
		}	
	public static UC4Object openObject(Connection connection, String name, boolean readOnly) throws IOException {
		//Say(" \t ++ Opening object: "+name);
		UC4ObjectName objName = null;
		if (name.indexOf('/') != -1) objName = new UC4UserName(name);
		else if (name.indexOf('-')  != -1) objName = new UC4HostName(name);
		else objName = new UC4ObjectName(name);		

		// last boolean returns an OpenObject
		OpenObject open = new OpenObject(objName,readOnly,true);
		connection.sendRequestAndWait(open);

		if (open.getMessageBox() != null) {
			System.err.println(" -- "+open.getMessageBox().toString().replace("\n", ""));
			return null;
		}
		return open.getUC4Object();
	}
	
	public static String getSessionTZ(Connection connection) throws TimeoutException, IOException{
		GetSessionTZ reqTZ = new GetSessionTZ();
		connection.sendRequestAndWait(reqTZ);

		if (reqTZ.getMessageBox() != null) {
			
			return null;
		}
		return reqTZ.getTimeZone().getDisplayName();
	}
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
