//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 17, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.search.DisplaySearch;
/**
 * @author Tim
 *
 */
public class SearchAction extends SearchA {
    static Logger logger = Logger.getLogger(SearchAction.class);
    public DisplaySearch setupSearch(RunData data, Context context)
    {
        return null;
    }
}

