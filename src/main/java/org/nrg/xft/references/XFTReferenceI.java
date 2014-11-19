/*
 * org.nrg.xft.references.XFTReferenceI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.references;
public interface XFTReferenceI extends Comparable<XFTReferenceI>{
	public boolean isManyToMany();
	public int compareTo(XFTReferenceI ref);
}

