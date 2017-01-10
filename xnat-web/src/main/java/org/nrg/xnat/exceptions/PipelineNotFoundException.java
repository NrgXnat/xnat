/*
 * web: org.nrg.xnat.exceptions.PipelineNotFoundException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.exceptions;

public class PipelineNotFoundException extends Exception {

    public PipelineNotFoundException() {

    }



    /**

     * @param message

     */

    public PipelineNotFoundException(String message) {

        super(message);

    }



    /**

     * @param cause

     */

    public PipelineNotFoundException(Throwable cause) {

        super(cause);

    }



    /**

     * @param message

     * @param cause

     */

    public PipelineNotFoundException(String message, Throwable cause) {

        super(message, cause);

    }


	
}
