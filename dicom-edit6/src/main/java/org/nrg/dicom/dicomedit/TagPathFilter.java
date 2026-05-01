/*
 * DicomEdit: TagPathFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.tags.TagPath;

public interface TagPathFilter {
    boolean allow(TagPath tagPath);

    boolean deny(TagPath tagPath);
}
