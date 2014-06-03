/*
 * org.nrg.xft.exception.DuplicateKeyException
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.exception;

/**
 * @author Tim
 *
 */
@SuppressWarnings("serial")
public class DuplicateKeyException extends Exception{
	public String FIELD = "";
	public String ELEMENT = "";
	public DuplicateKeyException(String element,String name)
	{
		super("Duplicate Field: '" + element + "'.'" + name + "'");
		FIELD = name;
		
		ELEMENT = element;
	}
	public DuplicateKeyException(String name)
	{
		super("Duplicate Field: '" + name + "'");
		FIELD = name;
	}
}

