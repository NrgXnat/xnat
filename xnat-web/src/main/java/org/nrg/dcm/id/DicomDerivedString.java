/*
 * org.nrg.dcm.id.DicomDerivedString
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.dcm.id;

import com.google.common.base.Function;
import org.dcm4che2.data.DicomObject;

public interface DicomDerivedString extends DicomObjectFunction,Function<DicomObject,String> {
}
