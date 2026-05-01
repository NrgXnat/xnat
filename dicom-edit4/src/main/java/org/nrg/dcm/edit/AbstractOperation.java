/*
 * DicomEdit: org.nrg.dcm.edit.AbstractOperation
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;


/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public abstract class AbstractOperation implements Operation {
    protected AbstractOperation(final String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    private final String _name;
}
