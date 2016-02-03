/*
 * org.nrg.dcm.id.DicomProjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.dcm.id;

import org.dcm4che2.data.DicomObject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;

public interface DicomProjectIdentifier extends DicomObjectFunction {
    XnatProjectdata apply(UserI user, DicomObject o);
}
