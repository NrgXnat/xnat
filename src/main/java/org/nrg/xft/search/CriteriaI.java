/*
 * core: org.nrg.xft.search.CriteriaI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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

