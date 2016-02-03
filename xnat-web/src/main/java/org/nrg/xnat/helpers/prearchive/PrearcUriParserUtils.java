/*
 * org.nrg.xnat.helpers.prearchive.PrearcUriParserUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.prearchive;

import org.apache.commons.lang.StringUtils;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.util.Template;
import org.restlet.util.Variable;

import java.util.*;

public final class PrearcUriParserUtils {
		
	/**
	 * A uri parser that reads prearchive uri that requests an action on multiple projects.
	 * @author aditya
	 *
	 */
	static class ProjectsParser implements UriParserI<List<String>> {
		final PrearcUriParserUtils.UriParser i;
		public ProjectsParser(PrearcUriParserUtils.UriParser i) {
			this.i = i;
		}
		
		/**
		 * Read multiple projects and throw an error if the session label or timestamp are specified.
		 */
		public List<String> readUri (String uri) throws java.util.MissingFormatArgumentException {
			Map<String,Object> so = i.readUri(uri);
			if (null == so.get("PROJECT_ID")) {
				throw new java.util.MissingFormatArgumentException("No projects specified in uri " + uri);
			}
			Reference ref = new Reference(uri);
			List<String> segs = ref.getSegments();
			if (segs.size() > 3) {
				throw new java.util.MissingFormatArgumentException("More in the uri past the project_id"); 
			}
			
			List<String> ls = new ArrayList<String>();
			String[] sa = StringUtils.split((String)so.get("PROJECT_ID"),',');
			for (int i = 0; i < sa.length ; i++) {
				if (StringUtils.upperCase(sa[i]).equals(StringUtils.upperCase(PrearcUtils.COMMON))) {
					ls.add(null);
				}
				else {
					ls.add(sa[i]);
				}
			}
			return ls;
		}
	}

	/**
	 * Parse a uri that requests an action on a single session 
	 * @author aditya
	 *
	 */
	static class SessionParser implements UriParserI<Map<String,String>> {
		final PrearcUriParserUtils.UriParser i;
		SessionParser(PrearcUriParserUtils.UriParser i) {
			this.i=i;
		}
		/**
		 * Read a single session from the uri. If the project name field contains more than one
		 * project an error is thrown.
		 */
		public Map<String,String> readUri (String uri) throws MissingFormatArgumentException{
			if(uri.startsWith("/data"))uri=uri.substring(5);
			
			Map <String,Object> so = i.readUri(uri);
			if (null == so.get("PROJECT_ID")) {
				throw new java.util.MissingFormatArgumentException("Unable to parse PROJECT_ID using template from uri: " + uri);
			}

			if (null == so.get("SESSION_LABEL")) {
				throw new java.util.MissingFormatArgumentException("Unable to parse SESSION_LABEL using template from uri: " + uri);
			}
			if (null == so.get("SESSION_TIMESTAMP")) {
				throw new java.util.MissingFormatArgumentException("Unable to parse TIMESTAMP using template from uri: " + uri);
			}
			
			Reference ref = new Reference(uri);
			List<String> segs = ref.getSegments();
			if (segs.size() > 5) {
				throw new java.util.MissingFormatArgumentException("More in the uri past the TIMESTAMP"); 
			}
			
			Map<String,String> ss = new HashMap<String,String>();
			Iterator<String> i = so.keySet().iterator();
			while(i.hasNext()) {
				String key = i.next();
				if (key.equals("PROJECT_ID")){
					if (StringUtils.upperCase((String) so.get(key)).equals(StringUtils.upperCase(PrearcUtils.COMMON))) {
						ss.put(key, null);
					}
					else {
						ss.put(key, (String) so.get(key));
					}
				}
				else {
					ss.put(key, (String) so.get(key));
				}
			}
			return ss;
		}
	}

	/**
	 * A base parser that reads a uri using the given template.
	 * @author aditya
	 *
	 */
	static class UriParser implements UriParserI<Map <String, Object>> {
		String template;
		Form f = null;
		UriParser (String template) {
			this.template = template;
		}
		/**
		 * Parse the uri with the given template. No errors are thrown
		 * for fields, instead they are set to null. Users of this method beware.
		 * 
		 * It also parses the attributes and stores them in locally.
		 * 
		 */
		public Map<String,Object> readUri (String uri) {
			Template t = new Template(template, Template.MODE_STARTS_WITH, Variable.TYPE_URI_SEGMENT, "", true, false);
			Map<String,Object> so = new HashMap<String,Object>();
			t.parse(uri,so);
			Reference ref = new Reference(uri);
			this.f = new Form(ref.getQuery(), CharacterSet.UTF_8, ',');
			return so;
		}
	}
}
