/*
 * web: org.nrg.xnat.turbine.utils.ArchivableItem
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.utils;

import org.nrg.xdat.om.base.BaseXnatExperimentdata.UnknownPrimaryProjectException;
import org.nrg.xft.ItemI;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;

import java.io.File;

public interface ArchivableItem extends ItemI {
    String getArchiveDirectoryName();

    File getExpectedCurrentDirectory() throws InvalidArchiveStructure, UnknownPrimaryProjectException;

    String getXSIType();

    String getId();

    String getProject();

    String getArchiveRootPath() throws UnknownPrimaryProjectException;
}
