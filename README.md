# UC4-Automic-REST-API

REST API Server for Automic's ONE Automation Platform

**Structure proposed:**

     http://<server name>:<port>/api/<product>/<api category>/<version>/<object type>?method=<method>&<all required URL parameters>

* **with:**

     - **server name**: hostname of server hosting the REST API app
     - **port**: port number of the REST API app
     - **api**: api (generic placeholder in case something else needs to sit on the same server / port)
     - **product**: automic product line: ara | awa | aso | etc. (makes the api more extensible)
     - **api category**: type of methods. Ex: display / update / delete / find / move etc. 
     - **version**: version of the API + Product + API category (allows more granular releases)
     - **object type**: type of objects to be considered. Ex: JOBS / JOBP / JSCH etc. NOT mandatory (depends on the api category).
     - **method**: method to execute. Only used when the 'api category' isnt enough to define the action that is being taken.
     - **URL parameters**: parameters required for the <method> & <api category> combination. ex: filters=[status:1800,type:JOBP] or search_usage=Y, etc.
     
**Important Additional Design Aspects:**

     - The authentication token can be either passed in every call as a URL parameter ("token=") or passed as a request header parameter (as 'token').
     - IF the Auth token is specified in the request header 'token' url parameter becomes unnecessary.
     - Tokens are non-persistent (they are not kept if the Server shuts down as they are tied to individual connection objects).
     - Tokens expire after a certain configurable period (is configured in the ConnectionConfig.json file).
     - Ideally the server should be able to return XML responses as well as JSON (JSON only for now) depending on the header content of the request.
     - Tokens are uniquely & randomly generated and do not encode any specific information.
     - There are "helpers" url parameters that can be used to retrieve more info on certain calls.
     - There needs to be a "commit" mechanism to prevent accidental changes in objects (not implemented yet).
     - Every api category is versionned: this is to facilitate backwards compatibility in the future
     - Update mechanisms will most likely leverage POST calls (with a JSON structure provided in the body) 
     - Filters are designed in a specific way and provided as a url parameter called filters:
     		filters=[FilterName:FilterValue]
     		=> the FilterNames available obviously depend on the Object & Action you are working with
     		=> the FilterValues also depend on the filterName. Some takes only Integers, some UC4Regex, some actual Regex, some have more specific formats
     		=> FilterNames & FilterValues can be easily chained (as long as they are separated by commas). Ex: filters=[name=NOVA*,status=1800,type=JOBF|JOBS]
     
   **How to Get Help (and get started):**
   
* there is a **special < api category >** called **"help"** you can always use in order to **retrieve the list of available < api categories >** for a given object type (and the list of versions available):
     	
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
     	
* There is a special **< method >** called **"usage"** you can always use in order to retrieve the **list of parameters / filters required** for a given **Object Type**, **version** & **api category** combination:
     	
  ex: http://localhost:8080/Automic-RESTful-Server/api/awa/search/v1/Activities?**method=usage**
     	
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

	=> This means that the **search < api category >** in combination with object **Activities** for **version 1** has **no required parameters** and **3 optional filters** you can use.
		
       
**Available Methods & API Categories:**

* **Authentication (login):**
     
    Ex: /api/awa/**login**/v1/**Auth**?**login**=BSP&**pwd**=Un1ver$e&**connection**=AEPROD&**client**=200

     * **Mandatory Parameters:**
     
          * _login_ (AE Connection login)
          * _pwd_ (AE Connection password)
          * _client_ (AE Client number)
          * _connection_ (AE Connection Name as specified in the REST API configuration file called connection_config.json)
          
     * Returns:
     
        {'status':'success','token':'s9dpur80s8rtvharifrm531387','expdate':'20161231235959'}
        
* **Authentication (logout):**
     
    Ex: /api/awa/**logout**/v1/**Auth**?**token**=123fjh324gf234k234

     * **Mandatory Parameters:**
     
          * _token_ (your auth token)
          

* **Search:**

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

* **Deactivate:**

     * Supported Objects:
     
          * **Activities**
               * Mandatory Parameters: 
               	* runid <Integer> (ex: runid=1234567891000)
					
               * Optional Parameters: 
		* force Y|N (ex: force=Y) (Force Deactivation, default is N)
                             
     * Examples:
     
          * /api/awa/deactivate/v1/Activities?runid=12345678&force=Y&token=s9dpur80s8rtvharifrm531387

     * returns: JSON

* **Quit:**

     * Supported Objects:
     
          * **Activities**
               * Mandatory Parameters: 
               	* runid <Integer> (ex: runid=1234567891000)
               
     * Examples:
     
          * /api/awa/quit/v1/Activities?runid=12345678&token=s9dpur80s8rtvharifrm531387

     * returns:

          JSON
          
* **Rerun:**

     * Supported Objects:
     
          * **Activities**
               * Mandatory Parameters: 
               	* runid <Integer> (ex: runid=1234567891000)
               
     * Examples:
     
          * /api/awa/rerun/v1/Activities?runid=12345678&token=s9dpur80s8rtvharifrm531387


     * returns: JSON

* **Unblock:**

     * Supported Objects:
     
          * **Activities**
               * Mandatory Parameters: 
					* runid <Integer> (ex: runid=1234567891000)
					* type <JOBS|JOBP> (ex: type=JOBP)
					
               * Optional Parameters: 
					* lnr <Integer> (ex: lnr=0001) (useful when unblocking a task within a workflow that is blocked because of the external taskdependency)
              
          ex: * /api/awa/unblock/v1/Activities?runid=12345678&token=s9dpur80s8rtvharifrm531387


     * returns: JSON

* **Resume / go / stop / suspend:**

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

* **display:**

     * Supported Objects:
     
          * **Engine**
               * Mandatory Parameters: 
               	* None
               * Optional Parameters: 
               	* method <showdb|showclients|showhosts|showhostgroups|showusers> (ex: method=showclients) 
               
     * Examples:
     
          * /api/awa/display/v1/Engine?method=showdb&token=s9dpur80s8rtvharifrm531387
          * /api/awa/display/v1/Engine?method=showusers&token=s9dpur80s8rtvharifrm531387


     * returns: JSON
     
!! The current scope is very limited, but the basics are in place and it should be extensible fairly easily & quickly.
