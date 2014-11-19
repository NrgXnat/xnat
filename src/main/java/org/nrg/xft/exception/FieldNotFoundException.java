/*
 * org.nrg.xft.exception.FieldNotFoundException
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
public class FieldNotFoundException extends Exception{
	public String FIELD = "";
    public String MESSAGE = "";
	public FieldNotFoundException(String name)
	{
        super("Field not found: '" + name + "'");
        FIELD = name;
        MESSAGE = "Field not found: '" + name + "'";
	}

    public FieldNotFoundException(String name, String message)
    {
        super(message);
        FIELD = name;
        MESSAGE = message;
    }
}

