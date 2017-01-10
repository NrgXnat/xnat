/*
 * web: org.nrg.action.ServerException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.action;

import org.restlet.data.Status;

public class ServerException extends ActionException {
	public ServerException(String msg,Exception e){
		super(msg,e);
	}
	public ServerException(String msg){
		super(msg);
	}
	public ServerException(Status s, String msg,Throwable e){
		super(s,msg,e);
	}
    public ServerException(Status s, String message){
        super(message);
        status=s;
    }
	public ServerException(Status s, Throwable e){
		super(s,e);
	}
	public ServerException(Throwable e){
		super(e);
	}
	
	@Override
	public Status getStatus() {
		return (status==null)?Status.SERVER_ERROR_INTERNAL:status;
	}
}
