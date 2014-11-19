/*
 * org.nrg.xdat.turbine.modules.actions.SearchAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/9/13 1:06 PM
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

