/*
 * org.nrg.xnat.helpers.uri.UriParserUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.uri;

import org.restlet.util.Template;
import org.restlet.util.Variable;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class UriParserUtils {
	public static final String _REMAINDER = "_REMAINDER";
	
	public static URIManager.DataURIA parseURI(String s) throws MalformedURLException{
		if(s.startsWith("/data")){
			s=s.substring(5);
		}else if(s.startsWith("/REST")){
			s=s.substring(5);
		}
		
		if(s.startsWith("/prearchive")){
			if(s.equals("/prearchive")){
				final Map<String,Object> t=Collections.emptyMap();
				return new URIManager.PrearchiveURI(t,s);
			}
			
			for(final URIManager.TemplateInfo template: URIManager.getTemplates(URIManager.TEMPLATE_TYPE.PREARC)){
				Map<String,Object> map=new UriParser(template.key,template.MODE).readUri(s);
				if(map.size()>0){
					return template.wrap(map,s);
				}
			}
		}else if(s.startsWith("/archive")){
			if(s.equals("/archive")){
				final Map<String,Object> t=Collections.emptyMap();
				return new URIManager.ArchiveURI(t,s);
			}
			
			for(final URIManager.TemplateInfo template: URIManager.getTemplates(URIManager.TEMPLATE_TYPE.ARC)){
				Map<String,Object> map=new UriParser(template.key,template.MODE).readUri(s);
				if(map.size()>0){
					return template.wrap(map,s);
				}
			}
			
		}else if(s.startsWith("/user")){
			if(s.equals("/user")){
				final Map<String,Object> t=Collections.emptyMap();
				return new URIManager.UserCacheURI(t,s);
			}
			
			for(final URIManager.TemplateInfo template: URIManager.getTemplates(URIManager.TEMPLATE_TYPE.CACHE)){
				Map<String,Object> map=new UriParser(template.key,template.MODE).readUri(s);
				if(map.size()>0){
					return template.wrap(map,s);
				}
			}
			
		}
		
			
		throw new MalformedURLException();
	}

	/**
	 * A base parser that reads a uri using the given template.
	 * @author aditya
	 *
	 */
	public static class UriParser implements UriParserI<Map <String, Object>> {
		String template;
		int mode=Template.MODE_STARTS_WITH;
		UriParser (String template) {
			this.template = template;
		}
		public UriParser (String template, int mode) {
			this.template = template;
			this.mode=mode;
		}
		/**
		 * Parse the uri with the given template. No errors are thrown
		 * for fields, instead they are set to null. Users of this object beware.
		 */
		public Map<String,Object> readUri (String uri) {
			final Template t = new Template(template, mode, Variable.TYPE_URI_SEGMENT, "", true, false);
			final Map<String,Object> so = new HashMap<String,Object>();
			final int matched=t.parse(uri,so);
			if(matched>-1 && matched<uri.length())
			{
				so.put(_REMAINDER, uri.substring(matched));
			}
			return so;
		}
	}
	
	
}
