/*
 * core: org.nrg.xdat.turbine.modules.actions.ModifyAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.Turbine;
import org.apache.turbine.services.pull.tools.TemplateLink;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public abstract class ModifyAction extends SecureAction {
    @Nonnull
    abstract protected String getDefaultEditScreen();

    protected void setDataAndContext(final RunData data, final Context context) {
        _context = context;
        _data = data;
    }


    protected void redirect(final boolean success, final String message) {
        if (success) {
            final Boolean expired          = (Boolean) _data.getSession().getAttribute("expired");
            final Boolean forgot           = (Boolean) _data.getSession().getAttribute("forgot");
            final String  loginTemplate    = Turbine.getConfiguration().getString("template.login");
            final String  homepageTemplate = Turbine.getConfiguration().getString("template.homepage");

            if (forgot != null && forgot) {
                //User forgot their password. They must log in again.
                if (StringUtils.isNotEmpty(loginTemplate)) {
                    // We're running in a templating solution
                    _data.setScreenTemplate(loginTemplate);
                } else {
                    _data.setScreen(Turbine.getConfiguration().getString("screen.login"));
                }
            } else if (expired != null && expired) {
                //They just updated expired password.
                _data.getSession().setAttribute("expired", Boolean.FALSE);//New password is not expired
                if (StringUtils.isNotEmpty(homepageTemplate)) {
                    // We're running in a templating solution
                    _data.setScreenTemplate(homepageTemplate);
                } else {
                    _data.setScreen(Turbine.getConfiguration().getString("screen.homepage"));
                }
            } else {
                setMessage(true, message);
            }
        } else {
            setMessage(false, message);
        }
    }

    private void setMessage(final boolean success, final String message) {
        _data.setRedirectURI(((TemplateLink) _context.get("link")).setPage(getEditScreen()).addQueryData("success", success).addQueryData("message", message).getURI());
    }

    @Nonnull
    private String getEditScreen() {
        final String screen = StringUtils.defaultIfBlank((String) TurbineUtils.GetPassedParameter("edit_screen", _data), getDefaultEditScreen());
        _log.info("Found edit screen value {}", screen);
        return screen;
    }

    private static final Logger _log = LoggerFactory.getLogger(ModifyAction.class);

    private RunData _data;
    private Context _context;
}
