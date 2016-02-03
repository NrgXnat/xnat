/*
 * org.nrg.xnat.turbine.utils.ArchivableItem
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.utils;

import org.nrg.xdat.om.base.BaseXnatExperimentdata.UnknownPrimaryProjectException;
import org.nrg.xft.ItemI;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;

import java.io.File;

public interface ArchivableItem extends ItemI{
	public String getArchiveDirectoryName();
	public File getExpectedCurrentDirectory() throws InvalidArchiveStructure, UnknownPrimaryProjectException;
	public String getXSIType();
	public String getId();
	public String getProject();
	public String getArchiveRootPath() throws UnknownPrimaryProjectException;
}
