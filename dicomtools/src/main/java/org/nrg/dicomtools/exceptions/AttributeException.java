/*
 * DicomEdit: org.nrg.dicomtools.exceptions.AttributeException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicomtools.exceptions;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public abstract class AttributeException extends Exception {
    public AttributeException(final String s) {
        super(s);
    }
}
