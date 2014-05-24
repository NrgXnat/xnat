//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 19, 2005
 *
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

