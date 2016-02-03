/*
 * org.nrg.xnat.helpers.prearchive.SessionException
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
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
