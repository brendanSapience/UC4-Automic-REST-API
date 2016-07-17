package com.automic.utils

import com.uc4.api.DateTime
import com.uc4.api.TaskFilter;

/**
 * 
 * @author bsp
 * @purpose various utilitarian methods
 */

class MiscUtils {

	static String[] getPatternsFromStringArg(String str, int numberOfParams){
		String SEPARATOR = ","
		if(numberOfParams == 1){
			return [str.split(SEPARATOR)[0]]
		}else if(numberOfParams == 2){
			if(str.contains(SEPARATOR)){
				return [str.split(SEPARATOR)[0],str.split(SEPARATOR)[1]]
			}else{return ["",""]}
		}else if(numberOfParams == 3){
			if(str.contains(SEPARATOR) && str.split(SEPARATOR).length==3){
				return [str.split(SEPARATOR)[0],str.split(SEPARATOR)[1],,str.split(SEPARATOR)[2]]
			}else{return ["",""]}
		}
		
	}
	
	static String toCamelCase( String text, boolean capitalized = false ) {
		text = text.replaceAll( "(_)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() } )
		return capitalized ? capitalize(text) : text
		}
	
	// checks that URL params contains all required parameters contained in HashMap
	static boolean checkParams(HashMap<String,String[]> Hash, params){
		
		boolean isError = false;
		Hash.each{ k, v -> if(k.contains("required_parameters"))
			{
				if(v.size() > 0){
					v.each { s -> String ParamName = s.split(/ /)[0];
						//println "Param: " + ParamName +"=" +params."${ParamName}";
						//println "Param: " + ParamName +" : " +params.name;
						if(params."${ParamName}" == null || params."${ParamName}" == "" ){
							isError = true;
						}
						
					}
				}
			}
			
		}
		return !isError;
		
	}
	
	
	def public static DateTime HandleDateText(String Date){
		String BeginNumberExtracted = Date.replace(':', '').replace("/","").replace(" ","").findAll( /\d+/ )[0] //makes sure we have no other character
		
		// Adjusting the length to 14 char
		if(BeginNumberExtracted.length() == 8){ //YYYYMMDD
			BeginNumberExtracted = BeginNumberExtracted + '000000'
		}else if(BeginNumberExtracted.length() == 12){ //YYYYMMDDhhmm
			BeginNumberExtracted = BeginNumberExtracted + '00'
		}else if(BeginNumberExtracted.length() == 4){ //hhmm
			BeginNumberExtracted = DateTime.now().getYear().toString()+ DateTime.now().getMonth().toString()+ DateTime.now().getDay().toString()+ BeginNumberExtracted + '00'
		}
		// if the length is still incorrect.. we just give an arbitrary window. Why? Because..
		if(BeginNumberExtracted.length() == 14){
			return new DateTime(BeginNumberExtracted.substring(0, 4).toInteger(),BeginNumberExtracted.substring(4, 6).toInteger(),BeginNumberExtracted.substring(6, 8).toInteger(),
				BeginNumberExtracted.substring(8, 10).toInteger(),BeginNumberExtracted.substring(10, 12).toInteger(),BeginNumberExtracted.substring(12, 14).toInteger())

		}
		
		
	}
	/**
	 * @purpose Handle Time / Date Filters. Input is treated as multiple possible formats:
	 * 			=> YYYYMMDDhhmm-YYYYMMDDhhmm
	 * 			=> YYYYMMDD-YYYYMMDDhhmm
	 * 			=> YYYYMMDDhhmm-YYYYMMDD
	 * 			=> YYYYMMDD-YYYYMMDD
	 * 			=> YYYYMMDDHHMM-NOW
	 * 			=> YYYYMMDD-NOW
	 * 			=> LAST[n][pattern] (with n any number and pattern can be YEARS or MONTHS or WEEKS or DAYS or HOURS or MINUTES or SECONDS. Ex: LAST5WEEKS, or LAST45MINUTES)
	 * @return DateTime[]
	 * @version NA
	 */
	def private static DateTime[] HandleDateFilter(String RawDate){
		if(RawDate.contains('-')){
			String RawBeginDate = RawDate.split("-")[0];
			String RawEndDate = RawDate.split("-")[1];
			DateTime BeginDate = null;
			DateTime EndDate = null;
			
			String BeginNumberExtracted = RawBeginDate.findAll( /\d+/ )[0] //makes sure we have no other character
			
			// Adjusting the length to 14 char
			if(BeginNumberExtracted.length() == 8){
				BeginNumberExtracted = BeginNumberExtracted + '000000'
			}else if(BeginNumberExtracted.length() == 12){
				BeginNumberExtracted = BeginNumberExtracted + '00'
			}
			// if the length is still incorrect.. we just give an arbitrary window. Why? Because..
			if(BeginNumberExtracted.length() != 14){
				BeginDate = DateTime.now().addMinutes(-4*60);
			}else{
				BeginDate = new DateTime(BeginNumberExtracted.substring(0, 4).toInteger(),BeginNumberExtracted.substring(4, 6).toInteger(),BeginNumberExtracted.substring(6, 8).toInteger(),
					BeginNumberExtracted.substring(8, 10).toInteger(),BeginNumberExtracted.substring(10, 12).toInteger(),BeginNumberExtracted.substring(12, 14).toInteger())
			
			
			}
			
			if(RawEndDate.toUpperCase() =~/NOW/){
				EndDate = DateTime.now();
			} else{
				String EndNumberExtracted = RawEndDate.findAll( /\d+/ )[0] //makes sure we have no other character
				
				// Adjusting the length to 14 char
				if(EndNumberExtracted.length() == 8){
					EndNumberExtracted = EndNumberExtracted + '235900'
				}else if(EndNumberExtracted.length() == 12){
					EndNumberExtracted = EndNumberExtracted + '00'
				}
				// if the length is still incorrect.. we just give an arbitrary window. Why? Because..
				if(EndNumberExtracted.length() != 14){
					EndDate = DateTime.now();
				}else{
					EndDate = new DateTime(EndNumberExtracted.substring(0, 4).toInteger(),EndNumberExtracted.substring(4, 6).toInteger(),EndNumberExtracted.substring(6, 8).toInteger(),
						EndNumberExtracted.substring(8, 10).toInteger(),EndNumberExtracted.substring(10, 12).toInteger(),EndNumberExtracted.substring(12, 14).toInteger())
				}
			}
			
			return [BeginDate,EndDate]
			
			
		}else{ //LASTNHOURS / LASTNMINUTES / LASTNDAYS |  type
			
		if(RawDate.toUpperCase() =~ /LAST[0-9]+(YEARS|YEAR|YR|Y|MONTHS|MONTH|MTH|DAYS|DAY|D|HOURS|HR|HOUR|H|MIN|MINUTE|MINUTES|SECONDS|SECOND|SEC|S|)/){
			
			String NumberOfUnits = RawDate.findAll( /\d+/ )[0]
			
			int Number = NumberOfUnits.toInteger()
			String Type = RawDate.split(NumberOfUnits)[1]
			
			DateTime NOW = DateTime.now().addDays(1)
			DateTime BEGINNING = DateTime.now()
			

			
			if(Type.toUpperCase() =~/YEARS|YEAR|YR/){
				BEGINNING.addYears(-Number)
			}

			if(Type.toUpperCase() =~/MONTHS|MONTH|MTH/){
				BEGINNING.addMonth(-Number)
			}
			
			if(Type.toUpperCase() =~/WEEKS|WEEK|WK/){
				BEGINNING.addDays(-7*Number)
			}
			
			if(Type.toUpperCase() =~/DAY|DAYS/){
				BEGINNING.addDays(-Number)
			}
			if(Type.toUpperCase() =~/HOURS|HOUR|HR/){
				BEGINNING.addMinutes(-60*Number)
			}
			if(Type.toUpperCase() =~/MIN|MINUTE|MINUTES/){
				BEGINNING.addMinutes(-Number)
			}
			if(Type.toUpperCase() =~/SECONDS|SECOND|SEC/){
				BEGINNING.addSeconds(-Number)
			}

//			req.setTimestampFrom(BEGINNING);
//			req.setTimestampTo(NOW);
		//println "Begin: " + BEGINNING
		//	println "to: " + NOW
			return [BEGINNING,NOW]
		}

	}
	}
	
}
