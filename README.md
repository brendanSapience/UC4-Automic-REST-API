# UC4-Automic-REST-API

Full documentation here: 

[https://github.com/brendanSapience/UC4-Automic-REST-API-Delivery/blob/master/Automic%20REST%20API%20Documentation.pdf]




REST API Server for Automic's ONE Automation Platform

**Structure:**

     http://<server name>:<port>/api/<product>/<action name>/<version>/<object type>?method=<method>&<all required URL parameters>

* **with:**

     - **server name**: hostname of server hosting the REST API app
     - **port**: port number of the REST API app
     - **api**: api (generic placeholder in case something else needs to sit on the same server / port)
     - **product**: automic product line: ara or awa (nothing else planned at this point)
     - **action name**: action to carry out, ex: display / update / delete / find / move etc. 
     - **version**: version of the API + Product + Action (allows more granular releases)
     - **object type**: type of objects to be considered: Jobs / Jobp / Workflows / All etc.
     - **method**: method to execute. Not always used: Usually only to provide additional info & context to a given action
     - **URL parameters**: parameters required for the <method> & <api category> combination. ex: filters=[status:1800,type:JOBP] or search_usage=Y, etc.
     
**Important Additional Design Aspects:**

     - The authentication token can be either passed in every call as a URL parameter ("token=") or passed as a request header parameter (as 'token').
     - IF the Auth token is specified in the request header 'token' url parameter becomes unnecessary.
     - Tokens are non-persistent (they are not kept if the Server shuts down as they are tied to individual connection objects).
     - Tokens expire after a certain configurable period (is configured in the ConnectionConfig.json file).
     - Only JSON outputs are supported
     - Tokens are uniquely & randomly generated and do not encode any specific information.
     - There are "helpers" url parameters at different levels to provide usage info.
     - passive operations (display/show etc.) require GET. 
     - some active operations (update) require POST (and a JSON body along with it).
     - a COMMIT parameter is required for POST requests (otherwise only a simulation runs)
     - Every action is versionned: this is to facilitate backwards compatibility in the future
     - Filters are designed in a specific way and provided as a url parameter called filters:
     		filters=[FilterName:FilterValue]
     		=> the FilterNames available obviously depend on the Object & Action you are working with
     		=> the FilterValues also depend on the filterName. Some takes only Integers, some UC4Regex, some actual Regex, some have more specific formats
     		=> FilterNames & FilterValues can be easily chained (as long as they are separated by commas). Ex: filters=[name=NOVA*,status=1800,type=JOBF|JOBS]
     
   **How to Get Help (and get started):**
   
* there is 2 **special URL Calls**, one for **awa**, one for **ara**, that you can use in order to **retrieve the list of available < objects >** that are currently supported:

	http://localhost:8080/UC4Rest/api/ara/objects
	
	http://localhost:8080/UC4Rest/api/awa/objects 
	
	{
		 "status": "success",
		 "count": 3,
		 "objects": [
		   "Packages",
		   "Workflows",
		   "Activities"
		 ]
	} 
   
* there is a **special < action >** called **"help"** you can always use in order to **retrieve the list of available < actions >** for a given object type (see above) & http request type (GET or POST):
     	
  ex: http://localhost:8080/Automic-RESTful-Server/api/awa/**help**/v1/Activities
     	
     	Returns:
     	
     	{
			"success": true,
			"count": 3,
			"data": [
				{
					"operation": "search",
					"versions" : ["searchv1","searchv2"]
				},
				{
					"operation": "rerun",
					"versions" : ["rerunv1"]
				},
				{
					"operation": "unblock",
					"versions" : ["unblockv1"]
				}
			]
		}
     	
     => this means that there are **3 < api categories >** you can use with Activities (search, rerun & unblock), where the search category has 2 version available.
     	
* There is a special **< method >** called **"usage"** you can always use in order to retrieve the **list of parameters / filters required** (for GET) or the **required / available JSON body structure** (for POST) for a given **Object Type**, **version** & **api category** combination:
     	
  ex: GET http://localhost:8080/Automic-RESTful-Server/api/awa/search/v1/Activities?**method=usage**
     	
     	Returns:
     	
     	{
			"success": true,
			"required_parameters": [],
			"optional_parameters": [],
			"optional_filters": [
				"status (format: filters=[status:1900])",
				"key1 (format: filters=[key1:*.*]",
				"type (format: filters=[type:JOBF])"
			],
			"required_methods": [],
			"optional_methods": [
				"usage"
			]
		}

	=> This means that the **search < api category >** in combination with object **Activities** for **version 1** needs to leverage GET and has **no required parameters** and **3 optional filters** you can use.
		
  ex: POST http://localhost:8080/Automic-RESTful-Server/api/awa/update/v1/Jobs?**method=usage**
     	
     	Returns:
     	
		{
		  "spec_filters": {
		    "active": "true|false",
		    "inactive": "true|false"
		  },
		  "std_filters": {
		    "date_from": "YYYYMMDDhhmm",
		    "date_search": false,
		    "date_search_type": "created|modified|used",
		    "date_to": "YYYYMMDDhhmm",
		    "include_links": true,
		    "name": "*",
		    "search_for_usage": false,
		    "search_text": false,
		    "search_text_docu": false,
		    "search_text_key": false,
		    "search_text_process": false,
		    "search_text_title": false,
		    "text": "*"
		  },
		  "updates": {
		    "status": "active|inactive"
		  }
		}

	=> This means that the **update < api category >** in combination with object **Jobs** for **version 1** needs to leverage POST and pass a JSON structure similar to what is returned above.
				
       
**Available Methods & API Categories:**

* **Authentication (login) (GET):**
     
    Ex: /api/awa/**login**/v1/**Auth**?**login**=BSP&**pwd**=Un1ver$e&**connection**=AEPROD&**client**=200

     * **Mandatory Parameters:**
     
          * _login_ (AE Connection login)
          * _pwd_ (AE Connection password)
          * _client_ (AE Client number)
          * _connection_ (AE Connection Name as specified in the REST API configuration file called connection_config.json)
          
     * Returns:
     
        {'status':'success','token':'s9dpur80s8rtvharifrm531387','expdate':'20161231235959'}
        
* **Authentication (logout) (GET):**
     
    Ex: /api/awa/**logout**/v1/**Auth**?**token**=123fjh324gf234k234

     * **Mandatory Parameters:**
     
          * _token_ (your auth token)
          

* **Search (GET):**

     * Supported Objects:
     
          * **Jobs** (Replaced by All!!!)
               * Required Parameters: 
               	* name <UC4Regex> (ex: name="NOVA.*")
               	
               * Optional Parameters: 
               	* search_usage <Y|N> (ex: search_usage=Y)
            
			ex: * /api/awa/search/v1/Jobs?name="NOVA.*"&token=s9dpur80s8rtvharifrm531387
			ex: * /api/awa/search/v1/Jobs?name="NOVA.*"&search_usage=Y&token=s9dpur80s8rtvharifrm531387
                              
          * **Activities** (Activity Window Content)
               * Optional Filters:          
               	* status <Integer|list of Integers|range of Integers> (ex: status:1800 or status:1800,1801,1802 or status:1800-1900)
               	* name   <UC4Regex>  (ex: ABC.*)
               	* key1   <UC4Regex>  (ex: ABC.*)
               	* key2   <UC4Regex>  (ex: ABC.*)
               	* type   <List of Object Types separated by "|"> (ex: JOBS|JOBP|JOBF)
               	* queue  <UC4Regex>  (ex: ABC.*)
               	* parentrunid  <Integer>   (ex: 123456789001)
               	* toprunid  <Integer>   (ex: 123456789001)
               	* user  <UC4Regex>   (ex: BSP*)
               	* platform <List of Platforms separated by "|"> (ex: WIN|UNIX|CIT)
               	* activation <((YYYYMMDD|YYYYMMDDHHMM)-(YYYYMMDD|YYYYMMDDHHMM|NOW))|LAST<Integer>(YEARS|MONTHS|DAYS|HOURS|MINUTES|SECONDS)> (ex: 20160601-NOW or 201606012359-201606040000 or LAST45MINUTES or LAST4DAYS)
               	* exkey1    (ex: exkey1) (exclude rather than include pattern in key1)
               	* exkey2    (ex: exkey2) (exclude rather than include pattern in key2)
               	* exhost    (ex: exhost) (exclude rather than include pattern in host)
               	* exname    (ex: exname) (exclude rather than include pattern in name)
               	* exuser    (ex: exuser) (exclude rather than include pattern in user)
               	
          	ex: * /api/awa/search/v1/Activities?filters=[status:1900]&token=s9dpur80s8rtvharifrm531387
          
          * **All** (All Objects)
               * Required Parameters: 
               	* name <UC4Regex> (ex: name="NOVA.*")
               	
               * Optional Parameters: 
               	* search_usage <Y|N> (ex: search_usage=Y)  
               	* LOTS more.. see help through REST Call
               	
			ex: * /api/awa/search/v1/All?name="NOVA.*"&token=s9dpur80s8rtvharifrm531387
			ex: * /api/awa/search/v1/All?name="NOVA.*"&search_usage=Y&token=s9dpur80s8rtvharifrm531387   
					             	
          * **Statistics** (period Window Content)
               * Optional Filters: 
               	* name   <UC4Regex>  (ex: ABC.*)
               	* status <Integer>   (ex: 1900) 
               	* key1   <UC4Regex>  (ex: ABC.*)
               	* key2   <UC4Regex>  (ex: ABC.*)
               	* type   <List of Object Types separated by "|"> (ex: JOBS|JOBP|JOBF)
               	* alias  <UC4Regex>  (ex: ABC.*)
               	* client <Integer>   (ex: 200)
               	* queue  <UC4Regex>  (ex: ABC.*)
               	* runid  <Integer>   (ex: 123456789001)
               	* platform <List of Platforms separated by "|"> (ex: WIN|UNIX|CIT)
               	* activation <((YYYYMMDD|YYYYMMDDHHMM)-(YYYYMMDD|YYYYMMDDHHMM|NOW))|LAST<Integer>(YEARS|MONTHS|DAYS|HOURS|MINUTES|SECONDS)> (ex: 20160601-NOW or 201606012359-201606040000 or LAST45MINUTES or LAST4DAYS)
               	* start <((YYYYMMDD|YYYYMMDDHHMM)-(YYYYMMDD|YYYYMMDDHHMM|NOW))|LAST<Integer>(YEARS|MONTHS|DAYS|HOURS|MINUTES|SECONDS)> (ex: 20160601-NOW or 201606012359-201606040000 or LAST45MINUTES or LAST4DAYS)
               	* end <((YYYYMMDD|YYYYMMDDHHMM)-(YYYYMMDD|YYYYMMDDHHMM|NOW))|LAST<Integer>(YEARS|MONTHS|DAYS|HOURS|MINUTES|SECONDS)> (ex: 20160601-NOW or 201606012359-201606040000 or LAST45MINUTES or LAST4DAYS)
                             
          ex: * /api/awa/search/v1/Statistics?filters=[platform:WIN|UNIX,type:EVNT|JOBS,name:*,activation:LAST4DAYS]&token=s9dpur80s8rtvharifrm531387
          ex: * /api/awa/search/v1/Statistics?filters=[platform:WIN,type:JOBS|JOBP,activation:LAST30MINUTES]&token=s9dpur80s8rtvharifrm531387   
                 
     * returns: JSON

* **Deactivate (GET):**

     * Supported Objects:
     
          * **Activities**
               * Mandatory Parameters: 
               	* runid <Integer> (ex: runid=1234567891000)
					
               * Optional Parameters: 
		* force Y|N (ex: force=Y) (Force Deactivation, default is N)
                             
     * Examples:
     
          * /api/awa/deactivate/v1/Activities?runid=12345678&force=Y&token=s9dpur80s8rtvharifrm531387

     * returns: JSON

* **Quit (GET):**

     * Supported Objects:
     
          * **Activities**
               * Mandatory Parameters: 
               	* runid <Integer> (ex: runid=1234567891000)
               
     * Examples:
     
          * /api/awa/quit/v1/Activities?runid=12345678&token=s9dpur80s8rtvharifrm531387

     * returns:

          JSON
          
* **Rerun (GET):**

     * Supported Objects:
     
          * **Activities**
               * Mandatory Parameters: 
               	* runid <Integer> (ex: runid=1234567891000)
               
     * Examples:
     
          * /api/awa/rerun/v1/Activities?runid=12345678&token=s9dpur80s8rtvharifrm531387


     * returns: JSON

* **Unblock (GET):**

     * Supported Objects:
     
          * **Activities**
               * Mandatory Parameters: 
					* runid <Integer> (ex: runid=1234567891000)
					* type <JOBS|JOBP> (ex: type=JOBP)
					
               * Optional Parameters: 
					* lnr <Integer> (ex: lnr=0001) (useful when unblocking a task within a workflow that is blocked because of the external taskdependency)
              
          ex: * /api/awa/unblock/v1/Activities?runid=12345678&token=s9dpur80s8rtvharifrm531387


     * returns: JSON

* **Resume / go / stop / suspend (GET):**

     * Supported Objects:
     
          * **Client**
               * Mandatory Parameters: 
               	* None
               * Optional Parameters: 
               	* client <Integer> (ex: client=200) (=> this option can only be used when connected to Client 0. If not on Client 0, current Client is considered.)
               
     * Examples:
     
          * /api/awa/resume/v1/Client?client=200&token=s9dpur80s8rtvharifrm531387
          * /api/awa/stop/v1/Client?token=s9dpur80s8rtvharifrm531387


     * returns: JSON

* **display (GET):**

     * Supported Objects:
     
          * **Engine**
               * Mandatory Parameters: 
               	* None
               * Optional Parameters: 
               	* method < showdb|showclients|showhosts|showhostgroups|showusers > (ex: method=showclients) 
               
     * Examples:
     
          * /api/awa/display/v1/Engine?method=showdb&token=s9dpur80s8rtvharifrm531387
          * /api/awa/display/v1/Engine?method=showusers&token=s9dpur80s8rtvharifrm531387


     * returns: JSON
     
* **update (POST):**

     * Supported Objects:
     
          * **Jobs**
               * Mandatory Parameters: 
               	* JSON Body in POST Request
               * Optional Parameters: 
               	* commit < Y > (ex: commit=Y) => Commit update operations (by default: N. Only a simulation runs)
              
           
     * POST Body Examples:
     
		{
			"std_filters":
			{
				"name" : "NOVA.CDS.DB2*",
				"include_links" : true,
				"search_for_usage" : false,
				"text" : "*",
				"search_text" : false,
				"search_text_title" : false,
				"search_text_key" : false,
				"search_text_process" : false,
				"search_text_docu" : false,
				"date_search" : false,
				"date_search_type" : "created|modified|used",
				"date_from" : "YYYYMMDDhhmm",
				"date_to" : "YYYYMMDDhhmm"
			},
			"spec_filters":
			{
			    "active" : true,
			    "inactive" : true
			},
			"updates":
			{
			    "status" : "inactive"
			}
		}


     * returns: JSON    

* **delete (POST):**

     * Supported Objects:
     
          * **Jobs**
               * Mandatory Parameters: 
               	* JSON Body in POST Request
               * Optional Parameters: 
               	* commit < Y > (ex: commit=Y) => Commit update operations (by default: N. Only a simulation runs)
				-> See method=usage for example of JSON body
				              
          * **All**
               * Mandatory Parameters: 
               	* JSON Body in POST Request
               * Optional Parameters: 
               	* commit < Y > (ex: commit=Y) => Commit update operations (by default: N. Only a simulation runs)       
				-> See method=usage for example of JSON body

     * returns: JSON    
     
..More to come..
