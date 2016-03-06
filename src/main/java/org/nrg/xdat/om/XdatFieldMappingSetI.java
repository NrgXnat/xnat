/*
 * org.nrg.xdat.om.XdatFieldMappingSetI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.om;

import java.util.ArrayList;

import org.nrg.xft.ItemI;

/**
 * @author XDAT
 */
public interface XdatFieldMappingSetI {

    /**
     * Gets the schema element name.
     *
     * @return The schema element name.
     */
    String getSchemaElementName();

    /**
     * allow
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatFieldMappingI
     */
    ArrayList getAllow();

    /**
     * Sets the value for allow.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    void setAllow(ItemI v) throws Exception;

    /**
     * sub_set
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatFieldMappingSetI
     */
    ArrayList getSubSet();

    /**
     * Sets the value for sub_set.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unused")
    void setSubSet(ItemI v) throws Exception;

    /**
     * @return Returns the method.
     */
    String getMethod();

    /**
     * Sets the value for method.
     *
     * @param v Value to Set.
     */
    void setMethod(String v);

    /**
     * @return Returns the xdat_field_mapping_set_id.
     */
    Integer getXdatFieldMappingSetId();

    /**
     * Sets the value for xdat_field_mapping_set_id.
     *
     * @param v Value to Set.
     */
    @SuppressWarnings("unused")
    void setXdatFieldMappingSetId(Integer v);
}
