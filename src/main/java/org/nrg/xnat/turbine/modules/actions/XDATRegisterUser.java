/*
 * web: org.nrg.xnat.turbine.modules.actions.XDATRegisterUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.Turbine;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.security.XnatProviderManager;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Slf4j
public class XDATRegisterUser extends org.nrg.xdat.turbine.modules.actions.XDATRegisterUser {
    private static final String  GOOGLE_RECAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify";
    private static final String  USER_AGENT           = "Mozilla/5.0";
    private static final Pattern PATTERN_ACCEPT_PAR   = Pattern.compile("^.*AcceptProjectAccess/par/(?<parId>[A-z0-9-]{36})\\?hash=(?<hash>[A-z0-9]{32})");
    public static final String EXTENSIONS = "org.nrg.xdat.register.validators.extensions";

    public XDATRegisterUser() {
        super();
    }

    protected XDATRegisterUser(final String pageForRetry) {
        super(pageForRetry);
    }

    @Override
    public void doPerform(final RunData data, final Context context) throws Exception {
        SiteConfigPreferences siteConfig  = XDAT.getSiteConfigPreferences();
        boolean isProjectAccessRequest = hasPAR(data);

        if( !(this instanceof RegisterExternalLogin) ){  // Don't block ldap account creation.
            if((isProjectAccessRequest && siteConfig.getSecurityExternalUserParDisabled()) ||
                    !isProjectAccessRequest && siteConfig.getSecurityNewUserRegistrationDisabled() ){
                data.setMessage("New user registration is not allowed on " + siteConfig.getSiteId());
                data.setScreenTemplate("Error.vm");
                return;
            }
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

        if (siteConfig.getUiNewUserRequireCaptcha() && !verify(parameters.get("g-recaptcha"), siteConfig.getValue("uiNewUserCaptchaPrivate"))) {
            data.setMessage("Invalid captcha");
            data.setScreenTemplate("Error.vm");
            log.warn("Invalid captcha: {}", parameters.get("g-recaptcha"));
            return;
        }

        //allow injection of new validation implementations
        final List<RegistrationValidatorI> compliantExtensions=getValidators();
        if(!CollectionUtils.isEmpty(compliantExtensions)){
            for (RegistrationValidatorI validator : compliantExtensions) {
                if (!validator.validate(data, context)) {
                    data.setScreenTemplate("Error.vm");
                    log.warn("Validation failed: {}", validator.getClass().getName());
                    return;
                }
            }
        }
        setUserAuth(data,context);
        super.doPerform(data, context);
    }

    private void setUserAuth(final RunData data, final Context context) throws UserInitException, UserFieldMappingException {
        final XdatUserAuth userAuth = (XdatUserAuth) context.get("userAuth");
        if (userAuth != null) {
            return;
        }
        final UserI found = Users.createUser(TurbineUtils.GetDataParameterHash(data));
        XnatProviderManager xnatProviderManager = XDAT.getContextService().getBeanSafely(XnatProviderManager.class);
        if (xnatProviderManager != null) {
            Map<String, XnatAuthenticationProvider> providers = xnatProviderManager.getVisibleEnabledProviders();
            if (providers.size() == 1) {
                Map.Entry<String, XnatAuthenticationProvider> firstEntry = providers.entrySet().iterator().next();
                final XnatAuthenticationProvider provider = firstEntry.getValue();
                if (provider != null) {
                    final XdatUserAuth auth = new XdatUserAuth(found.getUsername(), provider.getAuthMethod(), provider.getProviderId());
                    auth.setXdatUsername(found.getUsername());
                    context.put("userAuth", auth);
                }
            }
        }
    }

    public interface RegistrationValidatorI {
        boolean validate(final RunData data, final Context context);
    }

    private List<RegistrationValidatorI> getValidators() throws IOException, ClassNotFoundException {
        final List<RegistrationValidatorI> compliantExtensions=new ArrayList<>();

        final List<Class<?>> classes = Reflection.getClassesForPackage(EXTENSIONS);
        if (!CollectionUtils.isEmpty(classes)) {
            for(final Class<?> clazz : classes) {
                if (RegistrationValidatorI.class.isAssignableFrom(clazz)) {
                    try {
                        compliantExtensions.add((RegistrationValidatorI) clazz.newInstance());
                    } catch (Throwable e) {
                        log.error("Validation initialization failed: {}", RegistrationValidatorI.class.getName());
                    }
                } else {
                    log.error("Reflection: {}.{} is NOT an implementation of RegistrationValidatorI", EXTENSIONS, clazz.getName());
                }
            }
        }
        return compliantExtensions;
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

    public static boolean verify(String gRecaptchaResponse, String privateKey) throws IOException {
        if (StringUtils.isBlank(gRecaptchaResponse)) {
            return false;
        }

        try (final BufferedReader in = getBufferedReader(gRecaptchaResponse, privateKey)) {
            final StringBuilder response = new StringBuilder();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            final JsonNode node    = XDAT.getSerializerService().deserializeJson(response.toString());
            final boolean  success = node.get("success").asBoolean(false);
            final double   score   = node.get("score").asDouble(0.0);
            if (success && score > 0.5) {
                log.debug("Successful reCAPTCHA response with a score of {}", score);
                return true;
            }
            log.debug("Failed reCAPTCHA response with a score of {}", score);
        } catch (Exception e) {
            log.error("An error occurred trying to validate reCAPTCHA", e);
        }
        return false;
    }

    private static BufferedReader getBufferedReader(final String gRecaptchaResponse, final String privateKey) throws IOException {
        final HttpsURLConnection con = (HttpsURLConnection) new URL(GOOGLE_RECAPTCHA_URL).openConnection();

        // add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        final String postParams = "secret=" + privateKey + "&response=" + gRecaptchaResponse;

        // Send post request
        con.setDoOutput(true);
        try (final DataOutputStream output = new DataOutputStream(con.getOutputStream())) {
            output.writeBytes(postParams);
        }

        int responseCode = con.getResponseCode();
        log.debug("Sent 'POST' request to URL: {}, got response: {}", GOOGLE_RECAPTCHA_URL, responseCode);

        return new BufferedReader(new InputStreamReader(con.getInputStream()));
    }
}
