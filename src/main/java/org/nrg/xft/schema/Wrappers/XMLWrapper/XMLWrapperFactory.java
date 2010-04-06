//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 26, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.nrg.xft.schema.Wrappers.XMLWrapper;

import org.nrg.xft.schema.design.XFTElementWrapper;
import org.nrg.xft.schema.design.XFTFactory;
import org.nrg.xft.schema.design.XFTFactoryI;
import org.nrg.xft.schema.design.XFTFieldWrapper;
/**
 * @author Tim
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
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

