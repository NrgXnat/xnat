/*
 * core: org.nrg.xft.cl.ExtensibleClassLoaderI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.cl;

public interface ExtensibleClassLoaderI {
	public Class getClass(String identifier);
}
