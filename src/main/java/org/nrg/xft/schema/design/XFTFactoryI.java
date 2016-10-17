/*
 * core: org.nrg.xft.schema.design.XFTFactoryI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema.design;
import org.nrg.xft.schema.XFTElement;
import org.nrg.xft.schema.XFTField;

import java.util.ArrayList;
import java.util.Collection;
public interface XFTFactoryI {
	/**
	 * @param elements
	 * @return Returns a list of the elements after wrapping them
	 */
	public ArrayList loadElements(Collection elements);
	/**
	 * @param xe
	 * @return Returns XFTElementWrapper after wrapping provided element in it
	 */
	public XFTElementWrapper wrapElement(XFTElement xe);
	/**
	 * @param xe
	 * @return Returns XFTFieldWrapper after wrapping provided field in it
	 */
	public XFTFieldWrapper wrapField(XFTField xe);
	/**
	 * @param xe
	 * @return Returns rewrapped field
	 */
	public XFTFieldWrapper convertField(XFTFieldWrapper xe);
	/**
	 * @param xe
	 * @return Returns rewrapped element
	 */
	public XFTElementWrapper convertElement(XFTElementWrapper xe);
}

