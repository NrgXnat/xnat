/*
 * org.nrg.xft.search.QueryOrganizerI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.search;

import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;

/**
 * @author Tim
 *
 */
public interface QueryOrganizerI {
    public String translateXMLPath(String xmlPath) throws FieldNotFoundException;
    public String translateXMLPath(String xmlPath,String tableAlias) throws FieldNotFoundException;
    public String translateStandardizedPath(String xmlPath) throws FieldNotFoundException;
    public GenericWrapperElement getRootElement();
    public void addField(String xmlPath) throws ElementNotFoundException;
    
    public String buildQuery() throws IllegalAccessException,Exception;
}
