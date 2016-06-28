package com.automic.objects;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.ConnectException;

import groovy.json.JsonBuilder;

import com.automic.connection.ConnectionPoolItem;
import com.automic.utils.CommonJSONRequests;
import com.uc4.ara.feature.rm.CreateDeployPackage;
import com.uc4.ara.feature.rm.ExecuteApplicationWorkflow;
import com.uc4.ara.feature.rm.ExecuteGeneralWorkflow;
import com.uc4.ara.feature.rm.GetPackageState;
import com.uc4.ara.feature.rm.SetPackageState;

public class CommonARARequests {

	public static JsonBuilder createDeployPackage(String PCKNAME,String FOLDER,String TYPE,String APPNAME,ConnectionPoolItem item) throws Exception{
		
		// All ARA Requests will have the same workings:
		// 0- get mandatory parameters from the COnnectionPoolItem (thus leveraging what is built!)
		// 1- the rest of the parameters are simply passed to the method (they are checked before for presence)
		
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		//PCKNAME = "TestPackage1.3"
		//FOLDER = "84.51_POC"
		//TYPE = "84.51.POC"
		//APPNAME = "84.51_Oracle"
		
		// Params need to be passed in a String[]
		String[] PARAMS = {
							"--url",ARAURL,"--username",USERSTRING, "--password",PWD,
							  "-n", PCKNAME,"-o",USERSTRING, "-f",FOLDER , "-t",TYPE
							  ,"-a",APPNAME
						};


		// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
		ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
		
		CreateDeployPackage f = new CreateDeployPackage();
		f.initialize();
		int code = -1;
		try{
			code = f.run(PARAMS);
		}catch(ConnectException c){
			return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
		}
	    
	    // reverting to std sysout
	    ARACmdOutputManager.RevertStdOutputStream();
		String Msg = ARACmdOutputManager.extractMsg(baos.toString());

		// processing the textual output of the command
		if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
		else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not Create Package:  " + Msg, code, true);}	

	}
	public static JsonBuilder getPackageState(String PCKNAME,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		String[] PARAMS = {
							"--url",ARAURL,"--username",USERSTRING, "--password",PWD,
							  "--package", PCKNAME  
						};


		ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
		GetPackageState f = new GetPackageState();
		f.initialize();
		int code = -1;
		try{
			code = f.run(PARAMS);
		}catch(ConnectException c){
			return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
		}
	    ARACmdOutputManager.RevertStdOutputStream();
		String Msg = ARACmdOutputManager.extractMsg(baos.toString());
		
		// processing the textual output of the command
		if(baos.toString().contains("RESULT:")){
			String Status = baos.toString().split("RESULT:")[1].replace("\r\n","");
			String PackageID = baos.toString().split("state of package ")[1].split(" ")[0].replaceAll("'","");
			return CommonJSONRequests.renderPackageStateAsJSON(PCKNAME,PackageID, Status);	
		}
		return CommonJSONRequests.renderARAErrorAsJSON("Could Not Get Package State:  " + Msg, code, true);	
	}
	
	public static JsonBuilder setPackageState(String PCKNAME,String NEWSTATE,String CURRENTSTATE,String NOTMATCHING,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		String[] PARAMS = {
							"--url",ARAURL,"--username",USERSTRING, "--password",PWD,
							  "--package", PCKNAME,"--newstate",NEWSTATE, "--currentstate",CURRENTSTATE , "--notmatching",NOTMATCHING  
						};


		ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
		SetPackageState f = new SetPackageState();
		f.initialize();
		int code = -1;
		try{
			code = f.run(PARAMS);
		}catch(ConnectException c){
			return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
		}
	    ARACmdOutputManager.RevertStdOutputStream();
		String Msg = ARACmdOutputManager.extractMsg(baos.toString());
		
		// processing the textual output of the command
		if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
		else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not Change Package State:  " + Msg, code, true);}	

	}
	public static JsonBuilder executeApplicationWorkflow(String WFNAME,String APPNAME,String PCKNAME,String PROFILE, String SKIP,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		String[] PARAMS = {
							"--url",ARAURL,"--username",USERSTRING, "--password",PWD,
							  "--workflowname", WFNAME,
							  "--application",APPNAME,  
							  "--package",PCKNAME,   
							  "--profile",PROFILE,   
							  "--skipifinstalled",SKIP,   //optional YES or NO
		};
		
							 // "--startdate",SDATE, //optional
							 // "--manualstart",MANUALSTART , //optional YES or NO
							 // "--executor",EXECUTOR,   //optional
							//  "--queue",QUEUE,   //optional
							  
						

		ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
		ExecuteApplicationWorkflow f = new ExecuteApplicationWorkflow();
		f.initialize();
		int code = -1;
		try{
			code = f.run(PARAMS);
		}catch(ConnectException c){
			return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
		}
	    ARACmdOutputManager.RevertStdOutputStream();
		String Msg = ARACmdOutputManager.extractMsg(baos.toString());
		
		// processing the textual output of the command
		if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
		else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not Start Application Workflow:  " + Msg, code, true);}	

	}
	public static JsonBuilder executeGeneralWorkflow(String WFNAME,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		String[] PARAMS = {
							"--url",ARAURL,"--username",USERSTRING, "--password",PWD,
							  "--workflowname", WFNAME,
		};
		
							 // "--startdate",SDATE, //optional
							 // "--manualstart",MANUALSTART , //optional YES or NO
							 // "--executor",EXECUTOR,   //optional
							//  "--queue",QUEUE,   //optional
							  
						

		ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
		ExecuteGeneralWorkflow f = new ExecuteGeneralWorkflow();
		f.initialize();
		int code = -1;
		try{
			code = f.run(PARAMS);
		}catch(ConnectException c){
			return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
		}
	    ARACmdOutputManager.RevertStdOutputStream();
		String Msg = ARACmdOutputManager.extractMsg(baos.toString());
		
		// processing the textual output of the command
		if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
		else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not Start General Workflow:  " + Msg, code, true);}	

	}
}
