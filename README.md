# UC4-Automic-REST-API

REST API Server for Automic's ONE Automation Platform

**Structure proposed:**

     http://<server name>:<port>/api/<product>/<api category>/<version>/<object type (if needed)>?method=<method>&parameters=<all required parameters>

* **with:**

     - **server name**: hostname of server hosting the REST API app
     - **port**: port number of the REST API app
     - **api**: api (generic placeholder in case something else needs to sit on the same server / port)
     - **product**: automic product line: ara | awa | aso | etc. (makes the api more extensible)
     - **api category**: type of methods. Ex: display / update / delete / find / move etc. 
     - **version**: version of the API + Product + API category (allows more granular releases)
     - **object type**: type of objects to be considered. Ex: JOBS / JOBP / JSCH etc. NOT mandatory (depends on the api category).
     - **method**: method to execute. Can be the same as <api category> for simple stuff (display), otherwise specifies an individual operation: method=u_title (update title), method=u_restore_previous (restore previous version of object), etc.
     - **parameters**: parameters required for the <method>. ex: parameters=['status'=true], parameters=['oldpattern','newpattern'], etc.
     
**Important Additional Design Aspects:**

     - An authentication token must be passed in every call as a URL parameter (except for the authentication call itself).
     - The Auth Token can be specified in the request http header (as 'token' parameter), in which case the 'token' url parameter becomes unecessary
     - Tokens are non-persistent (they are not kept if the Server shuts down)
     - Tokens expire after a certain configurable period
     - Ideally the server should be able to return XML responses as well as JSON (JSON only for now) depending on the header content of the request
     - Tokens are uniquely & randomly generated and do not encode any information
     - There needs to be calls dedicated to retrieving available methods / objects & parameters as this REST API will rapidly get more and more complex
     - There needs to be a "commit" mechanism to prevent accidental changes in objects
     - Filters are designed in a specific way and provided as a url parameter called filters:
     		filters=[FilterName:FilterValue]
     		=> the FilterNames available obviously depend on the Object & Action you are working with
     		=> the FilterValues also depend on the filterName. Some takes only Integers, some UC4Regex, some actual Regex, some have more specific formats
     
     
**Available Methods & API Categories:**

* **Authentication:**
     
    Ex: /api/awa/**auth**/v1?**login**=BSP&**pwd**=Un1ver$e&**connection**=AEPROD&**client**=200

     * **Mandatory Parameters:**
     
          * _login_ (AE Connection login)
          * _pwd_ (AE Connection password)
          * _client_ (AE Client number)
          * _connection_ (AE Connection Name as specified in the REST API configuration file called connection_config.json)
          
     * Returns:
     
        {'status':'success','token':'s9dpur80s8rtvharifrm531387','expdate':'20161231235959'}

* **Search:**

     * Supported Objects:
     
          * **Jobs** 
               * Required Parameters: 
               	* name <UC4Regex> (ex: name="NOVA.*")
               	
               * Optional Parameters: 
               	* search_usage <Y|N> (ex: search_usage=Y)
            
				ex: * /api/awa/search/v1/Jobs?name="NOVA.*"&token=s9dpur80s8rtvharifrm531387
				ex: * /api/awa/search/v1/Jobs?name="NOVA.*"&search_usage=Y&token=s9dpur80s8rtvharifrm531387
                              
          * **Activities** (Activity Window Content)
               * Optional Filters:          
               	* status <Integer|list of Integers|range of Integers> (ex: status:1800 or status:1800,1801,1802 or status:1800-1900)
               
          	ex: * /api/awa/search/v1/Activities?filters=[status:1900]&token=s9dpur80s8rtvharifrm531387
          
          * **All** (All Objects)
               * Required Parameters: 
               	* name <UC4Regex> (ex: name="NOVA.*")
               	
               * Optional Parameters: 
               	* search_usage <Y|N> (ex: search_usage=Y)  
               	
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
                 
     * returns:

          JSON

* **Rerun:**

     * Supported Objects:
     
          * **Activities**
               * Mandatory Parameters: 
               	* runid <Integer> (ex: runid=1234567891000)
               
     * Examples:
     
          * /api/awa/rerun/v1/Activities?runid=12345678&token=s9dpur80s8rtvharifrm531387


     * returns:

          JSON

* **Unblock:**

     * Supported Objects:
     
          * **Activities**
               * Mandatory Parameters: 
					* runid <Integer> (ex: runid=1234567891000)
					* type <JOBS|JOBP> (ex: type=JOBP)
					
               * Optional Parameters: 
					* lnr <Integer> (ex: lnr=0001) (useful when unblocking a task within a workflow that is blocked because of the external taskdependency)
              
          ex: * /api/awa/unblock/v1/Activities?runid=12345678&token=s9dpur80s8rtvharifrm531387


     * returns:

          JSON


!! The current scope is very limited, but the basics are in place and it should be extensible fairly easily & quickly.
