//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 10, 2005
 *
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

