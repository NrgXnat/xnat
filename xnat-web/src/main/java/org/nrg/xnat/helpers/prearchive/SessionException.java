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
	private static final long serialVersionUID = 1L;
	public SessionException (String err) {
		super (err);
	}
	public SessionException () {
		super ();
	}
}
