/*
 * core: org.nrg.xft.exception.ItemNotFoundException
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
public class ItemNotFoundException  extends Exception{
	public String ELEMENT = "";
	public ItemNotFoundException(String name)
	{
		super("Item not found: '" + name + "'");
		ELEMENT = name;
	}
}

