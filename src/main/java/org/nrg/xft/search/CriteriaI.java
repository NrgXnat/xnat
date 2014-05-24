//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 29, 2005
 *
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

