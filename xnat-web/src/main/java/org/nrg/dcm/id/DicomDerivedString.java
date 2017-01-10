/*
 * web: org.nrg.dcm.id.DicomDerivedString
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import com.google.common.base.Function;
import org.dcm4che2.data.DicomObject;

public interface DicomDerivedString extends DicomObjectFunction,Function<DicomObject,String> {
}
