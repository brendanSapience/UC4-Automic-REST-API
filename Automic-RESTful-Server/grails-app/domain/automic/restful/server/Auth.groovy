package automic.restful.server

import java.util.Date;

class Auth {

	/* Default (injected) attributes of GORM */
	Long    id
	Long    version
 
	/* Automatic timestamping of GORM */
	Date    dateCreated
	Date    lastUpdated
	 
	 
	String login
	String password
	String host
	
    static constraints = {
    }
}
