//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 26, 2004
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

