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
     - Ideally the token will be moved to the request header (to be done..)
     - Tokens are non-persistent (they are not kept if the Server shuts down)
     - Tokens expire after a certain configurable period
     - Ideally the server should be able to return XML responses as well as JSON (JSON only for now) depending on the header content of the request
     - Tokens are uniquely & randomly generated and do not encode any information
     - There needs to be calls dedicated to retrieving available methods / objects & parameters as this REST API will rapidly get more and more complex
     - There needs to be a "commit" mechanism to prevent accidental changes in objects
     
     
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
               * Filters: name (Format: _filters=[name="NOVA.*"]_)
          * **Activities**
               * Filters: status (Format: _filters=[status=1800]_)
          * **All**
               * Filters: name (Format: _filters=[name="NOVA.*"]_) 
               
     * Examples:
     
          * /api/awa/search/v1/Jobs?filters=[name:"NOVA."]&token=s9dpur80s8rtvharifrm531387
          * /api/awa/search/v1/All?filters=[name:"NOVA."]&token=s9dpur80s8rtvharifrm531387
          * /api/awa/search/v1/Activities?filters=[status:1900]&token=s9dpur80s8rtvharifrm531387

     * returns:

          JSON

* **Rerun:**

     * Supported Objects:
          * **Activities**
               * Mandatory Parameters: runid (Format: _runid=12344556_)
               
     * Examples:
     
          * /api/awa/rerun/v1/Activities?runid=12345678&token=s9dpur80s8rtvharifrm531387


     * returns:

          JSON

* **Unblock:**

     * Supported Objects:
          * **Activities**
               * Mandatory Parameters: runid (Format: _runid=12344556_)
               
     * Examples:
     
          * /api/awa/unblock/v1/Activities?runid=12345678&token=s9dpur80s8rtvharifrm531387


     * returns:

          JSON


!! The current scope is very limited, but the basics are in place and it should be extensible fairly easily & quickly.
