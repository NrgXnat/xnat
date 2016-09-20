/*
 * core: org.nrg.xft.schema.design.SchemaElementI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.schema.design;

import java.util.ArrayList;

import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;

/**
 * @author Tim
 */
public interface SchemaElementI {

	String getSQLName();

	String getFormattedName();

    String getFullXMLName();

    String getDbName();

    GenericWrapperElement getGenericXFTElement();

    /**
     * returns SchemaFields in an ArrayList
     * @return A list of the primary keys for all of the schema fields.
     */
    ArrayList getAllPrimaryKeys();

    /**
     * @return Returns the preLoad.
     */
    boolean isPreLoad();

    /**
     * @param preLoad The preLoad to set.
     */
    void setPreLoad(boolean preLoad);

    /**
     * Gets another element indicated by the submitted ID.
     * @param s    The ID of the element to retrieve.
     * @return The indicated element if it exists.
     */
    @SuppressWarnings("unused")
    SchemaElementI getOtherElement(String s);
}
