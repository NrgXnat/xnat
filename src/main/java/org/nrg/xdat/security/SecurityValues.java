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
/**
 * @author Tim
 *
 */
public class SecurityValues {
	private Hashtable hash = new Hashtable();

	
	public void put(String field,Object value) throws Exception
	{
		hash.put(field,value);
	}
	
	public void setHash(Hashtable h)
	{
		hash = h;
	}
	
	public Hashtable getHash()
	{
		return hash;
	}
	
	
}

