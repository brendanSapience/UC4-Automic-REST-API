package com.automic.objects;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import groovy.json.JsonBuilder;

import com.automic.connection.ConnectionPoolItem;
import com.automic.utils.CommonJSONRequests;
import com.uc4.ara.feature.rm.CreateDeployPackage;
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
	    int code = f.run(PARAMS);

	    // reverting to std sysout
	    ARACmdOutputManager.RevertStdOutputStream();
		String Msg = ARACmdOutputManager.extractMsg(baos.toString());

		// processing the textual output of the command
		if(code == 0){return CommonJSONRequests.renderARAOKAsJSON("Package Created. " + Msg, code, true);}	
		if(code == 999){return CommonJSONRequests.renderARAErrorAsJSON("Could not find Application specified. " + Msg, code, true);}	
		if(code == 4){return CommonJSONRequests.renderARAErrorAsJSON("Could not create Package. " + Msg, code, true);}	
		
		return CommonJSONRequests.renderARAErrorAsJSON("Error. "  + Msg, code, true);
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
	    int code = f.run(PARAMS);
	    ARACmdOutputManager.RevertStdOutputStream();
		String Msg = ARACmdOutputManager.extractMsg(baos.toString());

		// processing the textual output of the command
		if(code == 0){return CommonJSONRequests.renderARAOKAsJSON("Package Created. " + Msg, code, true);}	
		if(code == 999){return CommonJSONRequests.renderARAErrorAsJSON("Could not find Application specified. " + Msg, code, true);}	
		if(code == 4){return CommonJSONRequests.renderARAErrorAsJSON("Could not create Package. " + Msg, code, true);}	
		
		return CommonJSONRequests.renderARAErrorAsJSON("Error. "  + Msg, code, true);
	}
}
