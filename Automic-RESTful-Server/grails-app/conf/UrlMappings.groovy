class UrlMappings {

	
	
    static mappings = {

        "/"(view:"/index")
        "500"(view:'/error')

///api/awa/display/v1/JOBS?method=getinfo&filters=[�name�:�*.ABC.*�, �type�:�JOBS�]&additional[�LOGIN']&token=dsfgsdfgfasdfa

       // "/api/awa/display/v1/$type?"(controller: 'display', action: 'show', method: 'GET')
      //  "/api/$product/display/$version/$type?(${method})?(${parameters})?(${filters})?(${additional})?(${token})?"(controller: 'display', action: 'show', method: 'GET')
 
		// http://localhost:8080/Automic-RESTful-Server/api/awa/auth/v1?login=BSP&pwd=Un1ver$e&connection=AEPROD&client=200
        // "/api/$product/auth/$version"(controller:'Auth', action: 'authenticate', method: 'GET')
		// "/api/$product/deauth/$version"(controller:'Auth', action: 'logout', method: 'GET')
		// ex: http://localhost:8080/Automic-RESTful-Server/api/awa/display/v1/Jobs?filters=[name:"NOVA.*"]&token=6uu78lnmoh3em4tfa300pdlcoo
		
	//	"/api/$product/router/$version/$object"(controller:{"${params.object}"}, action: 'router', method: 'GET')
		
	//	"/api/$product/$operation/$version/$object"(controller:{"${params.object}"}, action: {"${params.operation}${params.version}"}, method: 'GET')
		
		"/api/$product/$operation/$version/$object"(controller:{"${params.object}"}, action: 'router', method: 'GET')
		"/api/$product/help/$version/$object"(controller:{"${params.object}"}, action: 'help', method: 'GET')
   }
}
