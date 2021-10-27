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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Slf4j
public class XDATRegisterUser extends org.nrg.xdat.turbine.modules.actions.XDATRegisterUser {
    public XDATRegisterUser() {
        super();
    }

    protected XDATRegisterUser(final String pageForRetry) {
        super(pageForRetry);
    }

    @Override
    public void doPerform(final RunData data, final Context context) throws Exception {
        SiteConfigPreferences siteConfig  = XDAT.getSiteConfigPreferences();
        if(siteConfig.getSecurityNewUserRegistrationDisabled()){
            data.setMessage("New user registration is not allowed on " + siteConfig.getSiteId());
            data.setScreenTemplate("Error.vm");
            return;
        }

        if (!validate(data, context)) {
            return;
        }

        final Map<String, String> parameters = TurbineUtils.GetDataParameterHash(data);
        final String email = parameters.get(XDAT_USER_EMAIL);
        final List<String> projectIds = ProjectAccessRequest.RequestPARsByUserEmail(email, null).stream().map(ProjectAccessRequest::getProjectId).collect(Collectors.toList());
        if (!projectIds.isEmpty()) {
            context.put("pars", projectIds);
        }
        super.doPerform(data, context);
    }

    @Override
    public void directRequest(final RunData data, final Context context, final UserI user) throws Exception {
        final String nextPage   = (String) TurbineUtils.GetPassedParameter("nextPage", data);
        final String nextAction = (String) TurbineUtils.GetPassedParameter("nextAction", data);

        data.setScreenTemplate("Index.vm");

        String parID = (String) TurbineUtils.GetPassedParameter("par", data);
        String hash  = (String) TurbineUtils.GetPassedParameter("hash", data);
        if (StringUtils.isEmpty(parID)) {
            parID = (String) data.getSession().getAttribute("par");
            if (StringUtils.isNotBlank(parID)) {
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
                        final Matcher matcher = PATTERN_ACCEPT_PAR.matcher(cachedRequest);
                        if (matcher.find()) {
                            parID = matcher.group("parId");
                            hash = matcher.group("hash");
                            data.getParameters().add("par", parID);
                            data.getParameters().add("hash", hash);
                        }
                    }
                }
            }
        }

        if (StringUtils.isNotBlank(parID)) {
            log.debug("Got registration request for PAR {} with verification hash {}", parID, hash);
            final AcceptProjectAccess action = new AcceptProjectAccess();
            context.put("user", user);
            action.doPerform(data, context);
        } else if (getPreferences().getUserRegistration() && !getPreferences().getEmailVerification()) {
            if (!StringUtils.isEmpty(nextAction) && !nextAction.contains("XDATLoginUser") && !nextAction.equals(Turbine.getConfiguration().getString("action.login"))) {
                data.setAction(nextAction);
                ((VelocityAction) ActionLoader.getInstance().getInstance(nextAction)).doPerform(data, context);
            } else if (!StringUtils.isBlank(nextPage) && !StringUtils.equals(nextPage, Turbine.getConfiguration().getString("template.home"))) {
                data.setScreenTemplate(nextPage);
            }
        }
    }

    private static final Pattern PATTERN_ACCEPT_PAR = Pattern.compile("^.*AcceptProjectAccess/par/(?<parId>[A-z0-9-]{36})\\?hash=(?<hash>[A-z0-9]{32})");
}
