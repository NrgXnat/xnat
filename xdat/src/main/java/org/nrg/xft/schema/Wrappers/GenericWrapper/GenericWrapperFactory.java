/*
 * core: org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema.Wrappers.GenericWrapper;
import java.util.Hashtable;

import org.nrg.xft.schema.design.XFTElementWrapper;
import org.nrg.xft.schema.design.XFTFactory;
import org.nrg.xft.schema.design.XFTFactoryI;
import org.nrg.xft.schema.design.XFTFieldWrapper;
public class GenericWrapperFactory  extends XFTFactory implements XFTFactoryI {
	private static GenericWrapperFactory singleton = null;
	
	private Hashtable WRAPPED_ELEMENTS = new Hashtable();
		
	private GenericWrapperFactory(){}
		
	/**
	 * Singleton reference to the RWrapperFactory instance
	 * @return Singleton reference
	 */
	public static XFTFactoryI GetInstance()
	{
		if (singleton == null)
		{
			singleton = new GenericWrapperFactory();
		}
		return singleton;
	}
			
	/* (non-Javadoc)
	 * @see org.nrg.xft.schema.design.XFTFactory#getElementWrapper()
	 */
	public XFTElementWrapper getElementWrapper()
	{
		return new GenericWrapperElement();
	}
		
	/* (non-Javadoc)
	 * @see org.nrg.xft.schema.design.XFTFactory#getFieldWrapper()
	 */
	public XFTFieldWrapper getFieldWrapper()
	{
		return new GenericWrapperField();
	}
//	
//	/* (non-Javadoc)
//	 * @see org.nrg.xft.schema.design.XFTFactoryI#wrapElement(org.nrg.xft.schema.XFTElement)
//	 */
//	public XFTElementWrapper wrapElement(XFTElement xe)
//	{
//		if (WRAPPED_ELEMENTS.get(xe.getType().getFullForeignType())==null)
//		{
//			XFTElementWrapper wrap = getElementWrapper();
//			wrap.setWrapped(xe);
//			WRAPPED_ELEMENTS.put(xe.getType().getFullForeignType(),wrap);
//		}
//		return (XFTElementWrapper)WRAPPED_ELEMENTS.get(xe.getType().getFullForeignType());
//	}
}

