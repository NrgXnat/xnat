/*
 * org.nrg.xft.search.TableSearchI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
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

