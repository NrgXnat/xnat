//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Nov 9, 2004
 */
package org.nrg.xft.references;
/**
 * Interface used by XFTManyToManyReference and XFTSuperiorReference to identify
 * if it is a XFTManyToManyReference or a XFTSuperiorReference.
 * 
 * @author Tim
 */
public interface XFTReferenceI {
	public boolean isManyToMany();
}

