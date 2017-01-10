/*
 * web: org.nrg.xnat.turbine.modules.actions.ExampleListingAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.nrg.xdat.turbine.modules.actions.ListingAction;

public class ExampleListingAction  extends ListingAction {

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.actions.ListingAction#getDestinationScreenName(org.apache.turbine.util.RunData)
     */
    @Override
    public String getDestinationScreenName(RunData data) {
        return "ExampleListingActionScreen.vm";
    }
    
}

