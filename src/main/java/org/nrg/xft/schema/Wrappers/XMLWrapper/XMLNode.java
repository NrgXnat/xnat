/*
 * core: org.nrg.xft.schema.Wrappers.XMLWrapper.XMLNode
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema.Wrappers.XMLWrapper;

import java.util.ArrayList;

/**
 * @author Tim
 */
public interface XMLNode {
	public abstract String getName();

	public abstract ArrayList getChildren();
	public abstract ArrayList getAttributes();
}

