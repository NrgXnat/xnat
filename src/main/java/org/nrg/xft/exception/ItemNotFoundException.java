/*
 * core: org.nrg.xft.exception.ItemNotFoundException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
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
	public final String ELEMENT;
	public final String ID;

	public ItemNotFoundException(final String name) {
		super("Item not found: '" + name + "'");
		ELEMENT = name;
		ID = name;
	}

	public ItemNotFoundException(final String xsiType, final String id) {
		super("Item not found: '" + xsiType + "' with ID " + id);
		ELEMENT = xsiType;
		ID = id;
	}
	
	@Override
	public String toString() {
		return ELEMENT + "' with ID " + ID;
	}
}

