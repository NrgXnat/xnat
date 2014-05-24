//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 17, 2004
 */
package org.nrg.xft.schema;

/**
 * This class contains additional details about an XFTSchema that relate directly 
 * to the schema's use in an external webapp.
 * 
 * @author Tim
 */
public abstract class XFTWebAppSchema {
	public abstract String toString(String header);
}

