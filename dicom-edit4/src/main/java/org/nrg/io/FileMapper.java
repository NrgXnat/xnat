/*
 * DicomEdit: org.nrg.io.FileMapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.io;

import java.io.File;

@Deprecated
public interface FileMapper {
    File apply(File original);
}
