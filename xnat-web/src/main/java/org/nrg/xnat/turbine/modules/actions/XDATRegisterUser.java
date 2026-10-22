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
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
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
        // Require a valid CSRF token and a POST before any other work (the CSRF check validates the token only for
        // POST, PUT and DELETE).
        try {
            SecureAction.isCsrfTokenOk(data);
        } catch (Exception e) {
            data.setMessage("Due to a technical issue, the requested action cannot be performed.");
            data.setScreenTemplate("Login.vm");
            return;
        }
        if (!isPostRequest(data)) {
            data.setMessage("Due to a technical issue, the requested action cannot be performed.");
            data.setScreenTemplate("Login.vm");
            return;
        }

        SiteConfigPreferences siteConfig  = XDAT.getSiteConfigPreferences();
        if (registrationDisallowed(data, siteConfig)) {
            data.setMessage(registrationDisallowedMessage(data, siteConfig));
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
        final UserI found = Users.createUser(allowlistRegistrationParameters(TurbineUtils.GetDataParameterHash(data)));
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

        if (StringUtils.isNotBlank(parID) && hasValidPar(data)) {
            // Accept the PAR only when it was sent to the email being registered.
            log.debug("Got registration request for PAR {} with verification hash {}", parID, hash);
            final AcceptProjectAccess action = new AcceptProjectAccess();
            context.put("user", user);
            action.doPerform(data, context);
        } else if (getPreferences().getUserRegistration() && !getPreferences().getEmailVerification()) {
            if (!StringUtils.isEmpty(nextAction) && !nextAction.contains("XDATLoginUser") && !nextAction.equals(Turbine.getConfiguration().getString("action.login"))) {
                data.setAction(nextAction);
                // Dispatch through exec(...) so the target action's perform() runs its isAuthorized() check; a
                // direct doPerform(...) call would skip it.
                ActionLoader.getInstance().exec(data, nextAction);
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

    /**
     * Decides whether the registration-disabled switches refuse this request. Each switch has its own check:
     * <ul>
     *   <li>{@code securityExternalUserParDisabled} applies when {@link #isPendingParGuid(RunData)} finds a pending
     *       request for the guid, whatever the email;</li>
     *   <li>{@code securityNewUserRegistrationDisabled} applies unless {@link #hasValidPar(RunData)} finds a pending
     *       request sent to the email being registered.</li>
     * </ul>
     * External-login (LDAP/OpenID) account creation is never blocked here.
     *
     * @param data       The run data for the current request.
     * @param siteConfig The site configuration preferences.
     *
     * @return {@code true} if registration must be refused.
     */
    boolean registrationDisallowed(final RunData data, final SiteConfigPreferences siteConfig) {
        if (this instanceof RegisterExternalLogin) {
            return false; // Don't block ldap account creation.
        }
        return (isPendingParGuid(data) && siteConfig.getSecurityExternalUserParDisabled())
               || (!hasValidPar(data) && siteConfig.getSecurityNewUserRegistrationDisabled());
    }

    /**
     * The message explaining why {@link #registrationDisallowed(RunData, SiteConfigPreferences)} refused the request.
     * When a pending invitation exists for the submitted guid but was sent to a different email, and par registration
     * is allowed, registering with the invited email would succeed, so the message says so. Any other refusal shows
     * the generic site message.
     *
     * @param data       The run data for the current request.
     * @param siteConfig The site configuration preferences.
     *
     * @return The message to display.
     */
    String registrationDisallowedMessage(final RunData data, final SiteConfigPreferences siteConfig) {
        if (!siteConfig.getSecurityExternalUserParDisabled() && isPendingParGuid(data) && !hasValidPar(data)) {
            return "This project invitation could not be applied, because it was sent to a different email address. "
                   + "To accept it, register using the email address the invitation was sent to, or contact the project owner.";
        }
        return "New user registration is not allowed on " + siteConfig.getSiteId();
    }

    /**
     * Indicates whether the submitted {@code par} guid identifies a pending project access request, whatever the email
     * being registered. It is used only for the external-user-par-disabled switch (see
     * {@link #registrationDisallowed(RunData, SiteConfigPreferences)}). The new-user-registration-disabled switch, and
     * whether the PAR enables, verifies or grants the account, use {@link #hasValidPar(RunData)}, which also requires
     * the email to match.
     *
     * @param data The run data for the current request.
     *
     * @return {@code true} if a pending request matches the submitted guid.
     */
    boolean isPendingParGuid(final RunData data) {
        return pendingParForSubmittedGuid(data) != null;
    }

    /**
     * Treats a registration as backed by a project access request only when a pending PAR exists for the submitted guid
     * and was sent to the email being registered.
     *
     * @param data The run data for the current request.
     *
     * @return {@code true} only if a pending PAR matches the submitted guid and the registering email.
     */
    @Override
    protected boolean hasValidPar(final RunData data) {
        final ProjectAccessRequest request = pendingParForSubmittedGuid(data);
        if (request == null) {
            return false;
        }
        // The submitted email is XML-escaped when read, so un-escape it, then compare it to the invited email
        // ignoring case.
        final String submitted = StringUtils.trim(TurbineUtils.unescapeParam((String) TurbineUtils.GetPassedParameter(XDAT_USER_EMAIL, data)));
        return StringUtils.isNotBlank(request.getEmail()) && StringUtils.isNotBlank(submitted)
               && StringUtils.equalsIgnoreCase(StringUtils.trim(request.getEmail()), submitted);
    }

    /**
     * Returns the pending project access request identified by the submitted {@code par} guid (from the request
     * parameter or the session), or {@code null}. Only a request that is still pending (not yet approved) is
     * returned.
     *
     * @param data The run data for the current request.
     *
     * @return The pending project access request, or {@code null}.
     */
    private ProjectAccessRequest pendingParForSubmittedGuid(final RunData data) {
        final String guid = parIdentifier(data);
        if (StringUtils.isBlank(guid)) {
            return null;
        }
        final ProjectAccessRequest request;
        try {
            request = ProjectAccessRequest.RequestPARByGUID(guid, Users.getGuest());
        } catch (Exception e) {
            return null;
        }
        // Pending means not yet acted on: neither approved nor rejected (both set approval_date / approved).
        return request != null && request.getApprovalDate() == null && request.getApproved() == null ? request : null;
    }

    private static String parIdentifier(final RunData data) {
        final String fromParameter = (String) TurbineUtils.GetPassedParameter(XDAT_USER_PAR, data);
        if (StringUtils.isNotBlank(fromParameter)) {
            return fromParameter;
        }
        final Object fromSession = data.getSession().getAttribute(XDAT_USER_PAR);
        return fromSession == null ? null : fromSession.toString();
    }
}
