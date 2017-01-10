/*
 * web: org.nrg.xnat.turbine.utils.CatalogSet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.utils;

import org.nrg.xdat.bean.CatCatalogBean;

import java.util.Hashtable;

public class CatalogSet {
    public CatCatalogBean catalog = null;
    public Hashtable<String,Object> hash = null;
    
    public CatalogSet(CatCatalogBean c, Hashtable<String,Object> h){
        catalog=c;
        hash=h;
    }
}
