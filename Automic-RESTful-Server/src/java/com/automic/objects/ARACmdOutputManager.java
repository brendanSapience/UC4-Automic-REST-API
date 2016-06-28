package com.automic.objects;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ARACmdOutputManager {

	static PrintStream OLDPS;
	
	public void ARACmdOutputManager(){
	}
	
	public static ByteArrayOutputStream OverrideStdOutputStream(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);OLDPS = System.out;System.setOut(ps);
		return baos;
	}
	
	public static void RevertStdOutputStream(){
		 System.out.flush();
		 System.setOut(OLDPS);
	}
	
	public static String extractMsg(String response){
		String Msg = "";
		 if(response.contains("ERROR:")){
			 Msg = response.split("ERROR:")[1].replace("\r\n", " ");
			 //System.out.println("Message: " + ErrorMsg);
		 }else if(response.contains("INFO:")){
			 String[] InfoFields = response.split("INFO:");
			 Msg = "";
			 for(int i=0;i<InfoFields.length;i++){
				 if(i>=1){
					 Msg = Msg +" "+ InfoFields[i].replace("\r\n", " ") ;
				 }
				
			 }
			 
			// Msg = response.split("INFO:")[1].replace("\r\n", " ");
		 }
		return Msg;	
		

	}
	

	
    
	
}
