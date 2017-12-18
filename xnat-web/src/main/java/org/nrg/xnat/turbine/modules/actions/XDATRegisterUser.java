/*
 * web: org.nrg.xnat.turbine.modules.actions.XDATRegisterUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.Turbine;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
@Slf4j
public class XDATRegisterUser extends org.nrg.xdat.turbine.modules.actions.XDATRegisterUser {
    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        Map<String, String> parameters = TurbineUtils.GetDataParameterHash(data);
        if (parameters.containsKey("xdat:user.email")) {
            final String                    email = parameters.get("xdat:user.email");
            ArrayList<ProjectAccessRequest> pars  = ProjectAccessRequest.RequestPARsByUserEmail(email, null);
            if (pars != null && pars.size() > 0) {
                List<String> projectIds = new ArrayList<>();
                for (ProjectAccessRequest par : pars) {
                    projectIds.add(par.getProjectId());
                }
                context.put("pars", projectIds);
            }
        }
        super.doPerform(data, context);
    }

    @Override
    public void directRequest(RunData data, Context context, UserI user) throws Exception {
        String nextPage   = (String) TurbineUtils.GetPassedParameter("nextPage", data);
        String nextAction = (String) TurbineUtils.GetPassedParameter("nextAction", data);

        data.setScreenTemplate("Index.vm");

        String parID = (String) TurbineUtils.GetPassedParameter("par", data);
        String hash  = (String) TurbineUtils.GetPassedParameter("hash", data);
        if (StringUtils.isEmpty(parID)) {
            if (data.getSession().getAttribute("par") != null) {
                parID = (String) data.getSession().getAttribute("par");
                hash = (String) data.getSession().getAttribute("hash");
                data.getParameters().add("par", parID);
                data.getParameters().add("hash", hash);
                data.getSession().removeAttribute("par");
                data.getSession().removeAttribute("hash");
            } else {

                final SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(data.getRequest(), data.getResponse());
                if (savedRequest != null) {
                    final String cachedRequest = savedRequest.getRedirectUrl();
                    if (!StringUtils.isBlank(cachedRequest)) {
                        Matcher matcher = PATTERN_ACCEPT_PAR.matcher(cachedRequest);
                        if (matcher.find()) {
                            parID = matcher.group(1);
                            hash = matcher.group(2);
                            data.getParameters().add("par", parID);
                            data.getParameters().add("hash", hash);
                        }
                    }
                }
            }
        }

        if (parID != null) {
            log.debug("Got registration request for PAR {} with verification hash {}", parID, hash);
            final AcceptProjectAccess action = new AcceptProjectAccess();
            context.put("user", user);
            action.doPerform(data, context);
        } else {
            final SiteConfigPreferences preferences = XDAT.getSiteConfigPreferences();
            if (!StringUtils.isEmpty(nextAction) && !nextAction.contains("XDATLoginUser") && !nextAction.equals(Turbine.getConfiguration().getString("action.login"))) {
                if (preferences.getUserRegistration() & !preferences.getEmailVerification()) {
                    data.setAction(nextAction);
                    ((VelocityAction) ActionLoader.getInstance().getInstance(nextAction)).doPerform(data, context);
                }
            } else if (!StringUtils.isBlank(nextPage) &&
                       !StringUtils.equals(nextPage, Turbine.getConfiguration().getString("template.home")) &&
                       preferences.getUserRegistration() &&
                       !preferences.getEmailVerification()) {
                data.setScreenTemplate(nextPage);
            }
        }
    }

    private static final Pattern PATTERN_ACCEPT_PAR = Pattern.compile("^.*AcceptProjectAccess/par/([A-z0-9-]{36})\\?hash=([A-z0-9]{32})");
}
