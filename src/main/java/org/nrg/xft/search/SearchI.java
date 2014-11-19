/*
 * org.nrg.xft.search.SearchI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.search;

import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;

/**
 * @author Tim
 *
 */
public interface SearchI {
	public UserI getUser();
	public void setUser(UserI u);
	public void setElement(GenericWrapperElement e);
	public GenericWrapperElement getElement();
	public void setCriteriaCollection(CriteriaCollection cc);
	public CriteriaCollection getCriteriaCollection();
}

