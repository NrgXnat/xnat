/*
 * core: org.nrg.xdat.turbine.modules.screens.DefaultReport
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;

/**
 * @author Tim
 */
@Slf4j
public class DefaultReport extends SecureReport {
    public void finalProcessing(final RunData data, final Context context) {
        try {
            if (TurbineUtils.isAccessibleItem(getUser(), item)) {
                context.put("data_item", item.toHTML());
            } else {
                TurbineUtils.denyAccess(data);
            }
        } catch (Exception e) {
            try {
                log.error("An error occurred trying to render the default report for an item {}", item.getStringProperty("ID"), e);
            } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException ex) {
                log.error("An error occurred trying to render the default report for an item, then another error trying to get the item's ID", e);
            }
        }
    }
}

