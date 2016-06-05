package com.automic.utils

class MiscUtils {

	static String toCamelCase( String text, boolean capitalized = false ) {
		text = text.replaceAll( "(_)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() } )
		return capitalized ? capitalize(text) : text
		}
	
	static boolean checkParams(HashMap<String,String[]> Hash, params){
		
		boolean isError = false;
		Hash.each{ k, v -> if(k.contains("required"))
			{
				//println "${v}"
				
				if(v.size() > 0){
					v.each { s -> String ParamName = s.split(/ /)[0];
						println "Param: " + ParamName +"=" +params."${ParamName}";
						//println "Param: " + ParamName +" : " +params.name;
						if(params."${ParamName}" == null || params."${ParamName}" == "" ){
							isError = true;
						}
						
					}
				}
				//return !isError;
				//AllValues.each{ s -> println "${s}"}
			}
			
		}
		return !isError;
		
	}
	
}
