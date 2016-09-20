/*
 * core: org.nrg.xdat.bean.ClassMappingFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.bean;

public class ClassMappingFactory {
	private static ClassMappingI mapping=null;
	
	public static ClassMappingI getInstance() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		if(mapping==null){
			mapping=(ClassMappingI)Class.forName("org.nrg.xdat.bean.ClassMapping").newInstance();
		}
		return mapping;
	}
}
