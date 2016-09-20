/*
 * core: org.nrg.xft.TypeConverter.TypeMappingI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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

