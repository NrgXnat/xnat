/*
 * org.nrg.xnat.turbine.modules.actions.SetEmailSpecs
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.ArcArchivespecification;
import org.nrg.xdat.turbine.modules.actions.AdminAction;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

public class SetEmailSpecs extends AdminAction {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocitySecureAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        XFTItem item = PopulateItem.Populate(data,"arc:ArchiveSpecification",true).getItem();
        item.setUser(TurbineUtils.getUser(data));
        ArcSpecManager.save(new ArcArchivespecification(item), newEventInstance(data, EventUtils.CATEGORY.SIDE_ADMIN, "Modified email specifications."));
    }

}
