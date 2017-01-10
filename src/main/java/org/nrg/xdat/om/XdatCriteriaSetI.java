/*
 * core: org.nrg.xdat.om.XdatCriteriaSetI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;

import java.util.ArrayList;

import org.nrg.xft.ItemI;

/**
 * @author XDAT
 */
public interface XdatCriteriaSetI {

    String getSchemaElementName();

    /**
     * criteria
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatCriteriaI
     */
    ArrayList getCriteria();

    /**
     * Sets the value for criteria.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    void setCriteria(ItemI v) throws Exception;

    /**
     * child_set
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatCriteriaSetI
     */
    ArrayList getChildSet();

    /**
     * Sets the value for child_set.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    void setChildSet(ItemI v) throws Exception;

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
     * @return Returns the xdat_criteria_set_id.
     */
    @SuppressWarnings("unused")
    Integer getXdatCriteriaSetId();

    /**
     * Sets the value for xdat_criteria_set_id.
     *
     * @param v Value to Set.
     */
    @SuppressWarnings("unused")
    void setXdatCriteriaSetId(Integer v);
}
