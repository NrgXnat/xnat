/**
 * NrgServiceException
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 23, 2011
 */
package org.nrg.mail.services;

public class NrgServiceException extends Exception {
    public NrgServiceException() {
        super();
    }

    public NrgServiceException(String message) {
        super(message);
    }
    
    public NrgServiceException(Throwable exception) {
        super(exception);
    }
    
    public NrgServiceException(String message, Throwable exception) {
        super(message, exception);
    }
    
    private static final long serialVersionUID = -4841487090628530807L;
}
