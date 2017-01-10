/*
 * web: org.nrg.dcm.id.DicomProjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import org.dcm4che2.data.DicomObject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;

public interface DicomProjectIdentifier extends DicomObjectFunction {
    XnatProjectdata apply(UserI user, DicomObject o);
}
