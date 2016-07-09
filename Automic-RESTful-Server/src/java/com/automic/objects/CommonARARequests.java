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
import com.uc4.ara.feature.rm.CreateDeploymentTarget;
import com.uc4.ara.feature.rm.CreateEnvironment;
import com.uc4.ara.feature.rm.CreateProfile;
import com.uc4.ara.feature.rm.DeleteObject;
import com.uc4.ara.feature.rm.ExecuteApplicationWorkflow;
import com.uc4.ara.feature.rm.ExecuteGeneralWorkflow;
import com.uc4.ara.feature.rm.GetDeploymentProfileID;
import com.uc4.ara.feature.rm.GetDynamicProperty;
import com.uc4.ara.feature.rm.GetDynamicPropertyID;
import com.uc4.ara.feature.rm.GetFolder;
import com.uc4.ara.feature.rm.GetOwner;
import com.uc4.ara.feature.rm.GetPackageState;
import com.uc4.ara.feature.rm.SetDynamicProperty;
import com.uc4.ara.feature.rm.SetFolder;
import com.uc4.ara.feature.rm.SetOwner;
import com.uc4.ara.feature.rm.SetPackageState;

public class CommonARARequests {

	//deleteObject(NAME,TYPE,FAILIFMISSING,ConnectionManager.getConnectionItemFromToken(TOKEN));
	public static JsonBuilder deleteObject(String NAME, String TYPE, boolean FAILIFMISSING,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		
		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-n");TempParams.add(NAME);
		TempParams.add("-t");TempParams.add(TYPE);
		if(FAILIFMISSING){TempParams.add("-fm");TempParams.add("YES");};
		if(!FAILIFMISSING){TempParams.add("-fm");TempParams.add("NO");};
		
		// optional params
		
		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);

		// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
				ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
				
				DeleteObject f = new DeleteObject();
				f.initialize();
				int code = -1;
				try{
					code = f.run(PARAMS);
				}catch(ConnectException c){
					return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
				}catch(RuntimeException r){
					return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
				}
			    
			    // reverting to std sysout
			    ARACmdOutputManager.RevertStdOutputStream();
				String Msg = ARACmdOutputManager.extractMsg(baos.toString());

				// processing the textual output of the command
				if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
				else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not Delete Object:  " + Msg, code, true);}	
	}
	
	// CommonARARequests.setProperty(NAME,OBJNAME,VALUE,VALUETYPE,PROPTYPE,OBJTYPE,NAMESPACE,HIGHLIGHTED,FAILIFDIFFERS,FAILIFEXISTS,ConnectionManager.getConnectionItemFromToken(TOKEN));
	public static JsonBuilder setProperty(String NAME, String OBJNAME, String VALUE, String VALUETYPE,String PROPTYPE, String OBJTYPE,
			String NAMESPACE,String HIGHLIGHTED, String FAILIFDIFFERS, String FAILIFEXISTS,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		
		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-n");TempParams.add(NAME);
		TempParams.add("-on");TempParams.add(OBJNAME);
		TempParams.add("-v");TempParams.add(VALUE);
		TempParams.add("-vt");TempParams.add(VALUETYPE);
		TempParams.add("-pt");TempParams.add(PROPTYPE);
		
		// optional params
		if(OBJTYPE != null && !OBJTYPE.equals("")){TempParams.add("-ot");TempParams.add(OBJTYPE);}
		if(NAMESPACE != null && !NAMESPACE.equals("")){TempParams.add("-ns");TempParams.add(NAMESPACE);}
		if(HIGHLIGHTED != null && !HIGHLIGHTED.equals("")){TempParams.add("-h");}
		if(FAILIFDIFFERS != null && !FAILIFDIFFERS.equals("")){TempParams.add("-fd");}
		if(FAILIFEXISTS != null && !FAILIFEXISTS.equals("")){TempParams.add("-fe");}
		
		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);

		// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
				ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
				
				SetDynamicProperty f = new SetDynamicProperty();
				f.initialize();
				int code = -1;
				try{
					code = f.run(PARAMS);
				}catch(ConnectException c){
					return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
				}catch(RuntimeException r){
					return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
				}
			    
			    // reverting to std sysout
			    ARACmdOutputManager.RevertStdOutputStream();
				String Msg = ARACmdOutputManager.extractMsg(baos.toString());

				// processing the textual output of the command
				if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
				else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not set Owner:  " + Msg, code, true);}	
	}
	
	
	public static JsonBuilder setOwner(String OBJNAME, String OBJTYPE,String OWNER,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		
		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-n");TempParams.add(OBJNAME);
		TempParams.add("-t");TempParams.add(OBJTYPE);
		TempParams.add("-o");TempParams.add(OWNER);
		
		// optional params

		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);

		// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
				ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
				
				SetOwner f = new SetOwner();
				f.initialize();
				int code = -1;
				try{
					code = f.run(PARAMS);
				}catch(ConnectException c){
					return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
				}catch(RuntimeException r){
					return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
				}
			    
			    // reverting to std sysout
			    ARACmdOutputManager.RevertStdOutputStream();
				String Msg = ARACmdOutputManager.extractMsg(baos.toString());

				// processing the textual output of the command
				if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
				else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not set Owner:  " + Msg, code, true);}	
	}
	
	public static JsonBuilder getOwner(String OBJNAME, String OBJTYPE,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		
		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-n");TempParams.add(OBJNAME);
		TempParams.add("-t");TempParams.add(OBJTYPE);
		
		// optional params

		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);

		// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
				ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
				
				GetOwner f = new GetOwner();
				f.initialize();
				int code = -1;
				try{
					code = f.run(PARAMS);
				}catch(ConnectException c){
					return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
				}catch(RuntimeException r){
					return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
				}
			    
			    // reverting to std sysout
			    ARACmdOutputManager.RevertStdOutputStream();
				String Msg = ARACmdOutputManager.extractMsg(baos.toString());

				// processing the textual output of the command
				if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
				else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not get Owner:  " + Msg, code, true);}	
	}
	
	public static JsonBuilder setFolder(String OBJNAME, String OBJTYPE,String FOLDER,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		
		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-n");TempParams.add(OBJNAME);
		TempParams.add("-t");TempParams.add(OBJTYPE);
		TempParams.add("-f");TempParams.add(FOLDER);
		
		// optional params

		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);

		// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
				ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
				
				SetFolder f = new SetFolder();
				f.initialize();
				int code = -1;
				try{
					code = f.run(PARAMS);
				}catch(ConnectException c){
					return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
				}catch(RuntimeException r){
					return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
				}
			    
			    // reverting to std sysout
			    ARACmdOutputManager.RevertStdOutputStream();
				String Msg = ARACmdOutputManager.extractMsg(baos.toString());

				// processing the textual output of the command
				if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
				else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not set Folder:  " + Msg, code, true);}	
	}
	
	public static JsonBuilder getFolder(String OBJNAME, String OBJTYPE,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		
		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-n");TempParams.add(OBJNAME);
		TempParams.add("-t");TempParams.add(OBJTYPE);
		
		// optional params

		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);

		// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
				ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
				
				GetFolder f = new GetFolder();
				f.initialize();
				int code = -1;
				try{
					code = f.run(PARAMS);
				}catch(ConnectException c){
					return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
				}catch(RuntimeException r){
					return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
				}
			    
			    // reverting to std sysout
			    ARACmdOutputManager.RevertStdOutputStream();
				String Msg = ARACmdOutputManager.extractMsg(baos.toString());
				// processing the textual output of the command
				if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
				else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not get Folder:  " + Msg, code, true);}	
	}
	
	public static JsonBuilder getProperty(String NAME, String OBJNAME, String OBJTYPE, String NAMESPACE, String FAILIFMISSING,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		
		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-n");TempParams.add(NAME);
		TempParams.add("-on");TempParams.add(OBJNAME);
		
		// optional params
		if(OBJTYPE != null && !OBJTYPE.equals("")){TempParams.add("-ot");TempParams.add(OBJTYPE);}
		if(NAMESPACE != null && !NAMESPACE.equals("")){TempParams.add("-ns");TempParams.add(NAMESPACE);}
		if(FAILIFMISSING != null && !FAILIFMISSING.equals("")){TempParams.add("-fm");}
		
		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);

		// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
				ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
				
				GetDynamicProperty f = new GetDynamicProperty();
				f.initialize();
				int code = -1;
				try{
					code = f.run(PARAMS);
				}catch(ConnectException c){
					return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
				}catch(RuntimeException r){
					return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
				}
			    
			    // reverting to std sysout
			    ARACmdOutputManager.RevertStdOutputStream();
				String Msg = ARACmdOutputManager.extractMsg(baos.toString());

				// processing the textual output of the command
				if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
				else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not get Dynamic Property:  " + Msg, code, true);}	
	}
	
	//CommonARARequests.getPropertyID(NAME,FOLDER,OBJTYPE,NAMESPACE,ConnectionManager.getConnectionItemFromToken(TOKEN));
	public static JsonBuilder getPropertyID(String NAME, String OBJNAME, String OBJTYPE, String NAMESPACE,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		
		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-n");TempParams.add(NAME);
		TempParams.add("-on");TempParams.add(OBJNAME);
		
		// optional params
		if(OBJTYPE != null && !OBJTYPE.equals("")){TempParams.add("-ot");TempParams.add(OBJTYPE);}
		if(NAMESPACE != null && !NAMESPACE.equals("")){TempParams.add("-ns");TempParams.add(NAMESPACE);}

		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);

		// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
				ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
				
				GetDynamicPropertyID f = new GetDynamicPropertyID();
				f.initialize();
				int code = -1;
				try{
					code = f.run(PARAMS);
				}catch(ConnectException c){
					return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
				}catch(RuntimeException r){
					return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
				}
			    
			    // reverting to std sysout
			    ARACmdOutputManager.RevertStdOutputStream();
				String Msg = ARACmdOutputManager.extractMsg(baos.toString());

				// processing the textual output of the command
				if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
				else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not get Property ID:  " + Msg, code, true);}	
	}
	
	//createProfile(NAME,ENV,APP,OWNER,FOLDER,LOGIN,FAILIFEXISTS,ConnectionManager.getConnectionItemFromToken(TOKEN));
		public static JsonBuilder getProfileID(String NAME, String APP,ConnectionPoolItem item) throws Exception{
			String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
			String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
			String PWD = item.getPassword(); //"ara"
			
			ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

			// mandatory params
			TempParams.add("-pfl");TempParams.add(NAME);
			TempParams.add("-app");TempParams.add(APP);
			
			// optional params

			String[] PARAMS = new String[TempParams.size()];
			PARAMS = TempParams.toArray(PARAMS);

			// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
					ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
					
					GetDeploymentProfileID f = new GetDeploymentProfileID();
					f.initialize();
					int code = -1;
					try{
						code = f.run(PARAMS);
					}catch(ConnectException c){
						return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
					}catch(RuntimeException r){
						return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
					}
				    
				    // reverting to std sysout
				    ARACmdOutputManager.RevertStdOutputStream();
					String Msg = ARACmdOutputManager.extractMsg(baos.toString());

					// processing the textual output of the command
					if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
					else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not get Profile ID:  " + Msg, code, true);}	
		}
		
	//createProfile(NAME,ENV,APP,OWNER,FOLDER,LOGIN,FAILIFEXISTS,ConnectionManager.getConnectionItemFromToken(TOKEN));
	public static JsonBuilder createProfile(String NAME,String ENV, String APP,String OWNER,String FOLDER, String LOGIN,String FAILIFEXISTS,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		
		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-n");TempParams.add(NAME);
		TempParams.add("-f");TempParams.add(FOLDER);
		TempParams.add("-e");TempParams.add(ENV);
		TempParams.add("-a");TempParams.add(APP);
		TempParams.add("-o");TempParams.add(OWNER);
		
		// optional params
		if(LOGIN != null && !LOGIN.equals("")){TempParams.add("-l");TempParams.add(LOGIN);}
		if(FAILIFEXISTS != null && !FAILIFEXISTS.equals("")){TempParams.add("-fe");TempParams.add(FAILIFEXISTS);}

		
		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);
		
//		String[] PARAMS = {
//				"--url",ARAURL,"--username",USERSTRING, "--password",PWD,
//				  "-n", ENTNAME,"-o",ENTOWNER, "-f",ENTFOLDER , "-t",ENTTYPE, "-c", CUSTOMENTTYPE,
//				  "-s",STARTDATE,"-e",ENDDATE,"-co",CONDITIONS
//			};
		
		// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
				ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
				
				CreateProfile f = new CreateProfile();
				f.initialize();
				int code = -1;
				try{
					code = f.run(PARAMS);
				}catch(ConnectException c){
					return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
				}catch(RuntimeException r){
					return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
				}
			    
			    // reverting to std sysout
			    ARACmdOutputManager.RevertStdOutputStream();
				String Msg = ARACmdOutputManager.extractMsg(baos.toString());

				// processing the textual output of the command
				if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
				else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not Create Profile:  " + Msg, code, true);}	
	}
	public static JsonBuilder createEnvironment(String NAME,String FOLDER, String TYPE,String OWNER,String FAILIFEXISTS,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		
		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-n");TempParams.add(NAME);
		TempParams.add("-f");TempParams.add(FOLDER);
		TempParams.add("-t");TempParams.add(TYPE);
		
		// optional params
		if(OWNER != null && !OWNER.equals("")){TempParams.add("-o");TempParams.add(OWNER);}
		if(FAILIFEXISTS != null && !FAILIFEXISTS.equals("")){TempParams.add("-fe");TempParams.add(FAILIFEXISTS);}

		
		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);

		// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
				ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
				
				CreateEnvironment f = new CreateEnvironment();
				f.initialize();
				int code = -1;
				try{
					System.out.println("GAGA0");
					code = f.run(PARAMS);
					System.out.println("GAGA1");
				}catch(ConnectException c){
					return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
				}catch(RuntimeException r){
					return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
				}
			    
			    // reverting to std sysout
			    ARACmdOutputManager.RevertStdOutputStream();
				String Msg = ARACmdOutputManager.extractMsg(baos.toString());

				// processing the textual output of the command
				if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
				else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not Create Environment:  " + Msg, code, true);}	
	}
	
	// CommonARARequests.createTarget(NAME,FOLDER,TYPE,OWNER,ENVIRONMENTID,,AGENT,FAILIFEXISTS,ConnectionManager.getConnectionItemFromToken(TOKEN));

	public static JsonBuilder createTarget(String NAME,String FOLDER, String TYPE,String OWNER, String ENVIRONMENTID,
			String AGENT,String FAILIFEXISTS,ConnectionPoolItem item) throws Exception{
		String ARAURL = item.getARAUrl(); //"http://192.168.17.136/ARA"
		String USERSTRING = item.getClient()+"/"+item.getUser()+"/"+item.getDept(); //"100/ARA/ARA"
		String PWD = item.getPassword(); //"ara"
		
		ArrayList<String> TempParams = getMandatoryParametersArray(ARAURL,USERSTRING,PWD);

		// mandatory params
		TempParams.add("-n");TempParams.add(NAME);
		TempParams.add("-f");TempParams.add(FOLDER);
		TempParams.add("-t");TempParams.add(TYPE);
		
		// optional params
		if(OWNER != null && !OWNER.equals("")){TempParams.add("-o");TempParams.add(OWNER);}
		if(ENVIRONMENTID != null && !ENVIRONMENTID.equals("")){TempParams.add("-e");TempParams.add(ENVIRONMENTID);}
		if(AGENT != null && !AGENT.equals("")){TempParams.add("-a");TempParams.add(AGENT);}
		if(FAILIFEXISTS != null && !FAILIFEXISTS.equals("")){TempParams.add("-fe");TempParams.add(FAILIFEXISTS);}

		
		String[] PARAMS = new String[TempParams.size()];
		PARAMS = TempParams.toArray(PARAMS);

		// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
				ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
				
				CreateDeploymentTarget f = new CreateDeploymentTarget();
				f.initialize();
				int code = -1;
				try{
					code = f.run(PARAMS);
				}catch(ConnectException c){
					return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
				}catch(RuntimeException r){
					return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
				}
			    
			    // reverting to std sysout
			    ARACmdOutputManager.RevertStdOutputStream();
				String Msg = ARACmdOutputManager.extractMsg(baos.toString());

				// processing the textual output of the command
				if(code == 0){return CommonJSONRequests.renderARAOKAsJSON(Msg, code, true);}	
				else{return CommonJSONRequests.renderARAErrorAsJSON("Could Not Create Deployment Target:  " + Msg, code, true);}	
	}
	
	
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

		// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
				ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
				
				ArchiveEntity f = new ArchiveEntity();
				f.initialize();
				int code = -1;
				try{
					code = f.run(PARAMS);
				}catch(ConnectException c){
					return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
				}catch(RuntimeException r){
					return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
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

		// the dm-tools.jar just outputs everything to stdout.. overriding this behavior temporarily
		ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
		
		CreateDeployPackage f = new CreateDeployPackage();
		f.initialize();
		int code = -1;
		try{
			code = f.run(PARAMS);
		}catch(ConnectException c){
			return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
		}catch(RuntimeException r){
			return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
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
		}catch(RuntimeException r){
			return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
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
		
		ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
		SetPackageState f = new SetPackageState();
		f.initialize();
		int code = -1;
		try{
			code = f.run(PARAMS);
		}catch(ConnectException c){
			return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
		}catch(RuntimeException r){
			return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
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
		
		ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
		ExecuteApplicationWorkflow f = new ExecuteApplicationWorkflow();
		f.initialize();
		int code = -1;
		try{
			code = f.run(PARAMS);
		}catch(ConnectException c){
			return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
		}catch(RuntimeException r){
			return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
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

		ByteArrayOutputStream baos = ARACmdOutputManager.OverrideStdOutputStream();
		ExecuteGeneralWorkflow f = new ExecuteGeneralWorkflow();
		f.initialize();
		int code = -1;
		try{
			code = f.run(PARAMS);
		}catch(ConnectException c){
			return CommonJSONRequests.renderARAErrorAsJSON("Cannot connect to ARA Url: "+ARAURL, code, true);
		}catch(RuntimeException r){
			return CommonJSONRequests.renderARAErrorAsJSON("Error: "+ r.getMessage(), code, true);
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
