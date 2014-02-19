/*
 * org.nrg.xft.search.CriteriaI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.search;

/**
 * @author Tim
 *
 */
public interface CriteriaI extends SQLClause{
    public void setFieldByXMLPath(String xmlPath) throws Exception;
    public void setValue(Object v);
    public Object getValue();
}

