/*
 * web: org.nrg.xnat.helpers.prearchive.ProjectPrearchiveI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

import org.nrg.xft.XFTTable;

import java.util.Date;

public interface ProjectPrearchiveI {

	public abstract Date getLastMod();

	public abstract XFTTable getContent();

}
