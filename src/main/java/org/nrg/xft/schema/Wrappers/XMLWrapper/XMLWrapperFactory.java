/*
 * org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.schema.Wrappers.XMLWrapper;

import org.nrg.xft.schema.design.XFTElementWrapper;
import org.nrg.xft.schema.design.XFTFactory;
import org.nrg.xft.schema.design.XFTFactoryI;
import org.nrg.xft.schema.design.XFTFieldWrapper;
public class XMLWrapperFactory extends XFTFactory {
	private static XMLWrapperFactory singleton = null;
	
	private XMLWrapperFactory(){}
	
	public static XFTFactoryI GetInstance()
	{
		if (singleton == null)
		{
			singleton = new XMLWrapperFactory();
		}
		return singleton;
	}
		
	public XFTElementWrapper getElementWrapper()
	{
		return new XMLWrapperElement();
	}
	
	public XFTFieldWrapper getFieldWrapper()
	{
		return new XMLWrapperField();
	}
		
}

