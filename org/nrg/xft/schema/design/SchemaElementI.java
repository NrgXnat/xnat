//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Jul 14, 2005
 *
 */
package org.nrg.xft.schema.design;

import java.util.ArrayList;

import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;

/**
 * @author Tim
 *
 */
public interface SchemaElementI {

	public abstract String getSQLName();

	public abstract String getFormattedName();

    public abstract String getFullXMLName();

    public abstract String getDbName();

    public abstract GenericWrapperElement getGenericXFTElement();

   // public abstract ArrayList getSchemaFields();

    /**
     * returns SchemaFields in an ArrayList
     * @return
     */
    public abstract ArrayList getAllPrimaryKeys();

    /**
     * @return Returns the preLoad.
     */
    public abstract boolean isPreLoad();

    /**
     * @param preLoad The preLoad to set.
     */
    public abstract void setPreLoad(boolean preLoad);
    
    public SchemaElementI getOtherElement(String s);
}
