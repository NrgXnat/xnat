/*
 * web: org.nrg.xnat.turbine.modules.screens.Search
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;

@Slf4j
public class Search extends SecureScreen {
    @Override
    protected void doBuildTemplate(RunData data, Context context) {
        final String node   = (String) TurbineUtils.GetPassedParameter("node", data);
        final String search = (String) TurbineUtils.GetPassedParameter("new_search", data);
        log.debug("Now in Search.doBuildTemplate(), got values node=\"{}\", new_search=\"{}\"",
                  StringUtils.defaultIfBlank(node, "(no value)"),
                  StringUtils.defaultIfBlank(search, "(no value)"));

        if (StringUtils.isNotBlank(node)) {
            context.put("node", node);
        }
        if (StringUtils.isNotBlank(search)) {
            context.put("newSearch", "true");
        }
    }
}
