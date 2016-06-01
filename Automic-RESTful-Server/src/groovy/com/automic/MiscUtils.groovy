package com.automic

class MiscUtils {

	static String toCamelCase( String text, boolean capitalized = false ) {
		text = text.replaceAll( "(_)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() } )
		return capitalized ? capitalize(text) : text
		}
	
}
