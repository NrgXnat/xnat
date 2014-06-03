/*
 * org.nrg.xft.schema.Wrappers.XMLWrapper.XMLNode
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
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

