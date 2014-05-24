//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 26, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.nrg.xft.schema.design;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.nrg.xft.schema.XFTElement;
import org.nrg.xft.schema.XFTField;
/**
 * This class is used to wrap XFTElements and XFTFields to a given Wrapped type.
 * 
 * @author Tim
 */
public abstract class XFTFactory implements XFTFactoryI {
		
		public abstract XFTElementWrapper getElementWrapper();
		public abstract XFTFieldWrapper getFieldWrapper();

		/* (non-Javadoc)
		 * @see org.nrg.xft.schema.design.XFTFactoryI#loadElements(java.util.Collection)
		 */
		public ArrayList loadElements(Collection elements)
		{
			ArrayList al = new ArrayList();
			if (elements != null)
			{
				if (elements.size() > 0)
				{
					Iterator it = elements.iterator();
					while (it.hasNext())
					{
						XFTElement xe = (XFTElement)it.next();
						al.add(wrapElement(xe));
					}
				}
			}
			al.trimToSize();
			return al;
		}
	
		/* (non-Javadoc)
		 * @see org.nrg.xft.schema.design.XFTFactoryI#wrapElement(org.nrg.xft.schema.XFTElement)
		 */
		public XFTElementWrapper wrapElement(XFTElement xe)
		{
			XFTElementWrapper wrap = getElementWrapper();
			wrap.setWrapped(xe);
			return wrap;
		}
		
		/* (non-Javadoc)
		 * @see org.nrg.xft.schema.design.XFTFactoryI#wrapField(org.nrg.xft.schema.XFTField)
		 */
		public XFTFieldWrapper wrapField(XFTField xe)
		{
			XFTFieldWrapper wrap = getFieldWrapper();
			wrap.setWrapped(xe);
			return wrap;
		}
		
		/* (non-Javadoc)
		 * @see org.nrg.xft.schema.design.XFTFactoryI#convertField(org.nrg.xft.schema.design.XFTFieldWrapper)
		 */
		public XFTFieldWrapper convertField(XFTFieldWrapper temp)
		{
			return wrapField(temp.getWrapped());
		}
		
		/* (non-Javadoc)
		 * @see org.nrg.xft.schema.design.XFTFactoryI#convertElement(org.nrg.xft.schema.design.XFTElementWrapper)
		 */
		public XFTElementWrapper convertElement(XFTElementWrapper temp)
		{
			return wrapElement(temp.getWrapped());
		}
}

