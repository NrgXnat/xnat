//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 10, 2005
 *
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

