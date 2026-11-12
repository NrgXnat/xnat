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
import org.apache.commons.text.StringEscapeUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.hibernate.exception.ConstraintViolationException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.exceptions.UsernameAuthMappingNotFoundException;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.security.XnatProviderManager;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.nrg.xnat.security.provider.XnatDatabaseAuthenticationProvider;
import org.nrg.xnat.security.tokens.XnatDatabaseUsernamePasswordAuthenticationToken;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
@Slf4j
public class RegisterExternalLogin extends XDATRegisterUser {
    public RegisterExternalLogin() {
        super("RegisterExternalLogin.vm");
        _service = XDAT.getContextService().getBean(XdatUserAuthService.class);
        _provider = XDAT.getContextService().getBean(XnatDatabaseAuthenticationProvider.class);
        _manager = XDAT.getContextService().getBean(XnatProviderManager.class);
    }

    @Override
    public void doPerform(final RunData data, final Context context) throws Exception {
        // Both operations finish an external login from earlier in this session. XnatAuthenticationFilter stores a
        // UsernameAuthMappingNotFoundException on the session when an external login doesn't map to an XNAT account;
        // without it there is nothing to finish, as on the RegisterExternalLogin screen. The external identity comes
        // from that session attribute, not from the request.
        final UsernameAuthMappingNotFoundException externalAuth = getVerifiedExternalAuth(data.getRequest());
        if (externalAuth == null) {
            log.warn("RegisterExternalLogin request without an external login in the session (remote address {})", data.getRequest().getRemoteAddr());
            TurbineUtils.redirectToLogin(data);
            return;
        }

        // The merge operation doesn't reach the base workflow's CSRF check, so check here for both operations.
        try {
            SecureAction.isCsrfTokenOk(data);
        } catch (Exception e) {
            data.setMessage("Due to a technical issue, the requested action cannot be performed.");
            data.setScreenTemplate("Login.vm");
            return;
        }

        // Both operations must be a POST: the CSRF check validates the token only for POST, PUT and DELETE.
        if (!isPostRequest(data)) {
            TurbineUtils.redirectToLogin(data);
            return;
        }

        final String operation = (String) TurbineUtils.GetPassedParameter("operation", data);

        if (StringUtils.equals("merge", operation)) {
            final String username = (String) TurbineUtils.GetPassedParameter("username", data);
            final String password = (String) TurbineUtils.GetPassedParameter("password", data);
            try {
                validateXnatLogin(username, password);
                createUserAuthRecord(data, operation, externalAuth);
                final UserI user = Users.getUser(username);
                XDAT.loginUser(data, user, false);
                TurbineUtils.setBannerMessage(data, "Your login <b>" + StringEscapeUtils.escapeHtml4(externalAuth.getUsername()) + "</b> is now connected to your <b>" + StringEscapeUtils.escapeHtml4(username) + "</b> account on this XNAT server.");
                data.setScreenTemplate("Index.vm");
                // The session's external login has been used, so remove it.
                clearVerifiedExternalAuth(data.getRequest());
            } catch (BadCredentialsException e) {
                log.info("User logged in with username {} through auth method {}, then tried to associate with existing XNAT account '{}' but provided invalid credentials", externalAuth.getUsername(), externalAuth.getAuthMethodId(), username);
                retryAuthentication(data, context, "The submitted username and password didn't match an existing active XNAT account.");
            } catch (AuthenticationCredentialsNotFoundException e) {
                retryAuthentication(data, context, "You must specify a value for both username and password.");
            }
        } else {
            try {
                // Run the new user through the standard registration workflow. The authentication method and ID come
                // from the session's external login. The provider's auto-enable and auto-verify settings go to the
                // base workflow in the context.
                final String  login         = (String) TurbineUtils.GetPassedParameter("xdat:user.login", data);
                final boolean existedBefore = userExists(login);

                final String authMethod = externalAuth.getAuthMethod();
                final String providerId = externalAuth.getAuthMethodId();

                final XnatAuthenticationProvider provider = _manager.getProvider(authMethod, providerId);
                if (provider != null) {
                    context.put("providerAutoEnabled", provider.isAutoEnabled());
                    // Auto-verify only when the submitted email matches the external login's email; otherwise the
                    // email is verified normally. The submitted email is XML-escaped when read, so un-escape it to
                    // match addresses containing characters such as ' or &.
                    final String submittedEmail = TurbineUtils.unescapeParam((String) TurbineUtils.GetPassedParameter("xdat:user.email", data));
                    context.put("providerAutoVerified", provider.isAutoVerified() && submittedEmailMatchesProof(submittedEmail, externalAuth));
                }

                context.put("userAuth", createUserAuthRecord(data, operation, externalAuth));
                runRegistrationWorkflow(data, context);

                // Once an account has been created, the session's external login has been used, so remove it.
                if (!existedBefore && userExists(login)) {
                    clearVerifiedExternalAuth(data.getRequest());
                }
            } catch (Exception e) {
                handleInvalid(data, context, "An error occurred trying to register your user account. Please try again or contact the system administrator if you need more help.");
            }
        }
    }

    /**
     * Returns the external login that {@link org.nrg.xnat.security.XnatAuthenticationFilter} stored on the current
     * session, or {@code null} if there is none.
     *
     * @param request The current request.
     *
     * @return The session's external login, or {@code null} if the session carries none.
     */
    static UsernameAuthMappingNotFoundException getVerifiedExternalAuth(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        final Object attribute = session.getAttribute(UsernameAuthMappingNotFoundException.class.getSimpleName());
        return attribute instanceof UsernameAuthMappingNotFoundException ? (UsernameAuthMappingNotFoundException) attribute : null;
    }

    /**
     * Removes the external login from the session once a merge or registration has used it.
     *
     * @param request The current request.
     */
    static void clearVerifiedExternalAuth(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(UsernameAuthMappingNotFoundException.class.getSimpleName());
        }
    }

    /**
     * Indicates whether the email submitted on the registration form matches the external login's email. Only then
     * does the provider's auto-verify apply.
     *
     * @param submittedEmail The email submitted on the registration form.
     * @param externalAuth   The session's external login.
     *
     * @return {@code true} only if both emails are present and equal (ignoring case and surrounding whitespace).
     */
    static boolean submittedEmailMatchesProof(final String submittedEmail, final UsernameAuthMappingNotFoundException externalAuth) {
        final String proofEmail = externalAuth.getEmail();
        return StringUtils.isNotBlank(proofEmail) && StringUtils.isNotBlank(submittedEmail)
               && StringUtils.equalsIgnoreCase(StringUtils.trim(submittedEmail), StringUtils.trim(proofEmail));
    }

    private boolean userExists(final String login) {
        if (StringUtils.isBlank(login)) {
            return false;
        }
        try {
            return Users.getUser(login) != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Runs the standard registration workflow inherited from {@link XDATRegisterUser}. Kept as a separate seam so that
     * the decisions this action makes around it can be tested without running the whole workflow.
     *
     * @param data    The run data for the current request.
     * @param context The Velocity context for the current request.
     *
     * @throws Exception When the registration workflow fails.
     */
    void runRegistrationWorkflow(final RunData data, final Context context) throws Exception {
        super.doPerform(data, context);
    }

    @Override
    public void directRequest(final RunData data, final Context context, final UserI user) throws Exception {
        super.directRequest(data, context, user);
    }

    @Override
    public void handleInvalid(final RunData data, final Context context, final String message) {
        super.handleInvalid(data, context, StringUtils.startsWith(message, "Registration error") ? message : "Registration error: " + message);
    }

    private void validateXnatLogin(final String username, final String password) {
        // If it's a merge with an existing account, we need to validate the username and password.
        if (StringUtils.isBlank(username)) {
            throw new AuthenticationCredentialsNotFoundException("You must provide a valid username.");
        }
        if (StringUtils.equalsIgnoreCase("guest", username)) {
            throw new AuthenticationCredentialsNotFoundException("You can't associate an external authentication account with the guest user.");
        }
        if (StringUtils.isBlank(password)) {
            throw new AuthenticationCredentialsNotFoundException("You must provide a password value.");
        }

        try {
            final Authentication authentication = _provider.authenticate(new XnatDatabaseUsernamePasswordAuthenticationToken(username, password));
            if (authentication.isAuthenticated()) {
                return;
            }
        } catch (AuthenticationException ignored) {
            // We'll throw an exception below whether auth operation returned false or threw auth exception.
        }
        throw new BadCredentialsException("The submitted username and password didn't match an existing active XNAT account.");
    }

    private void retryAuthentication(final RunData data, final Context context, final String message) {
        preserveVariables(data, context);
        data.setRedirectURI(null);
        data.setMessage("Authentication failed: " + message);
        data.setScreenTemplate("RegisterExternalLogin.vm");
    }

    private XdatUserAuth createUserAuthRecord(final RunData data, final String operation, final UsernameAuthMappingNotFoundException externalAuth) {
        final boolean isMerge = StringUtils.equals("merge", operation);

        // The external identity being linked comes from the session's external login.
        final String authUsername = externalAuth.getUsername();
        final String authMethod   = externalAuth.getAuthMethod();
        final String authMethodId = externalAuth.getAuthMethodId();

        final XdatUserAuth auth = new XdatUserAuth(authUsername, authMethod, authMethodId);
        auth.setXdatUsername((String) TurbineUtils.GetPassedParameter(isMerge ? "username" : "xdat:user.login", data));

        // Only save if this is a merge. Otherwise the user auth record will be saved when the UserI record is saved.
        if (!isMerge) {
            return auth;
        }
        try {
            return _service.create(auth);
        } catch (ConstraintViolationException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof PSQLException exception) {
                final ServerErrorMessage message = exception.getServerErrorMessage();
                if (message != null && PATTERN_DUPLICATE_KEY.matcher(message.toString()).matches() && StringUtils.isBlank(_service.getXdatUsernameByAuthNameAndProvider(authUsername, authMethod, authMethodId))) {
                    log.info("Found duplicate user auth record for username '{}' and auth method '{}' ('{}'), but no corresponding XNAT user. Allowing account creation to proceed.", authUsername, authMethodId, authMethod);
                    return _service.getUserByNameAndAuth(authUsername, authMethod, authMethodId);
                }
            }
            throw e;
        }
    }

    private static final Pattern PATTERN_DUPLICATE_KEY = Pattern.compile("^.*duplicate key value violates unique constraint.*Key \\(auth_user, auth_method_id\\)=.*already exists\\.$", Pattern.DOTALL);

    private final XdatUserAuthService                _service;
    private final XnatDatabaseAuthenticationProvider _provider;
    private final XnatProviderManager                _manager;
}
