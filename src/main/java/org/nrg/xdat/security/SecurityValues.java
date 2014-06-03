/*
 * org.nrg.xdat.security.SecurityValues
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.security;
import java.util.Hashtable;
import java.util.Map;
/**
 * @author Tim Olsen <tim@deck5consulting.com>
 *
 *  Basically just a map of key value pairs identifying items membership.
 *  
 *  example: a subject that is owned by project one, and shared into project 2
 *  Map{"xnat:subjectData/project":"proj1","xnat:subjectData/sharing/share":"proj2"}
 */
public class SecurityValues {
	private Map<String,Object> hash = new Hashtable<String,Object>();

	
	public void put(String field,Object value) throws Exception
	{
		hash.put(field,value);
	}
	
	public void setHash(Map<String,Object> h)
	{
		hash = h;
	}
	
	public Map<String,Object> getHash()
	{
		return hash;
	}
	
	
}

