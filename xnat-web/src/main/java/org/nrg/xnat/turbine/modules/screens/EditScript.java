/*
 * org.nrg.xnat.turbine.modules.screens.Configuration
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.commons.lang.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.automation.entities.Script;
import org.nrg.automation.services.ScriptService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.modules.screens.AdminScreen;

public class EditScript extends AdminScreen {

    public EditScript() {
        _service = XDAT.getContextService().getBean(ScriptService.class);
    }

    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        log.debug("Found some stuff.");
        final String scriptId = data.getParameters().get("scriptId");
        if (!StringUtils.isEmpty(scriptId)) {
            final Script script = _service.getByScriptId(scriptId);
            if (script == null) {
                context.put("error", "The script indicated by " + scriptId + " could not be found.");
            } else {
                context.put("script", script);
            }
        } else {
            context.put("script", _service.newEntity());
        }
    }

    private ScriptService _service;
}
