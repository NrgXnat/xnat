/*
 * org.nrg.xft.schema.design.XFTFactoryI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.schema.design;
import java.util.ArrayList;
import java.util.Collection;

import org.nrg.xft.schema.XFTElement;
import org.nrg.xft.schema.XFTField;
public interface XFTFactoryI {
	/**
	 * @param elements
	 * @return
	 */
	public ArrayList loadElements(Collection elements);
	/**
	 * @param xe
	 * @return
	 */
	public XFTElementWrapper wrapElement(XFTElement xe);
	/**
	 * @param xe
	 * @return
	 */
	public XFTFieldWrapper wrapField(XFTField xe);
	/**
	 * @param xe
	 * @return
	 */
	public XFTFieldWrapper convertField(XFTFieldWrapper xe);
	/**
	 * @param xe
	 * @return
	 */
	public XFTElementWrapper convertElement(XFTElementWrapper xe);
}

