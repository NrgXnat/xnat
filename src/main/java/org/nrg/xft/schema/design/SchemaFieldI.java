/*
 * core: org.nrg.xft.schema.design.SchemaFieldI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema.design;

import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;

/**
 * @author Tim
 *
 */
public interface SchemaFieldI {
    public abstract String getId();

    public abstract String getName();

    public abstract String getSQLName();

    public abstract String getXMLPathString(String elementName);

    public abstract boolean isReference();

    public abstract GenericWrapperField getGenericXFTField();
    
    public abstract SchemaElementI getParentElement();

    public abstract SchemaElementI getReferenceElement()
            throws XFTInitException,ElementNotFoundException;


    public abstract boolean isBooleanField();

}
