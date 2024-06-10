/*
 * web: org.nrg.xnat.archive.ValidationException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.restlet.data.Status;

import java.util.Arrays;

public class ValidationException extends ArchivingException {
	private static final long serialVersionUID = 1L;
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final Status status = Status.SERVER_ERROR_INTERNAL;
	private final ValidationResultsI results;
	
	/**
	 * @param results cause of the ValidationException: must not be valid
	 */
	public ValidationException(final ValidationResults results) {
		super(status, makeMessage(results));
		if (results.isValid()) {
			throw new IllegalArgumentException("validation must have failed");
		}
		this.results = results;
	}
	
	/**
	 * Retrieves the ValidationResults underlying this exception.
	 * @return ValidationResults
	 */
	public ValidationResultsI getValidationResults() {
		return results;
	}
	
	private static String makeMessage(final ValidationResults results) {
		final StringBuilder sb = new StringBuilder();
		for (final Object[] result : results.getResults()) {
			sb.append(Arrays.asList(result)).append(LINE_SEPARATOR);
		}
		return sb.toString();
	}
}
