/*
 * core: org.nrg.xft.exception.ElementNotFoundException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.exception;

@SuppressWarnings("serial")
public class ElementNotFoundException extends XftItemException {
	public final String ELEMENT;
	public ElementNotFoundException(final String name) {
		super("Element not found: '" + name + "'");
		ELEMENT = name;
	}
}

