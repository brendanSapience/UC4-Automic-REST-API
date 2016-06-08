package automic.restful.server

import com.automic.actions.AuthActions
import com.automic.connection.AECredentials;
import com.automic.connection.ConnectionManager;
import com.automic.utils.ActionClassUtils
import com.uc4.communication.Connection

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class AuthController {

	static String ConnectionSettingsFileName = "ConnectionConfig.json"
	
    def index() { }
	
	def help = {
		// all operations and all versions available - no list to maintained.. its dynamically calculated :)
		ActionClassUtils utils = new ActionClassUtils(new AuthActions().metaClass.methods*.name.unique())
		render(text: utils.getOpsAndVersionsAsJSON(), contentType: "text/json", encoding: "UTF-8")
	}
	
	def router = {
		String FILTERS = params.filters;
		String TOKEN = params.token;
		String VERSION = params.version;
		String METHOD = params.method;
		String OPERATION = params.operation;
		
		
		
		if(request.getHeader("Token")){TOKEN = request.getHeader("Token")};
		
			boolean NeedToken = true;
			if(OPERATION.equalsIgnoreCase("help") || OPERATION.equalsIgnoreCase("login")){NeedToken=false;}
			
			if(ConnectionManager.runTokenChecks(TOKEN)==null || !NeedToken){
				com.uc4.communication.Connection conn;
				if(NeedToken){conn = ConnectionManager.getConnectionFromToken(TOKEN);}
				
				// go to Actions and trigger $OPERATION$VERSION(params, conn)
				JsonBuilder myRes;
				try{
					if(OPERATION.equalsIgnoreCase("login")){
						def ConnectonFile = grailsAttributes.getApplicationContext().getResource(ConnectionSettingsFileName).getFile()
						myRes = com.automic.actions.AuthActions."${OPERATION}"(VERSION,params,ConnectonFile);
					}else{
						myRes = com.automic.actions.AuthActions."${OPERATION}"(VERSION,params,conn);
					}
					
				}catch(MissingMethodException){
					myRes = new JsonBuilder([status: "error", message: "version "+VERSION+" does not exist for operation: "+OPERATION])
				}
				render(text:  myRes, contentType: "text/json", encoding: "UTF-8")
			}else if(ConnectionManager.runTokenChecks(TOKEN)!=null && NeedToken){render(text:  ConnectionManager.runTokenChecks(TOKEN), contentType: "text/json", encoding: "UTF-8")}
	}
}
