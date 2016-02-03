/*
 * org.nrg.action.ActionException
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.action;

import org.restlet.data.Status;

public abstract class ActionException extends Exception {
	private static final long serialVersionUID = -2423585253188531015L;
	public Status status = null;

	public ActionException() {
		super();
	}

	public ActionException(String message) {
		super(message);
	}

	public ActionException(Throwable cause) {
		super(cause);
	}

	public ActionException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ActionException(Status s, String msg,Throwable e){
		super(msg,e);
		status=s;
	}
	
	public ActionException(Status s, String message){
		super(message);
		status=s;
	}

	public ActionException(Status s, Throwable e){
		super(e);
		status=s;
	}

	public abstract Status getStatus();
}