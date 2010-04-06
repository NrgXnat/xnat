//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
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

