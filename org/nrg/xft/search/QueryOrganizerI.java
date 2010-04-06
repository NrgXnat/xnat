//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on May 10, 2005
 *
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
