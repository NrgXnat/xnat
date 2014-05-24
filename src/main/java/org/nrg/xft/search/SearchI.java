//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Feb 7, 2005
 *
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

