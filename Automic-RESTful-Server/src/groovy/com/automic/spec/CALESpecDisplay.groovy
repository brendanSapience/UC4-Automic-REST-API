package com.automic.spec

import com.uc4.api.DateTime
import com.uc4.api.InvalidUC4NameException
import com.uc4.api.UC4HostName
import com.uc4.api.UC4ObjectName
import com.uc4.api.VersionControlListItem
import com.uc4.api.objects.CalendarKeyword
import com.uc4.api.objects.CustomAttribute
import com.uc4.api.objects.GroupCalendarKeyword
import com.uc4.api.objects.Job;
import com.uc4.api.objects.MonthlyCalendarKeyword
import com.uc4.api.objects.RollCalendarKeyword
import com.uc4.api.objects.StaticCalendarKeyword
import com.uc4.api.objects.UC4Object
import com.uc4.api.objects.WeeklyCalendarKeyword
import com.uc4.api.objects.YearlyCalendarKeyword
import com.uc4.communication.Connection;
import com.uc4.communication.requests.VersionControlList
import com.automic.objects.CommonAERequests;
import com.automic.objects.AEFolderRequests;
import com.automic.utils.*
import groovy.json.JsonBuilder

class CALESpecDisplay {
	public static def ShowObject(Connection conn, com.uc4.api.objects.Calendar cal){
		VersionControlList vcl = new VersionControlList(CommonAERequests.getUC4ObjectNameFromString(cal.getName(),false));
		CommonAERequests.sendSyncRequest(conn,vcl,false);
		Iterator<VersionControlListItem> iterator = vcl.iterator()

		Iterator<CalendarKeyword> calIt = cal.keywordIterator();
		
		def keywordData = 
				calIt.collect {[
				name:it.getName().name,
				static:it.static,
				weekly:it.weekly,
				monthly:it.monthly,
				yearly:it.yearly,
				roll:it.roll,
				group:it.group,
				data:getCalendarKeywordAsJSON(it)
				]}
			
		
		def versionsData = 
			 iterator.collect {[
				 name:it.getSavedName().getName(),
				 version: it.getVersionNumber()
				 ]}
		  
		
		def data = [
			status: "success",
			count: 1,
			data: [
				 name: cal.getName(),
				 type:cal.getType(),
				 keywords:keywordData,
				 versions: versionsData,
				 ]
		  ]

		def json = new JsonBuilder(data)
		return json;	
	}	
	
	private static def getCalendarKeywordAsJSON(CalendarKeyword keyword){
		if(keyword.weekly){
			WeeklyCalendarKeyword key = (WeeklyCalendarKeyword) keyword;
			
			return [
			monday: key.monday,
			tuesday: key.tuesday,
			wednesday: key.wednesday,
			thursday: key.thursday,
			friday: key.friday,
			saturday: key.saturday,
			sunday: key.sunday,
			everynweek: key.nWeek,
			endweek: key.getEndWeek(),
			startweek: key.getStartWeek(),
			]
		}
		if(keyword.monthly){
			MonthlyCalendarKeyword key = (MonthlyCalendarKeyword) keyword;
			return [
				dates: key.dates,
				definedinterval: key.definedInterval,
				everynmonth: key.nMonth,
				startmonth: key.startMonth,
				endmonth: key.endMonth,
				interval: key.isIntervalBeginOfMonth(),
				intervalmonthbegin: key.intervalBeginOfMonth,
				intervalendday: key.intervalEndDay,
				intervaleveryday: key.intervalEveryDay,
				intervalstartday: key.intervalStartDay,
				]
		}
		if(keyword.yearly){
			YearlyCalendarKeyword key = (YearlyCalendarKeyword) keyword;
			return [
				//dates: key.dates,
				definedinterval: key.definedInterval,
				everynyear: key.nYear,
				startyear: key.startYear,
				interval: key.isIntervalBeginOfYear(),
				intervalyearbegin: key.intervalBeginOfYear,
				intervalenddate: key.intervalEndDate,
				intervaleveryday: key.intervalEveryDay,
				intervalstartdate: key.intervalStartDate,
				]
		}
		if(keyword.static){
			StaticCalendarKeyword key = (StaticCalendarKeyword) keyword;
			ArrayList<DateTime> it = key.iterator().toList();
			return [
				dates: it.collect {
					it.toString()
				}
				]
		}
		if(keyword.group){
			GroupCalendarKeyword key = (GroupCalendarKeyword) keyword;
			return [
				all:key.allIterator().toList(),
				none:key.noneIterator(),
				one:key.oneIterator(),
				
			]
		}
		if(keyword.roll){
			RollCalendarKeyword key = (RollCalendarKeyword) keyword;
			return [
				adjustdays:key.adjustDays,
				adjustcalendar:key.adjustmentCalendar,
				adjustsign:key.adjustSign,
				operator:key.operator,
				sourcecal:key.sourceCalendar,
				colissions:key.collisionIterator().toList(),
			]
		}
		
	}
}
