/*
 * core: org.nrg.xdat.schema.SchemaField
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.schema;

import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.identifier.Identifier;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.schema.design.SchemaFieldI;
import org.nrg.xft.utils.ValidationUtils.XFTValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.List;

/**
 * @author Tim
 *
 */
public class SchemaField implements Identifier, SchemaFieldI{
    private static final Logger logger = LoggerFactory.getLogger(SchemaField.class);
    GenericWrapperField wrapped = null;
    Hashtable possibleValues = null;

    public SchemaField(GenericWrapperField f)
    {
        wrapped = f;
    }

    public SchemaField(String xmlPath) throws ElementNotFoundException,FieldNotFoundException
    {
        try {
            wrapped = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
        } catch (XFTInitException e) {
            logger.error("",e);
        }
    }

    public GenericWrapperField getWrapped()
    {
        return wrapped;
    }

    public String getId()
    {
        return wrapped.getId();
    }

    public String getName()
    {
        return wrapped.getName();
    }

    public String getSQLName()
    {
        return wrapped.getSQLName();
    }

    public String getXMLPathString(String elementName)
    {
        return wrapped.getXMLPathString(elementName);
    }

    public boolean isReference()
    {
        return wrapped.isReference();
    }

    public SchemaElementI getParentElement()
    {
        return new SchemaElement((wrapped.getParentElement().getGenericXFTElement()));
    }

    public SchemaElementI getReferenceElement() throws XFTInitException,ElementNotFoundException
    {
        try {
            return new SchemaElement(wrapped.getReferenceElement().getGenericXFTElement());
        } catch (XFTInitException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Hashtable getPossibleValues(String login)throws Exception
    {
        if (possibleValues == null)
        {
            possibleValues = new Hashtable();

            if (isReference())
            {
                possibleValues = ((SchemaElement)getReferenceElement()).getDistinctIdentifierValues(login);
            }else{
                try {
                    List<String> values = XFTValidator.GetPossibleValues(wrapped.getParentElement().getFullXMLName(),wrapped.getSQLName());
                    for (final String value : values) {
                        possibleValues.put(value, value);
                    }
                } catch (XFTInitException e) {
                    logger.error("",e);
                } catch (Exception e) {
                    logger.error("",e);
                }
            }
        }

        return possibleValues;
    }

    public boolean isBooleanField()
    {
        return !isReference() && wrapped.getXMLType().getLocalType().equalsIgnoreCase("boolean");
    }

    public GenericWrapperField getGenericXFTField(){
        return this.wrapped;
    }

}