/*
 * core: org.nrg.xdat.security.AuthorizerI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * 
 */
package org.nrg.xdat.security;

import org.nrg.xft.XFTItem;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;

/**
 * @author tolsen01
 *
 */
public interface AuthorizerI {
	public void authorizeRead(final XFTItem e, final UserI user) throws Exception;
	public void authorizeSave(final XFTItem e, final UserI user) throws Exception;
	public void authorizeDelete(final XFTItem e, final UserI user) throws Exception;
	public void authorizeRead(final GenericWrapperElement e, final UserI user) throws Exception;
	public void authorizeSave(final GenericWrapperElement e, final UserI user) throws Exception;
	public void authorizeDelete(final GenericWrapperElement e, final UserI user) throws Exception;
}
