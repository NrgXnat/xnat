/*
 * org.nrg.xnat.archive.AlreadyArchivingException
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.archive;

import org.restlet.data.Status;

public class AlreadyArchivingException extends ArchivingException {
	private static final long serialVersionUID = 1L;
	private static final Status status = Status.CLIENT_ERROR_FORBIDDEN;
	private static final String message = "Session archiving already in progress";
	
	public AlreadyArchivingException() {
		super(status, message);
	}
}
