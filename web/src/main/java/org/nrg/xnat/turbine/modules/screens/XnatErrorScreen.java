/*
 * web: org.nrg.xnat.turbine.modules.screens.XnatErrorScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.modules.screens.VelocityErrorScreen;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("unused")
public class XnatErrorScreen extends VelocityErrorScreen {

    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        super.doBuildTemplate(data, context);

        final HttpServletRequest request = data.getRequest();
        final Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");

        final Integer status = (Integer) request.getAttribute("javax.servlet.error.status_code");
        final String uri = (String) request.getAttribute("javax.servlet.error.request_uri");
        final String message = throwable == null ? "Unknown error occurred" : throwable.getMessage();

        if (_log.isDebugEnabled()) {
            _log.debug("Reporting a {} error requesting URI '{}': {}", status, uri, message);
        }
        context.put("status", status != null ? status : "Unknown status");
        context.put("uri", StringUtils.isNotBlank(uri) ? uri : "Unknown URI");
        context.put("message", message);
        SecureScreen.loadAdditionalVariables(data, context);
    }

    private static final Logger _log = LoggerFactory.getLogger(XnatErrorScreen.class);
}
