/*
 * core: org.nrg.xft.exception.MetaDataException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
