/*
 * core: org.nrg.xdat.turbine.modules.actions.DisplayItemAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.turbine.util.RunData;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.navigation.DefaultReportIdentifier;
import org.nrg.xdat.navigation.DefaultReportIdentifierI;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.design.SchemaElementI;

/**
 * @author Tim
 */
@Slf4j
public class DisplayItemAction extends SecureAction {
    public void doPerform(final RunData data, final Context context) throws Exception {
        preserveVariables(data, context);
        final String defaultReportIdentifierClass = XDAT.getSiteConfigurationProperty("UI.defaultReportIdentifier", "org.nrg.xdat.navigation.DefaultReportIdentifier");

        try {
            final Object object = Class.forName(defaultReportIdentifierClass).newInstance();
            if (object instanceof DefaultReportIdentifierI) {
                data.setScreenTemplate(((DefaultReportIdentifier) object).identifyReport(data, context));
            }
        } catch (Throwable e) {
            log.error("An error occurred trying to retrieve the configured default report identifier class {}", defaultReportIdentifierClass, e);
            TurbineUtils.OutputPassedParameters(data, context, this.getClass().getName());
            data.setMessage(e.getMessage());
            data.setScreenTemplate("Error.vm");
        }
    }

    public static String GetReportScreen(final SchemaElementI se) {
        final String templateName = "XDATScreen_report_" + se.getSQLName() + ".vm";
        return Velocity.resourceExists("/screens/" + templateName) ? templateName : "DefaultReport.vm";
    }

    public static String GetReportScreen(final String elementName) throws XFTInitException, ElementNotFoundException {
        return GetReportScreen(SchemaElement.GetElement(elementName));
    }
}

