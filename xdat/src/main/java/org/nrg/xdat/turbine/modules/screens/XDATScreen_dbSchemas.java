/*
 * xdat: org.nrg.xdat.turbine.modules.screens.XDATScreen_dbSchemas
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2025, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xft.schema.db.entities.DBBackedSchema;
import org.nrg.xft.schema.db.services.DBBackedSchemaService;

import java.util.List;

/**
 * Screen for listing database-backed schemas
 */
public class XDATScreen_dbSchemas extends AdminScreen {

    @Override
    public void doBuildTemplate(RunData data, Context context) {
        try {
            DBBackedSchemaService dbBackedSchemaService = XDAT.getContextService().getBean(DBBackedSchemaService.class);
            List<DBBackedSchema> schemas = dbBackedSchemaService.findAllSchema();

            context.put("dbschemas", schemas);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}