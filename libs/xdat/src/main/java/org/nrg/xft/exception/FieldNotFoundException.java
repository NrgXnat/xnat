/*
 * core: org.nrg.xft.exception.FieldNotFoundException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.exception;

@SuppressWarnings("serial")
public class FieldNotFoundException extends XftItemException {
	public final String FIELD;
    public final String MESSAGE;

	public FieldNotFoundException(final String name) {
        this(name, "Field not found: '" + name + "'");
	}

    public FieldNotFoundException(final String name, final String message) {
        super(message);
        FIELD = name;
        MESSAGE = message;
    }
}

