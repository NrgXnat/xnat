//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Jul 14, 2005
 *
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
