/*
 * core: org.nrg.xft.exception.DuplicateKeyException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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

