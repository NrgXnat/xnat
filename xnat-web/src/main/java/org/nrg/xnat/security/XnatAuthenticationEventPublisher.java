package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.exception.DataException;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.nrg.xnat.security.provider.XnatMulticonfigAuthenticationProvider;
import org.nrg.xnat.security.tokens.XnatAuthenticationToken;
import org.nrg.xnat.security.tokens.XnatDatabaseUsernamePasswordAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.nrg.framework.orm.DatabaseHelper.convertPGIntervalToSeconds;

@Component
@Slf4j
public final class XnatAuthenticationEventPublisher implements AuthenticationEventPublisher {
    @Autowired(required = false)
    public void setMultipleAuthProviders(final List<XnatMulticonfigAuthenticationProvider> providers) {
        if (providers != null) {
            for (XnatMulticonfigAuthenticationProvider multiProvider : providers) {
                for (XnatAuthenticationProvider provider : multiProvider.getProviders()) {
                    _providers.put(provider.getProviderId(), provider.getAuthMethod());
                }
            }
        }
    }

    @Autowired
    public XnatAuthenticationEventPublisher(final XdatUserAuthService userAuthService, final SiteConfigPreferences siteConfigPreferences, final List<AuthenticationProvider> providers) {
        _failedAttemptsManager = new FailedAttemptsManager(this, userAuthService, siteConfigPreferences);
        _lastSuccessfulLoginManager = new LastSuccessfulLoginManager(this, userAuthService);
        _userAuthService = userAuthService;
        _providers.putAll(providers.stream().filter(XnatAuthenticationProvider.class::isInstance)
                .map(XnatAuthenticationProvider.class::cast)
                .collect(Collectors.toMap(XnatAuthenticationProvider::getProviderId,
                        XnatAuthenticationProvider::getAuthMethod)));
    }

    @Override
    public void publishAuthenticationFailure(final AuthenticationException exception, final Authentication authentication) {
        _failedAttemptsManager.addFailedLoginAttempt(authentication);
    }

    @Override
    public void publishAuthenticationSuccess(final Authentication authentication) {
        _failedAttemptsManager.clearCount(authentication);
        _lastSuccessfulLoginManager.updateLastSuccessfulLogin(authentication);
    }

    @EventListener
    public void authenticationSuccess(final AuthenticationSuccessEvent event) {
        publishAuthenticationSuccess(event.getAuthentication());
    }

    @EventListener
    public void authenticationFailure(final AuthorizationFailureEvent event) {
        publishAuthenticationFailure(null, event.getAuthentication());
    }

    private XdatUserAuth getUserByAuth(final Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        final Object principal = authentication.getPrincipal();
        final String username = principal == null
                                ? authentication.getName()
                                : principal instanceof String
                                  ? (String) principal
                                  : ((UserI) principal).getUsername();

        if (StringUtils.isBlank(username)) {
            throw new RuntimeException("An error occurred trying to get user from authentication: no principal or user name was found.");
        }
        if (StringUtils.equalsAnyIgnoreCase(username, "guest", "anonymousUser")) {
            log.debug("Someone's trying to retrieve an unauthenticated user account: {}", username);
            return null;
        }

        final String method;
        final String provider;
        if (authentication instanceof XnatDatabaseUsernamePasswordAuthenticationToken || !(authentication instanceof XnatAuthenticationToken)) {
            method = XdatUserAuthService.LOCALDB;
            provider = "";
        } else {
            provider = ((XnatAuthenticationToken) authentication).getProviderId();
            method = _providers.get(provider);
        }

        try {
            final XdatUserAuth userAuth = _userAuthService.getUserByNameAndAuth(username, method, provider);
            if (userAuth != null) {
                log.info("Found user auth record for XNAT user '{}' by username '{}' in provider definition '{}' of auth method '{}'", userAuth.getXdatUsername(), username, provider, method);
            } else {
                log.warn("Searched for user auth record by username '{}' in provider definition '{}' of auth method '{}', but didn't find anything.", username, provider, method);
            }
            return userAuth;
        } catch (DataException exception) {
            throw new RuntimeException("An error occurred trying to validate the given information. Please check your username and password. If this problem persists, please contact your system administrator.");
        }
    }

    private static final class LastSuccessfulLoginManager {
        LastSuccessfulLoginManager(final XnatAuthenticationEventPublisher publisher, final XdatUserAuthService userAuthService) {
            _publisher = publisher;
            _userAuthService = userAuthService;
        }

        private void updateLastSuccessfulLogin(final Authentication authentication) {
            final XdatUserAuth userAuth = _publisher.getUserByAuth(authentication);
            if (userAuth != null) {
                log.info("Updating last successful login date for user {}", userAuth.getXdatUsername());
                final Date now = Calendar.getInstance(TimeZone.getDefault()).getTime();
                userAuth.setLastSuccessfulLogin(now);
                userAuth.setLastLoginAttempt(now);
                _userAuthService.update(userAuth);
            }
        }

        private final XdatUserAuthService              _userAuthService;
        private final XnatAuthenticationEventPublisher _publisher;
    }

    private static final class FailedAttemptsManager {
        FailedAttemptsManager(final XnatAuthenticationEventPublisher publisher, final XdatUserAuthService userAuthService, final SiteConfigPreferences preferences) {
            _publisher = publisher;
            _userAuthService = userAuthService;
            _preferences = preferences;
        }

        /**
         * Clears the failed login account for the user specified in the submitted authentication object.
         *
         * @param authentication The authentication object containing the user principal.
         */
        public void clearCount(final Authentication authentication) {
            if (_preferences.getMaxFailedLogins() > 0) {
                final XdatUserAuth userAuth = _publisher.getUserByAuth(authentication);
                if (userAuth != null) {
                    log.info("Clearing the failed login count for the user {}", userAuth.getXdatUsername());
                    _userAuthService.resetFailedLogins(userAuth);
                }
            }
        }

        /**
         * Increments failed login count for the user specified in the submitted authentication object.
         *
         * @param authentication The authentication that failed.
         */
        private synchronized void addFailedLoginAttempt(final Authentication authentication) {
            final XdatUserAuth userAuth = _publisher.getUserByAuth(authentication);
            if (userAuth != null && !userAuth.getXdatUsername().equals("guest")) {
                if (_preferences.getMaxFailedLogins() > 0) {
                    if (_userAuthService.addFailedLoginAttempt(userAuth)) {
                        log.info("Added failed login attempt for XNAT user '{}' through username '{}', provider ID '{}', method '{}'. This resulted in the user being locked out.", userAuth.getXdatUsername(), userAuth.getAuthUser(), userAuth.getAuthMethodId(), userAuth.getAuthMethod());
                    }
                }

                if (StringUtils.isNotEmpty(userAuth.getXdatUsername())) {
                    final Integer uid = Users.getUserId(userAuth.getXdatUsername());
                    if (uid != null) {
                        try {
                            if (userAuth.getFailedLoginAttempts().equals(_preferences.getMaxFailedLogins())) {
                                final String expiration = TurbineUtils.getDateTimeFormatter().format(DateUtils.addMilliseconds(GregorianCalendar.getInstance().getTime(), 1000 * (int) convertPGIntervalToSeconds(_preferences.getMaxFailedLoginsLockoutDuration())));
                                log.info("Locked out {} user account until {}", userAuth.getXdatUsername(), expiration);
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

        private final XdatUserAuthService              _userAuthService;
        private final SiteConfigPreferences            _preferences;
        private final XnatAuthenticationEventPublisher _publisher;
    }

    private final FailedAttemptsManager      _failedAttemptsManager;
    private final LastSuccessfulLoginManager _lastSuccessfulLoginManager;
    private final XdatUserAuthService        _userAuthService;
    private final Map<String, String>        _providers = new HashMap<>();
}
