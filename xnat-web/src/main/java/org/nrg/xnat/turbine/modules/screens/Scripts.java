/*
 * web: org.nrg.xnat.turbine.modules.screens.Scripts
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.automation.entities.Script;
import org.nrg.automation.services.ScriptService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.modules.screens.AdminScreen;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Scripts extends AdminScreen {

    public Scripts() {
        _service = XDAT.getContextService().getBean(ScriptService.class);
    }

    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        List<Script> scripts = _service.getAll();
        Collections.sort(scripts, new ScriptSorter());
        context.put("scripts", scripts);
    }

    class ScriptSorter implements Comparator<Script> {
        @Override
        public int compare(final Script script1, final Script script2) {
            return script1.getScriptId().compareToIgnoreCase(script2.getScriptId());
        }
    }

    private ScriptService _service;
}
