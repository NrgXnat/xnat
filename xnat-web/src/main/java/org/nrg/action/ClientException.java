/*
 * org.nrg.action.ClientException
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

public class ClientException extends ActionException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ClientException(String msg,Throwable e){
		super(msg,e);
	}
	public ClientException(String msg){
		super(msg);
	}
	public ClientException(Status s, String msg,Throwable e){
		super(s,msg,e);
	}
    public ClientException(Status s, String message){
        super(message);
        status=s;
    }
	public ClientException(Status s, Throwable e){
		super(s,e);
	}
	public ClientException(Throwable e){
		super(e);
	}
	
	@Override
	public Status getStatus() {
		return (status==null)?Status.CLIENT_ERROR_BAD_REQUEST:status;
	}
}
