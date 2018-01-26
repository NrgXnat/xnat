/*
 * web: org.nrg.xnat.security.XnatProviderManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.velocity.VelocityContext;
import org.hibernate.exception.DataException;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.security.alias.AliasTokenAuthenticationProvider;
import org.nrg.xnat.security.alias.AliasTokenAuthenticationToken;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.nrg.xnat.security.provider.XnatDatabaseAuthenticationProvider;
import org.nrg.xnat.security.provider.XnatLdapAuthenticationProvider;
import org.nrg.xnat.security.tokens.XnatDatabaseUsernamePasswordAuthenticationToken;
import org.nrg.xnat.security.tokens.XnatLdapUsernamePasswordAuthenticationToken;
import org.springframework.context.MessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.nrg.framework.orm.DatabaseHelper.convertPGIntervalToIntSeconds;

@Slf4j
public class XnatProviderManager extends ProviderManager {
    public XnatProviderManager(final SiteConfigPreferences preferences, final List<AuthenticationProvider> providers, final XdatUserAuthService userAuthService, final AnonymousAuthenticationProvider anonymousAuthenticationProvider, final DataSource dataSource, final MessageSource messageSource) {
        super(providers);
        _userAuthService = userAuthService;
        _anonymousAuthenticationProvider = anonymousAuthenticationProvider;
        _dataSource = dataSource;
        _eventPublisher = new AuthenticationAttemptEventPublisher(this, userAuthService, preferences);
        _messageSource = messageSource;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Class<? extends Authentication> toTest        = authentication.getClass();
        AuthenticationException         lastException = null;
        Authentication                  result        = null;
        List<AuthenticationProvider>    providers     = new ArrayList<>();

        // HACK: This is a hack to work around open XNAT auth issue. If this is a bare un/pw auth token, use anon auth.
        if (authentication.getClass() == UsernamePasswordAuthenticationToken.class && authentication.getName().equalsIgnoreCase("guest")) {
            providers.add(_anonymousAuthenticationProvider);
            authentication = new AnonymousAuthenticationToken(_anonymousAuthenticationProvider.getKey(), authentication.getPrincipal(), Collections.<GrantedAuthority>singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        } else {
            for (AuthenticationProvider candidate : getProviders()) {
                if (!candidate.supports(toTest)) {
                    continue;
                }
                if (authentication instanceof XnatLdapUsernamePasswordAuthenticationToken) {
                    if (!(candidate instanceof XnatLdapAuthenticationProvider)) {
                        continue;
                    }
                    XnatLdapAuthenticationProvider ldapCandidate = (XnatLdapAuthenticationProvider) candidate;
                    if (!((XnatLdapUsernamePasswordAuthenticationToken) authentication).getProviderId().equalsIgnoreCase(ldapCandidate.getProviderId())) {
                        //This is a different LDAP provider than the one that was selected.
                        continue;
                    }
                }
                if (candidate instanceof XnatDatabaseAuthenticationProvider && ((XnatDatabaseAuthenticationProvider) candidate).isPlainText()) {
                    String username = authentication.getPrincipal().toString();
                    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"}) final Boolean encrypted = new JdbcTemplate(_dataSource).query("SELECT primary_password_encrypt<>0 OR (primary_password_encrypt IS NULL AND CHAR_LENGTH(primary_password)=64) FROM xdat_user WHERE login=? LIMIT 1", new String[]{username}, new RowMapper<Boolean>() {
                        public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return rs.getBoolean(1);
                        }
                    }).get(0);

                    if (encrypted) {
                        continue;
                    }
                }
                providers.add(candidate);
            }
        }

        assert providers.size() > 0 : "No provider found for authentication of type " + authentication.getClass().getSimpleName();

        for (AuthenticationProvider provider : providers) {
            log.debug("Authentication attempt using " + provider.getClass().getName());

            try {
                result = provider.authenticate(authentication);
                if (result != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found a provider that worked for " + authentication.getName() + ": " + provider.getClass().getSimpleName());
                    }

                    copyDetails(authentication, result);
                    break;
                }
            } catch (AccountStatusException exception) {
                log.warn("Error occurred authenticating login request", exception);
                lastException = exception;
            } catch (NewLdapAccountNotAutoEnabledException e) {
                try {
                    AdminUtils.sendNewUserNotification(e.getUserDetails(), "", "", "", new VelocityContext());
                } catch (Exception exception) {
                    log.error("Error occurred sending new user request email", exception);
                }
                lastException = e;

            } catch (AuthenticationException e) {
                lastException = e;
            }
        }

        if (result != null) {
            _eventPublisher.publishAuthenticationSuccess(authentication);
            return result;
        } else {
            // Parent was null, or didn't authenticate (or throw an exception).
            if (lastException == null) {
                final String message = _messageSource.getMessage("providerManager.providerNotFound", new Object[]{toTest.getName()}, "No authentication provider found for {0}", Locale.getDefault());
                lastException = new ProviderNotFoundException(message);
            }

            _eventPublisher.publishAuthenticationFailure(lastException, authentication);
            throw lastException;
        }
    }

    public XdatUserAuth getUserByAuth(final Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        final String username;
        final Object principal = authentication.getPrincipal();
        if (principal == null) {
            username = authentication.getName();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            username = ((UserI) principal).getLogin();
        }

        if (StringUtils.isBlank(username)) {
            throw new RuntimeException("An error occurred trying to get user from authentication: no principal or user name was found.");
        }

        final String method;
        final String provider;
        if (authentication instanceof XnatLdapUsernamePasswordAuthenticationToken) {
            provider = ((XnatLdapUsernamePasswordAuthenticationToken) authentication).getProviderId();
            method = XdatUserAuthService.LDAP;
        } else {
            provider = "";
            method = XdatUserAuthService.LOCALDB;
        }

        try {
            return _userAuthService.getUserByNameAndAuth(username, method, provider);
        } catch (DataException exception) {
            throw new RuntimeException("An error occurred trying to validate the given information. Please check your username and password. If this problem persists, please contact your system administrator.");
        }
    }

    public UsernamePasswordAuthenticationToken buildUPTokenForAuthMethod(String authMethod, String username, String password) {
        XnatAuthenticationProvider chosenProvider = findAuthenticationProviderByAuthMethod(authMethod);
        return buildUPToken(chosenProvider, username, password);
    }

    public UsernamePasswordAuthenticationToken buildUPTokenForProviderName(String providerName, String username, String password) {
        XnatAuthenticationProvider chosenProvider = findAuthenticationProviderByProviderName(providerName);
        return buildUPToken(chosenProvider, username, password);
    }

    public String retrieveAuthMethod(final String username) {
        if (_cachedAuthMethods.containsKey(username)) {
            return _cachedAuthMethods.get(username);
        } else {
            final String authMethod;
            try {
                final List<XdatUserAuth> userAuthMethods = _userAuthService.getUsersByName(username);
                if (userAuthMethods.size() == 1) {
                    authMethod = userAuthMethods.get(0).getAuthMethod();
                    // The list may contain localdb auth method even when password is empty and LDAP authentication is used (MRH)
                } else if (userAuthMethods.size() > 1) {
                    String methodCandidate = null;
                    for (UserAuthI userAuth : userAuthMethods) {
                        methodCandidate = userAuth.getAuthMethod();
                        if (!methodCandidate.equalsIgnoreCase(XdatUserAuthService.LOCALDB)) {
                            break;
                        }
                    }
                    authMethod = StringUtils.defaultIfBlank(methodCandidate, XdatUserAuthService.LOCALDB);
                } else if (AliasToken.isAliasFormat(username)) {
                    authMethod = XdatUserAuthService.TOKEN;
                } else {
                    authMethod = XdatUserAuthService.LOCALDB;
                }
            } catch (DataException exception) {
                log.error("An error occurred trying to retrieve the auth method", exception);
                throw new RuntimeException("An error occurred trying to validate the given information. Please check your username and password. If this problem persists, please contact your system administrator.");
            }
            _cachedAuthMethods.put(username, authMethod);
            return authMethod;
        }
    }

    private static UsernamePasswordAuthenticationToken buildUPToken(XnatAuthenticationProvider provider, String username, String password) {
        if (provider instanceof XnatLdapAuthenticationProvider) {
            return new XnatLdapUsernamePasswordAuthenticationToken(username, password, provider.getProviderId());
        } else if (provider instanceof AliasTokenAuthenticationProvider) {
            return new AliasTokenAuthenticationToken(username, password);
        } else {
            return new XnatDatabaseUsernamePasswordAuthenticationToken(username, password);
        }
    }

    private XnatAuthenticationProvider findAuthenticationProviderByAuthMethod(final String authMethod) {
        return findAuthenticationProvider(new XnatAuthenticationProviderMatcher() {
            @Override
            public boolean matches(XnatAuthenticationProvider provider) {
                return provider.getAuthMethod().equalsIgnoreCase(authMethod);
            }
        });
    }

    private XnatAuthenticationProvider findAuthenticationProviderByProviderName(final String providerName) {
        return findAuthenticationProvider(new XnatAuthenticationProviderMatcher() {
            @Override
            public boolean matches(XnatAuthenticationProvider provider) {
                return provider.getName().equalsIgnoreCase(providerName);
            }
        });
    }

    private XnatAuthenticationProvider findAuthenticationProvider(XnatAuthenticationProviderMatcher matcher) {
        List<AuthenticationProvider> prov = getProviders();
        for (AuthenticationProvider ap : prov) {
            if (XnatAuthenticationProvider.class.isAssignableFrom(ap.getClass())) {
                XnatAuthenticationProvider xap = (XnatAuthenticationProvider) ap;
                if (matcher.matches(xap)) {
                    return xap;
                }
            }
        }
        return null;
    }

    private void copyDetails(Authentication source, Authentication destination) {
        if ((destination instanceof AbstractAuthenticationToken) && (destination.getDetails() == null)) {
            AbstractAuthenticationToken token = (AbstractAuthenticationToken) destination;

            token.setDetails(source.getDetails());
        }
    }

    private static final class AuthenticationAttemptEventPublisher implements AuthenticationEventPublisher {

        private final FailedAttemptsManager      failedAttemptsManager;
        private final LastSuccessfulLoginManager lastSuccessfulLoginManager;

        private AuthenticationAttemptEventPublisher(final XnatProviderManager manager, final XdatUserAuthService service, final SiteConfigPreferences preferences) {
            failedAttemptsManager = new FailedAttemptsManager(manager, service, preferences);
            lastSuccessfulLoginManager = new LastSuccessfulLoginManager(manager, service);
        }

        public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
            //increment failed login attempt
            failedAttemptsManager.addFailedLoginAttempt(authentication);
        }

        public void publishAuthenticationSuccess(Authentication authentication) {
            failedAttemptsManager.clearCount(authentication);
            lastSuccessfulLoginManager.updateLastSuccessfulLogin(authentication);
        }
    }

    private static final class LastSuccessfulLoginManager {
        public LastSuccessfulLoginManager(final XnatProviderManager manager, final XdatUserAuthService service) {
            _manager = manager;
            _service = service;
        }

        private void updateLastSuccessfulLogin(final Authentication auth) {
            final XdatUserAuth userAuth = _manager.getUserByAuth(auth);
            if (userAuth != null) {
                final Date now = Calendar.getInstance(TimeZone.getDefault()).getTime();
                userAuth.setLastSuccessfulLogin(now);
                userAuth.setLastLoginAttempt(now);
                _service.update(userAuth);
            }
        }

        private final XnatProviderManager _manager;
        private final XdatUserAuthService _service;
    }

    private static final class FailedAttemptsManager {
        public FailedAttemptsManager(final XnatProviderManager manager, final XdatUserAuthService service, final SiteConfigPreferences preferences) {
            _manager = manager;
            _service = service;
            _preferences = preferences;
        }

        /**
         * Increments failed Login count
         *
         * @param authentication The authentication that failed.
         */
        private synchronized void addFailedLoginAttempt(final Authentication authentication) {
            final XdatUserAuth userAuth = _manager.getUserByAuth(authentication);
            if (userAuth != null && !userAuth.getXdatUsername().equals("guest")) {
                if (_preferences.getMaxFailedLogins() > 0) {
                    userAuth.setFailedLoginAttempts(userAuth.getFailedLoginAttempts() + 1);
                    final Date now = new Date();
                    userAuth.setLastLoginAttempt(now);
                    if (userAuth.getFailedLoginAttempts() == _preferences.getMaxFailedLogins()) {
                        userAuth.setLockoutTime(now);
                    }
                    _service.update(userAuth);
                }

                if (StringUtils.isNotEmpty(userAuth.getXdatUsername())) {
                    final Integer uid = Users.getUserId(userAuth.getXdatUsername());
                    if (uid != null) {
                        try {
                            if (userAuth.getFailedLoginAttempts().equals(_preferences.getMaxFailedLogins())) {
                                final String expiration = TurbineUtils.getDateTimeFormatter().format(DateUtils.addMilliseconds(GregorianCalendar.getInstance().getTime(), 1000 * convertPGIntervalToIntSeconds(_preferences.getMaxFailedLoginsLockoutDuration())));
                                log.info("Locked out user account {} until {}", userAuth.getXdatUsername(), expiration);
                                if (Roles.isSiteAdmin(new XDATUser(userAuth.getXdatUsername()))) {
                                    AdminUtils.emailAllAdmins(userAuth.getXdatUsername() + " account temporarily disabled. This is an admin account.", "User " + userAuth.getXdatUsername() + " has been temporarily disabled due to excessive failed login attempts. The user's account will be automatically enabled at " + expiration + ".");
                                } else {
                                    AdminUtils.sendAdminEmail(userAuth.getXdatUsername() + " account temporarily disabled.", "User " + userAuth.getXdatUsername() + " has been temporarily disabled due to excessive failed login attempts. The user's account will be automatically enabled at " + expiration + ".");
                                }
                            }
                        } catch (Exception e) {
                            //ignore
                        }
                    }
                }
            }
        }

        public void clearCount(final Authentication authentication) {
            if (_preferences.getMaxFailedLogins() > 0) {
                final XdatUserAuth userAuth = _manager.getUserByAuth(authentication);
                if (userAuth != null) {
                    userAuth.setFailedLoginAttempts(0);
                    userAuth.setLockoutTime(null);
                    _service.update(userAuth);
                }
            }
        }

        private final XnatProviderManager   _manager;
        private final XdatUserAuthService   _service;
        private final SiteConfigPreferences _preferences;
    }

    private interface XnatAuthenticationProviderMatcher {
        boolean matches(XnatAuthenticationProvider provider);
    }

    private static final Map<String, String> _cachedAuthMethods = new ConcurrentHashMap<>(); // This will prevent 20,000 curl scripts from hitting the db every time

    private final MessageSource                   _messageSource;
    private final XdatUserAuthService             _userAuthService;
    private final AnonymousAuthenticationProvider _anonymousAuthenticationProvider;
    private final DataSource                      _dataSource;
    private final AuthenticationEventPublisher    _eventPublisher;
}
