/*
 * core: org.nrg.xft.exception.DuplicateKeyException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Tim
 *
 */
@SuppressWarnings("serial")
public class DuplicateKeyException extends XftItemException {
	public final String FIELD;
	public final String ELEMENT;

	public DuplicateKeyException(final String name) {
		this(name, "");
	}

	public DuplicateKeyException(final String element, final String name) {
		super(StringUtils.isBlank(element) ? "Duplicate Field: '" + name + "'" : "Duplicate Field: '" + element + "'.'" + name + "'");
		FIELD = name;
		ELEMENT = StringUtils.defaultIfBlank(element, "");
	}
}
