// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * Created on May 1, 2006
 *
 */
package org.nrg.xft.exception;

/**
 * @author Tim
 *
 */
@SuppressWarnings("serial")
public class MetaDataException extends Exception {

    /**
     * 
     */
    public MetaDataException() {
        super();
    }

    /**
     * @param arg0
     */
    public MetaDataException(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     */
    public MetaDataException(Throwable arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public MetaDataException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
