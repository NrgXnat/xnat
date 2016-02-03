/*
 * org.nrg.xnat.turbine.modules.actions.BulkDeleteAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/4/13 9:51 AM
 */
package org.nrg.xnat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.nrg.xdat.turbine.modules.actions.ListingAction;

public class BulkDeleteAction  extends ListingAction {

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.actions.ListingAction#getDestinationScreenName(org.apache.turbine.util.RunData)
     */
    @Override
    public String getDestinationScreenName(RunData data) {
        return "BulkDeleteActionScreen.vm";
    }
    
}

