/*
 * org.nrg.xnat.archive.ArchivingException
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

public class ArchivingException extends Exception {
	private static final long serialVersionUID = 1L;
	private static final Status DEFAULT_STATUS = Status.SERVER_ERROR_INTERNAL;
	private final Status status;
	
	public ArchivingException(Status status) {
		super();
		this.status = status;
	}
	
	public ArchivingException() {
		this(DEFAULT_STATUS);
	}

	public ArchivingException(Status status, String message) {
		super(message);
		this.status = status;
	}
	
	public ArchivingException(String message) {
		this(DEFAULT_STATUS, message);
	}

	public ArchivingException(Status status, Throwable cause) {
		super(cause);
		this.status = status;
	}
	
	public ArchivingException(Throwable cause) {
		this(DEFAULT_STATUS, cause);
	}

	public ArchivingException(Status status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	public ArchivingException(String message, Throwable cause) {
		this(DEFAULT_STATUS, message, cause);
	}

	/**
	 * What is the HTTP Status code for the response?
	 * @return Restlet wrapper around an HTTP status code
	 */
	public final Status getStatus() {
		return status;
	}
}
