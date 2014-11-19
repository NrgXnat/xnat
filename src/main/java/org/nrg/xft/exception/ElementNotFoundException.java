/*
 * org.nrg.xft.exception.ElementNotFoundException
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.exception;

@SuppressWarnings("serial")
public class ElementNotFoundException extends Exception{
	public String ELEMENT = "";
	public ElementNotFoundException(String name)
	{
		super("Element not found: '" + name + "'");
		ELEMENT = name;
	}
}

