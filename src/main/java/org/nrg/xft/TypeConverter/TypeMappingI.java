//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Apr 1, 2004
 */
package org.nrg.xft.TypeConverter;
import java.util.Hashtable;
/**
 * @author Tim
 */
public interface TypeMappingI {
	public Hashtable getMapping();
	public String getName();
}

