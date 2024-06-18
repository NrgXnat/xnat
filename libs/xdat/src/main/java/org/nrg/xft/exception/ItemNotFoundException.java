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
 */
@SuppressWarnings("serial")
public class ItemNotFoundException extends XftItemException {
	public final String ELEMENT;
	public final String ID;

	public ItemNotFoundException(final String name) {
		this("Item not found: '" + name + "'", name, name);
	}

	public ItemNotFoundException(final String xsiType, final String id) {
		this("Item not found: '" + xsiType + "' with ID " + id, xsiType, id);
	}
	
	@Override
	public String toString() {
		return ELEMENT + "' with ID " + ID;
	}

	private ItemNotFoundException(final String message, final String element, final String id) {
		super(message);
		ELEMENT = element;
		ID = id;
	}
}

