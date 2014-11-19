/*
 * org.nrg.xft.identifier.Identifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.identifier;

/**
 * @author Tim
 *
 */
public interface Identifier {
	
	/**
	 * @return String which uniquely identifies this object (id or name)
	 */
	public String getId();
}

