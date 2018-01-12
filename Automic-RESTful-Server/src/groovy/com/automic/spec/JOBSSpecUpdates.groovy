package com.automic.spec

import com.uc4.api.UC4HostName
import com.uc4.api.UC4ObjectName
import com.uc4.api.objects.AttributesSQL
import com.uc4.api.objects.CustomAttribute
import com.uc4.api.objects.Job
import com.uc4.api.objects.ObjectValues
import com.uc4.api.objects.OCVPanel.CITValue
import groovy.json.JsonBuilder
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import com.automic.utils.CommonJSONRequests
import com.automic.utils.MiscUtils

class JOBSSpecUpdates {

	public static def UpdateObject(Job obj,JsonUpdates,boolean Commit){
		
		
//		Iterator<CustomAttribute> myIt = obj.header.customAttributeIterator();
//		while(myIt.hasNext()){
//			CustomAttribute c = myIt.next();
//			println "DEBUG:"+c.getName()+":"+c.getValue();
//		}
		
		
		if(JsonUpdates.status != null && JsonUpdates.status.equalsIgnoreCase("active")){obj.header().setActive(true);}
		if(JsonUpdates.status != null && JsonUpdates.status.equalsIgnoreCase("inactive")){obj.header().setActive(false);}
		
		if(JsonUpdates.genatruntime != null && JsonUpdates.genatruntime == true){obj.attributes().setGenerateAtRuntime(true);}
		if(JsonUpdates.genatruntime != null && JsonUpdates.genatruntime == false){obj.attributes().setGenerateAtRuntime(false);}
		
		if(JsonUpdates.addvar != null && JsonUpdates.addvar != ""){
			String Value = "";
			if(JsonUpdates.addvar.split(",").length > 1){Value = JsonUpdates.addvar.split(",")[1]}
			obj.values().addValue(JsonUpdates.addvar.split(",")[0], Value,false);
		}
		if(JsonUpdates.delvar != null && JsonUpdates.delvar != ""){
			obj.values().removeValue(JsonUpdates.delvar);
		}
		if(JsonUpdates.updvar != null && JsonUpdates.updvar != ""){
			String[] args = MiscUtils.getPatternsFromStringArg(JsonUpdates.updvar,3)
			String VarName = args[0];
			String VarValueSourcePattern = args[1];
			String VarValueTarget = args[2];
			String FinalValueTarget = obj.values().getValue(VarName).replaceAll(VarValueSourcePattern, VarValueTarget)
			obj.values().addValue(VarName, FinalValueTarget,true);
		}
		if(JsonUpdates.title != null && JsonUpdates.title != ""){
			String[] args = MiscUtils.getPatternsFromStringArg(JsonUpdates.title,2)
				String SourcePattern = args[0];
				String ReplacementStr = args[1];
				String FinalValueTarget = obj.header().getTitle().replaceAll(args[0], args[1])
				obj.header().setTitle(FinalValueTarget);
		}
		if(JsonUpdates.key1 != null && JsonUpdates.key1 != ""){
			String[] args = MiscUtils.getPatternsFromStringArg(JsonUpdates.key1,2)
			String SourcePattern = args[0];
			String ReplacementStr = args[1];
			String FinalValueTarget = obj.header().getArchiveKey1().replaceAll(args[0], args[1])
			obj.header().setArchiveKey1(FinalValueTarget);
		}
		if(JsonUpdates.key2 != null && JsonUpdates.key2 != ""){
			String[] args = MiscUtils.getPatternsFromStringArg(JsonUpdates.key2,2)
			String SourcePattern = args[0];
			String ReplacementStr = args[1];
			String FinalValueTarget = obj.header().getArchiveKey2().replaceAll(args[0], args[1])
			obj.header().setArchiveKey2(FinalValueTarget);
		}
		if(JsonUpdates.host != null && JsonUpdates.host != ""){
			String[] args = MiscUtils.getPatternsFromStringArg(JsonUpdates.host,2)
			String SourcePattern = args[0];
			String ReplacementStr = args[1];
			String FinalValueTarget = obj.attributes().getHost().toString().replaceAll(args[0], args[1])
			obj.attributes().setHost(new UC4HostName(FinalValueTarget));
		}
		if(JsonUpdates.login != null && JsonUpdates.login != ""){
			String[] args = MiscUtils.getPatternsFromStringArg(JsonUpdates.login,2)
			String SourcePattern = args[0];
			String ReplacementStr = args[1];
			String FinalValueTarget = obj.attributes().getLogin().toString().replaceAll(args[0], args[1])
			obj.attributes().setLogin(new UC4ObjectName(FinalValueTarget));
		}
		if(JsonUpdates.queue != null && JsonUpdates.queue != ""){
			String[] args = MiscUtils.getPatternsFromStringArg(JsonUpdates.queue,2)
			String SourcePattern = args[0];
			String ReplacementStr = args[1];
			String FinalValueTarget = obj.attributes().getQueue().toString().replaceAll(args[0], args[1])
			obj.attributes().setQueue(new UC4ObjectName(FinalValueTarget));
		}
		if(JsonUpdates.process != null && JsonUpdates.process != ""){
			String[] args = MiscUtils.getPatternsFromStringArg(JsonUpdates.process,2)
			String SourcePattern = args[0];
			String ReplacementStr = args[1];
			String FinalValueTarget = obj.getProcess().replaceAll(args[0], args[1])
			obj.setProcess(FinalValueTarget);
		}
		if(JsonUpdates.preprocess != null && JsonUpdates.preprocess != ""){
			String[] args = MiscUtils.getPatternsFromStringArg(JsonUpdates.preprocess,2)
			String SourcePattern = args[0];
			String ReplacementStr = args[1];
			String FinalValueTarget = obj.getPreProcess().replaceAll(args[0], args[1])
			obj.setPreProcess(FinalValueTarget);
		}
		if(JsonUpdates.postprocess != null && JsonUpdates.postprocess != ""){
			String[] args = MiscUtils.getPatternsFromStringArg(JsonUpdates.postprocess,2)
			String SourcePattern = args[0];
			String ReplacementStr = args[1];
			String FinalValueTarget = obj.getPostProcess().replaceAll(args[0], args[1])
			obj.setPostProcess(FinalValueTarget);
		}
		
		// For SQL Jobs Specifically
		if(obj.getType().equals("JOBS_SQL")){
			AttributesSQL attr = (AttributesSQL) obj.hostAttributes();
			String CONNNAME = JsonUpdates.db_connname;
			String DBNAME = JsonUpdates.db_dbname;
			String SERVERNAME = JsonUpdates.db_servername;
			if(CONNNAME != null && !CONNNAME.equals("")){attr.setConnection(new UC4ObjectName(CONNNAME))}
			if(DBNAME != null && !DBNAME.equals("")){attr.setDatabaseName(DBNAME)}
			if(SERVERNAME != null && !SERVERNAME.equals("")){attr.setDatabaseServer(SERVERNAME)}
		}
		
		// For CIT/RA Jobs Specifically
		if(obj.getType().equals("JOBS_CIT")){
			
			// subjobtype is in job.getRAJobType()
			HashMap<String, String> UpdateValuesHash = new HashMap<>();
			JSONArray ValueList = JsonUpdates.ra_values;
			for(int i=0;i<ValueList.size();i++){
				JSONObject valueJSONObj = (JSONObject) ValueList.get(i);
				String myKey = valueJSONObj.keys().next();
				String myVal = valueJSONObj.get(myKey);
				UpdateValuesHash.put(myKey,myVal);
			}
			
			Iterator<CITValue> ItValues  = obj.ocvValues().iterator();
			while(ItValues.hasNext()){
				CITValue val = ItValues.next();
				if(UpdateValuesHash.containsKey(val.getXmlName())){
					val.setValue(UpdateValuesHash.get(val.getXmlName()));
				}
			}
			
		}
		
	}
	

	public static def getJSONStructure(ArrayList<Job> SelectedObjects,boolean commit){

		def data = [
			status: "success",
			commit: commit,
			count: SelectedObjects.size(),
			data: SelectedObjects.collect {[
				 name: it.name, 
				 title: it.header.title,
				 key1 :  it.header().getArchiveKey1(),
				 key2 :  it.header().getArchiveKey2(),
				 type:it.type, 
				 host: it.attributes().getHost(),
				 login: it.attributes().getLogin(),
				 queue: it.attributes().getQueue(),
				 genatruntime: it.attributes().isGenerateAtRuntime(),
				 access:it.access, 
				 active:it.header.isActive(),
				 jobtype:it.jobtype,
				 variables:CommonJSONRequests.getObjectVariablesAsJSON(it.values()),
				 prompts: CommonJSONRequests.getObjectPromptsAsJSON(it.values()),
				 process: it.getProcess(),
				 preprocess: it.getPreProcess(),
				 postprocess: it.getPostProcess(),
				// f:
				 ]}
			//properties:it.getProperties().toMapString()
		  ]

		def json = new JsonBuilder(data)
		return json;
	}
}
