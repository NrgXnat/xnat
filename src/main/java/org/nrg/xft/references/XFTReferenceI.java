/*
 * core: org.nrg.xft.references.XFTReferenceI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.references;
public interface XFTReferenceI extends Comparable<XFTReferenceI>{
	public boolean isManyToMany();
	public int compareTo(XFTReferenceI ref);
}

