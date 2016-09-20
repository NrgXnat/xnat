/*
 * core: org.nrg.xft.cl.ExtensibleClassLoader
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.cl;

import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;

public class ExtensibleClassLoader implements ExtensibleClassLoaderI{
	public static final String EMAIL_IMPL = "EMAIL_IMPL";
	static Logger logger = Logger.getLogger(ExtensibleClassLoader.class);
	Map<String,Class> props=new Hashtable<String,Class>();
	
	private static ExtensibleClassLoaderI loader=null; 
	
	public ExtensibleClassLoader(){
		props.put(EMAIL_IMPL, org.nrg.xft.email.EmailerImpl.class);
	}
	
	public Class getClass(String identifier) {
		return props.get(identifier);
	}

	
	public static Class GetClass(String identifier) throws InstantiationException,IllegalAccessException,ClassNotFoundException{
		Class match;
		if(loader==null){
			try {
				Class c=Class.forName("org.nrg.xnat.custom.cl.ExtensibleClassLoader");
				loader=(ExtensibleClassLoaderI)c.newInstance();
			} catch (ClassNotFoundException e) {
				try {
					Class c=Class.forName("org.nrg.xnat.cl.ExtensibleClassLoader");
					loader=(ExtensibleClassLoaderI)c.newInstance();
				} catch (ClassNotFoundException e1) {
					loader=new ExtensibleClassLoader();
				}
			}
		}
		
		match = loader.getClass(identifier);
		
		if(match==null){
			throw new ClassNotFoundException(identifier);
		}

		
		return match;
	}
}
