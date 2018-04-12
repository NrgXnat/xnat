/*
 * web: org.nrg.xnat.helpers.prearchive.SessionException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

public class SessionException extends Exception {
	public SessionException (final Error error, final String message) {
		super (message);
		_error = error;
	}

	public Error getError() {
		return _error;
	}

	public enum Error {
		AlreadyExists,
		DoesntExist,
        NoProjectSpecified,
        InvalidStatus,
        InvalidSession,
        DatabaseError
	}

	private final Error _error;
}
