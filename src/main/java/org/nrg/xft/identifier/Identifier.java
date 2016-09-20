/*
 * core: org.nrg.xft.identifier.Identifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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

