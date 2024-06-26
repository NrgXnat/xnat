/*
 * core: org.nrg.xdat.turbine.modules.actions.EditItemAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.schema.design.SchemaElementI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tim
 */
@SuppressWarnings("unused")
public class EditItemAction extends SecureAction {

    private static final Logger logger = LoggerFactory.getLogger(EditItemAction.class);

    public void doPerform(RunData data, Context context) throws Exception {
        preserveVariables(data, context);
        try {
            ItemI o = TurbineUtils.GetItemBySearch(data);

            if (o != null) {
                TurbineUtils.SetEditItem(o, data);

                SchemaElementI se = SchemaElement.GetElement(o.getXSIType());

                String templateName = GetEditScreen(se);
                data.setScreenTemplate(templateName);


                logger.info("Routing request to '" + templateName + "'");
            } else {
                logger.error("No Item Found.");
                TurbineUtils.OutputPassedParameters(data, context, this.getClass().getName());
                data.setScreenTemplate("Index.vm");
            }
        } catch (Exception e) {
            logger.error("", e);
            TurbineUtils.OutputPassedParameters(data, context, this.getClass().getName());
            data.setMessage(e.getMessage());
            data.setScreenTemplate("Index.vm");
        }
    }

    public static String GetEditScreen(SchemaElementI se) {
        String templateName = "/screens/XDATScreen_edit_" + se.getSQLName() + ".vm";
        if (Velocity.resourceExists(templateName)) {
            templateName = "XDATScreen_edit_" + se.getSQLName() + ".vm";
        } else {
            templateName = "XDATScreen_edit.vm";
        }
        return templateName;
    }
}
