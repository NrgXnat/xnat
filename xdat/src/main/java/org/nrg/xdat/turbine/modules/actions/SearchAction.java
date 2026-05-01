/*
 * core: org.nrg.xdat.turbine.modules.actions.SearchAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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

