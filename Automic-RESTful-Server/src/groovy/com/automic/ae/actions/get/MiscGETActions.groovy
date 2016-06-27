package com.automic.ae.actions.get

import com.automic.DisplayFilters
import com.uc4.api.DateTime
import com.uc4.api.SearchResultItem
import com.uc4.api.Task
import com.uc4.api.TaskFilter
import com.uc4.api.UC4ObjectName
import com.uc4.api.UC4TimezoneName
import com.uc4.api.TaskFilter.TimeFrame
import com.uc4.communication.Connection
import com.uc4.communication.requests.ActivityList
import com.uc4.communication.requests.AdoptTask
import com.uc4.communication.requests.CancelTask
import com.uc4.communication.requests.DeactivateTask
import com.uc4.communication.requests.ExecuteObject
import com.uc4.communication.requests.GenericStatistics
import com.uc4.communication.requests.GetComments
import com.uc4.communication.requests.GetSessionTZ
import com.uc4.communication.requests.QuitTask
import com.uc4.communication.requests.Report
import com.uc4.communication.requests.RestartTask
import com.uc4.communication.requests.ResumeTask
import com.uc4.communication.requests.RollbackTask
import com.uc4.communication.requests.SearchObject
import com.uc4.communication.requests.SuspendTask
import com.uc4.communication.requests.UnblockJobPlanTask
import com.uc4.communication.requests.UnblockWorkflow
import com.uc4.communication.requests.XMLRequest

import groovy.json.JsonBuilder

import com.automic.connection.AECredentials
import com.automic.connection.ConnectionManager
import com.automic.objects.CommonAERequests
import com.automic.utils.CommonJSONRequests
import com.automic.utils.MiscUtils
import com.uc4.communication.requests.GetComments.Comment
import com.automic.objects.AECrypter;

class MiscGETActions {

	/**
	 * @purpose this section contains all "routing" methods: routing methods call internal versionned methods. ex: "search" can call searchv1 or searchv2 etc. depending on the version in URL params
	 * @param version: action version to use to call the proper method
	 * @param params: all URL params
	 * @param conn: Connection object to AE
	 * @return JsonBuilder object
	 */
	
	public static def encrypt(String version, params,Connection conn,request, grailsattr,String TOKEN){return "encrypt${version}"(params,conn,TOKEN)}
	public static def decrypt(String version, params,Connection conn,request, grailsattr,String TOKEN){return "decrypt${version}"(params,conn,TOKEN)}
	
	/**
	 * @purpose encrypt a password or string (for usage in connection string in ini files)
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def encryptv1(params,Connection conn,String TOKEN){
		def SupportedThings = [:]
		SupportedThings = [
			'required_parameters': ['key (format: key=MyPasswordInClearText) -> clear password to encrypt'],
			'optional_parameters': ['internal (no value) -> only for internal use (requires binary key file)'],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage'],
			'developer_comment': 'requires admin privileges on this API'
			]
		
		boolean INTERNAL = false;
		String METHOD = params.method ?: '' 
		if(params.internal != null){INTERNAL = true}
		
		if( METHOD != null && METHOD.equalsIgnoreCase("usage")){
			JsonBuilder json
			if(ConnectionManager.getConnectionItemFromToken(TOKEN).isAdmin()){
				json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			}else{
				json = CommonJSONRequests.renderErrorAsJSON("request denied")
			}
			return json
		}else{
			// undocumented method for now?
			if(ConnectionManager.getConnectionItemFromToken(TOKEN).isAdmin()){
				String CLEARSTR = params.key;
				if(CLEARSTR == null || CLEARSTR.equals("")){
					return CommonJSONRequests.renderErrorAsJSON("parameter key cannot be empty.")
				}else{
				
					if(INTERNAL){
						if(AECrypter.isBinKeyFilePresent()){
							String EncStrWithFile = AECrypter.enMaximWithBinFile(CLEARSTR)
							//String EncStrWithFile = AECrypter.enMaximWithInternalKey(CLEARSTR)
							return new JsonBuilder([status: "success", encrypted: EncStrWithFile])
							
						}else{
							return CommonJSONRequests.renderErrorAsJSON("You havent said the magic word! No key file found on server.")
						}
					}else{
							String EncStrWithFile = AECrypter.enMaximWithInternalKey(CLEARSTR)
							return new JsonBuilder([status: "success", encrypted: EncStrWithFile])
					}
				}
			}else{
				return CommonJSONRequests.renderErrorAsJSON("request denied")
			}
		}
	}
	
	/**
	 * @purpose decrypt an encrypted password in form --10**********  (from connection string in ini files)
	 * @return JsonBuilder object
	 * @version v1
	 */
	public static def decryptv1(params,Connection conn,String TOKEN){
		def SupportedThings = [:]
		SupportedThings = [
			'required_parameters': ['key (format: key=--10EncP@ssW0rd) -> encrypted password to decrypt'],
			'optional_parameters': ['internal (no value) -> only for internal use (requires binary key file)'],
			'optional_filters': [],
			'required_methods': [],
			'optional_methods': ['usage'],
			'developer_comment': 'requires admin privileges on this API'
			]
		
		boolean INTERNAL = false;
		String METHOD = params.method;
		if(params.internal != null){INTERNAL = true}
		
		if( METHOD != null && METHOD.equalsIgnoreCase("usage")){
			JsonBuilder json
			if(ConnectionManager.getConnectionItemFromToken(TOKEN).isAdmin()){
				json = CommonJSONRequests.getSupportedThingsAsJSONFormat(SupportedThings);
			}else{
				json = CommonJSONRequests.renderErrorAsJSON("request denied")
			}
			return json
		}else{
			if(ConnectionManager.getConnectionItemFromToken(TOKEN).isAdmin()){
				String CLEARSTR = params.key;
				if(CLEARSTR == null || CLEARSTR.equals("")){
					return CommonJSONRequests.renderErrorAsJSON("parameter key cannot be empty.")
				}else{
					if(INTERNAL){
						if(AECrypter.isBinKeyFilePresent()){
							String EncStrWithFile = AECrypter.deMaximWithBinFile(CLEARSTR)
							return new JsonBuilder([status: "success", encrypted: EncStrWithFile])
							
						}else{
							return CommonJSONRequests.renderErrorAsJSON("You havent said the magic word! No key file found on server.")
						}
					}else{
						String EncStrWithFile = AECrypter.deMaximWithInternalKey(CLEARSTR)
						return new JsonBuilder([status: "success", encrypted: EncStrWithFile])
					}
	
				}
			}else{
				return CommonJSONRequests.renderErrorAsJSON("request denied")
			}
		}
	}
}
