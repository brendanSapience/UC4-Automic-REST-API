# UC4-Automic-REST-API

REST API Server for Automic's ONE Automation Platform

**Structure proposed:**

     http://<server name>:<port>/api/<product>/<api category>/<version>/<object type (if needed)>?method=<method>&parameters=<all required parameters>

* **with:**

     * <server name>: hostname of server hosting the REST API app
     * <port>: port number of the REST API app
     * <api>: api (generic placeholder in case something else needs to sit on the same server / port)
     * <product>: automic product line: ara | awa | aso | etc. (makes the api more extensible)
     * <api category>: type of methods. Ex: display / update / delete / find / move etc. 
     * <version>: version of the API + Product + API category (allows more granular releases)
     * <object type>: type of objects to be considered. Ex: JOBS / JOBP / JSCH etc. NOT mandatory (depends on the api category).
     * <method>: method to execute. Can be the same as <api category> for simple stuff (display), otherwise specifies an individual operation: method=u_title (update title), method=u_restore_previous (restore previous version of object), etc.
     * <parameters>: parameters required for the <method>. ex: parameters=['status'=true], parameters=['oldpattern','newpattern'], etc.
     

**Methods / api categories**

* **Authentication:**

    => http://localhost:8080/Automic-RESTful-Server/api/awa/auth/v1?login=BSP&pwd=Un1ver$e&connection=AEPROD&client=200

* * **returns:**
        {'status':'success','token':'s9dpur80s8rtvharifrm531387','expdate':'20161231235959'}

* **Search:**
* * **Supported Objects:
*  - Jobs
*  - Activities

http://localhost:8080/Automic-RESTful-Server/api/awa/display/v1/Jobs?filters=[name:"NOVA.*"]&token=s9dpur80s8rtvharifrm531387

returns:

{
"success":true
"count":2,
"data":
    [
    {"name":"INTERNAL.EC2.CHECK.HOST.REACHABLE","folder":"0200/INTEGRATIONS/EC2/EC2.TEMPLATES","title":"Check that a given host is reachable","type":"JOBS","open":""}
    ,
    {"name":"INTERNAL.EC2.CHECK.INSTANCE.STATUS","folder":"0200/INTEGRATIONS/EC2/EC2.TEMPLATES","title":"Internal - Check Instance Status","type":"JOBS","open":""}
    ]
}

More to come!

Here are the Raw Notes on REST API Design:

General Structure:

Mandatory & Optional General Parameters:

-All calls (except authentication) must contain a auth token parameter for secure use: => this will be a parameter in the URL

    =>  &token=dsfgsdfgfasdfa

-All calls (including authentication) can (optional) contain a parameter to specify the output format: => this should be specified in the Request Header !!
     (ideally we'd implement JSON and XML .. but let's start with JSON only ;))

Authentication call:

http://<server name>:<port>/api/awa/auth/v1?method=auth&conn=AEPPROD&login=bsp&dept=automic&client=200&pwd=Un1ver$e
     => conn parameter is a name. it should be mapped to a list of ports and hostname (similar to what is in the xml config file of ECC). Hostname & ports should be shielded from users.
    // Not for now: => lang parameter should be optional (Defaults to EN)
    // Not for now:  => validity is an optional parameter, it should be customizable, but should not exceed a certain threshold (60 minutes?)
     => should we allow client 0 auth? Prob, yes.
   
this should return a unique auth token:
     {‘code’:200,’message’:’ok’,’token’:324mhgjhf234kh234,’expires’:'20160601235900'}

     => Return an auth token if successful. Backend saves connection in pool and uniquely identifies it to the token.
     => the token should NOT contain any info: ex: conn name, user, dept, client (randomly generated and unique)

Getting Help:

     ideally there should be a way to retrieve, per <api category>, a list of available:
          - Objects
          - Methods
          - Parameters
     
     perhaps:
     /api/awa/help/v1? => show list of <api categories>
     /api/awa/admin/v1/show_objects? => show list of available objects
     /api/awa/admin/v1/AGENTS?method=show_methods => show list of methods

Commit Flag:

     should we add a simulation mode ? or a commit flag ? or neither ?

Examples:

     ADMIN:
/api/awa/admin/v1/AGENTS?method=u_delete&filters=[‘category’:’RA’]
/api/awa/admin/v1/AGENTS?method=u_setauth&filters=[‘category’:’RA’]&parameters=[‘*’,’Y’,’Y’,’Y’,N']
/api/awa/admin/v1/CHANGES?method=list

     JOBS:
/api/awa/display/v1/JOBS?method=getinfo&filters=[‘name’:’*.ABC.*’, ’type’:’JOBS’]&additional[‘LOGIN']&token=dsfgsdfgfasdfa
/api/awa/update/v1/JOBS?method=display_methods&token=dsfgsdfgfasdfa
/api/awa/update/v1/JOBS?method=u_active&parameters=['status'=true]&token=dsfgsdfgfasdfa
/api/awa/update/v1/JOBS?method=u_title&parameters=['oldpattern','newpattern']&token=dsfgsdfgfasdfa

     CONN:
          /api/awa/update/v1/CONN?method=db_type&parameters=[’MS_SQL']&token=dsfgsdfgfasdfa
/api/awa/update/v1/CONN?method=ra_updval&parameters=[’name’=‘val']&token=dsfgsdfgfasdfa
/api/awa/update/v1/CONN?method=u_title&parameters=['oldpattern','newpattern']&token=dsfgsdfgfasdfa

     ALL:
          /api/awa/general/v1?method=create&parameters=[’type’:’JOBS’,’name’:’JOBS.EX.1']&token=dsfgsdfgfasdfa
          /api/awa/general/v1?method=delete&parameters=[’name’:’*JOBS.EX.1*']&token=dsfgsdfgfasdfa
          /api/awa/general/v1?method=move&parameters=[’name’:’*JOBS.EX.1*']&token=dsfgsdfgfasdfa

     RUNS:
          /api/awa/display/v1/RUNS?method=display&filters=[’name’:’*JOBS.EX.1*']&token=dsfgsdfgfasdfa
         /api/awa/runs/v1?method=cancel&parameters=[’name’:’*JOBS.EX.1*']&token=dsfgsdfgfasdfa

