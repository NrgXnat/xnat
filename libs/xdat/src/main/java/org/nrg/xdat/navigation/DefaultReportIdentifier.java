/*
 * core: org.nrg.xdat.navigation.DefaultReportIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.navigation;

import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.actions.DisplayItemAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.schema.design.SchemaElementI;

public class DefaultReportIdentifier implements DefaultReportIdentifierI {
    public DefaultReportIdentifier() {

    }

    public String identifyReport(RunData data, Context context) throws Exception {
        final SchemaElementI schemaElement = TurbineUtils.GetSchemaElementBySearch(data);
        if (schemaElement == null) {
            final String elementName = TurbineUtils.escapeParam(data.getParameters().getString("search_element"));
            if (StringUtils.isNotBlank(elementName)) {
                throw new ElementNotFoundException(elementName);
            }
            throw new ElementNotFoundException("No element found.");
        }
        return DisplayItemAction.GetReportScreen(schemaElement);
    }
}
