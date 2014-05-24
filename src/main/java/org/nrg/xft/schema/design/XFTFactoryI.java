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

import org.nrg.xft.schema.XFTElement;
import org.nrg.xft.schema.XFTField;
/**
 * Interface for Factory implementation.
 * 
 * @author Tim
 */
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

