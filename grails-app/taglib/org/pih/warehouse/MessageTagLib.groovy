package org.pih.warehouse

class MessageTagLib {
   
	static namespace = "warehouse"
	
	Locale defaultLocale = new Locale(grailsApplication.config.locale.defaultLocale)
	
	def message = { attrs, body ->
		Locale l = attrs.locale  ?: session?.user?.locale ?: defaultLocale; 
		out << g.message(code:attrs.code, args: attrs.args, encodeAs: attrs.encodeAs, error: attrs.error, message: attrs.message, locale: l)
	}
}
