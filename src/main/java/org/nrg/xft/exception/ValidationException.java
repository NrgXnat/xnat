/*
 * core: org.nrg.xft.exception.ValidationException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.exception;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;

@Getter
@Accessors(prefix = "_")
public class ValidationException extends XftItemException {
	public ValidationException(final ValidationResults validation) {
		super(validation.toFullString());
		_validation = validation;
	}

	public ValidationException(final String message, final ValidationResults validation) {
		super(message);
		_validation = validation;
	}

	private final ValidationResults _validation;
}

