/*
 * core: org.nrg.xft.search.TableSearchI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.search;

import org.nrg.xft.XFTTableI;

/**
 * @author Tim
 *
 */
public interface TableSearchI extends SearchI{
    public void addCriteria(String xmlPath, String comparisonType, Object value) throws Exception;
    public XFTTableI execute(String userName) throws Exception;
}

