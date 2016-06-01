package automic.restful.server

class Jobs {
  
    /* Default (injected) attributes of GORM */
    Long    id
    Long    version
 
    /* Automatic timestamping of GORM */
    Date    dateCreated
    Date    lastUpdated
     
     
    String jobName
    String jobLogin
    String jobHost
 
    static constraints = {
        jobName blank:false, nullable:false
    }
}
