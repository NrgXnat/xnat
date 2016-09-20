/*
 * core: org.nrg.xft.exception.InvalidReference
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.exception;

@SuppressWarnings("serial")
public class InvalidReference extends XFTInitException {
	public InvalidReference(String message)
	{
		super(message);
	}
}

