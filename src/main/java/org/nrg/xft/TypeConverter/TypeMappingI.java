/*
 * org.nrg.xft.TypeConverter.TypeMappingI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
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

