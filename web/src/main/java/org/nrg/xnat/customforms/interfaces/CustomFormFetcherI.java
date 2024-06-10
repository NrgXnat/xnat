/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * @author: Mohana Ramaratnam (mohana@radiologics.com)
 * @since: 07-03-2021
 */
package org.nrg.xnat.customforms.interfaces;

import org.nrg.xft.security.UserI;

public interface CustomFormFetcherI {

    String getCustomForm(UserI user, String xsiType, String id, String projectIdQueryParam, String visitId, String subType, boolean appendPreviousNextButtons) throws Exception;

}