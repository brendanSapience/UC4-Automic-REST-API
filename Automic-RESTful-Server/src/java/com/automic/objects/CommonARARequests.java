package com.automic.objects;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.util.ArrayList;

import groovy.json.JsonBuilder;

import com.automic.connection.ConnectionPoolItem;
import com.automic.utils.CommonJSONRequests;
import com.uc4.ara.feature.rm.ArchiveEntity;
import com.uc4.ara.feature.rm.CreateDeployPackage;
import com.uc4.ara.feature.rm.ExecuteApplicationWorkflow;
import com.uc4.ara.feature.rm.ExecuteGeneralWorkflow;
import com.uc4.ara.feature.rm.GetPackageState;
import com.uc4.ara.feature.rm.SetPackageState;

public class CommonARARequests {

	public static JsonBuilder archiveEntities(String ENTNAME,String ENTOWNER, String ENTFOLDER,String ENTTYPE, String CUSTOMENTTYPE,
			String STARTDATE,String ENDDATE,String CONDITIONS,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		
		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-t");TempParams.add(ENTTYPE);
		
		// optional params
		if(ENTNAME != null && !ENTNAME.equals("")){TempParams.add("-n");TempParams.add(ENTNAME);}
		if(ENTOWNER != null && !ENTOWNER.equals("")){TempParams.add("-o");TempParams.add(ENTOWNER);}
		if(ENTFOLDER != null && !ENTFOLDER.equals("")){TempParams.add("-f");TempParams.add(ENTFOLDER);}
		if(CUSTOMENTTYPE != null && !CUSTOMENTTYPE.equals("")){TempParams.add("-c");TempParams.add(CUSTOMENTTYPE);}
		if(STARTDATE != null && !STARTDATE.equals("")){TempParams.add("-s");TempParams.add(STARTDATE);}
		if(ENDDATE != null && !ENDDATE.equals("")){TempParams.add("-e");TempParams.add(ENDDATE);}
		if(CONDITIONS != null && !CONDITIONS.equals("")){TempParams.add("-co");TempParams.add(CONDITIONS);}
		
		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);
		
//		String[] PARAMS = {
//				"--url",ARAURL,"--username",USERSTRING, "--password",PWD,
//				  "-n", ENTNAME,"-o",ENTOWNER, "-f",ENTFOLDER , "-t",ENTTYPE, "-c", CUSTOMENTTYPE,
//				  "-s",STARTDATE,"-e",ENDDATE,"-co",CONDITIONS
//			};
		
		// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
				ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
				
				ArchiveEntity f = new ArchiveEntity();
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
		
		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-n");TempParams.add(PCKNAME);
		TempParams.add("-o");TempParams.add(USERSTRING);
		TempParams.add("-f");TempParams.add(FOLDER);
		TempParams.add("-t");TempParams.add(TYPE);
		TempParams.add("-a");TempParams.add(APPNAME);
		// optional params
		
		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);
						
		// Params need to be passed in a String[]
//		String[] PARAMS = {
//							"--url",ARAURL,"--username",USERSTRING, "--password",PWD,
//							  "-n", PCKNAME,"-o",USERSTRING, "-f",FOLDER , "-t",TYPE
//							  ,"-a",APPNAME
//						};


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
		
		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("--package");TempParams.add(PCKNAME);
//		
//		String[] PARAMS = {
//							"--url",ARAURL,"--username",USERSTRING, "--password",PWD,
//							  "--package", PCKNAME  
//						};
		
		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);
		
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
		

		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-pkg");TempParams.add(PCKNAME);
		TempParams.add("--newstate");TempParams.add(NEWSTATE);
		TempParams.add("--notmatching");TempParams.add(NOTMATCHING);
		
		if(CURRENTSTATE != null && !CURRENTSTATE.equals("")){TempParams.add("-c");TempParams.add(CURRENTSTATE);}
		
		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);
		
//		String[] PARAMS = {
//							"--url",ARAURL,"--username",USERSTRING, "--password",PWD,
//							  "--package", PCKNAME,"--newstate",NEWSTATE, "--currentstate",CURRENTSTATE , "--notmatching",NOTMATCHING  
//						};


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
		

		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-n");TempParams.add(WFNAME);
		TempParams.add("-a");TempParams.add(APPNAME);
		TempParams.add("-pkg");TempParams.add(PCKNAME);
		TempParams.add("-prf");TempParams.add(PROFILE);
		
		//if(STARTDATE != null && !STARTDATE.equals("")){TempParams.add("-d");TempParams.add(STARTDATE);}
		//if(MANUALSTART != null && !MANUALSTART.equals("")){TempParams.add("-m");TempParams.add(MANUALSTART);}
		//if(EXECUTOR != null && !EXECUTOR.equals("")){TempParams.add("-m");TempParams.add(EXECUTOR);}
		//if(QUEUE != null && !QUEUE.equals("")){TempParams.add("-m");TempParams.add(QUEUE);}
		//if(SKIPIFINSTALLED != null && !SKIPIFINSTALLED.equals("")){TempParams.add("-m");TempParams.add(SKIPIFINSTALLED);}
		
		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);
		
//		String[] PARAMS = {
//							"--url",ARAURL,"--username",USERSTRING, "--password",PWD,
//							  "--workflowname", WFNAME,
//							  "--application",APPNAME,  
//							  "--package",PCKNAME,   
//							  "--profile",PROFILE,   
//							  "--skipifinstalled",SKIP,   //optional YES or NO
//		};		

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
		
		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-n");TempParams.add(WFNAME);
		
		//if(STARTDATE != null && !STARTDATE.equals("")){TempParams.add("-d");TempParams.add(STARTDATE);}
		//if(MANUALSTART != null && !MANUALSTART.equals("")){TempParams.add("-m");TempParams.add(MANUALSTART);}
		//if(EXECUTOR != null && !EXECUTOR.equals("")){TempParams.add("-m");TempParams.add(EXECUTOR);}
		//if(QUEUE != null && !QUEUE.equals("")){TempParams.add("-m");TempParams.add(QUEUE);}
		//if(DYNAMICPROP != null && !DYNAMICPROP.equals("")){TempParams.add("-m");TempParams.add(DYNAMICPROP);}

		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);
		
//		String[] PARAMS = {
//							"--url",ARAURL,"--username",USERSTRING, "--password",PWD,
//							  "--workflowname", WFNAME,
//		};							  			

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
	
	
	private static ArrayList<String> getMandatoryParametersArray(String ARAURL, String USERSTRING, String PWD){
		ArrayList<String> TempParams = new ArrayList<String>();
		TempParams.add("--url");
		TempParams.add(ARAURL);
		TempParams.add("--username");
		TempParams.add(USERSTRING);
		TempParams.add("--password");
		TempParams.add(PWD);
		return TempParams;
	}
}
