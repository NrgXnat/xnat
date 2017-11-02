/*
 * web: org.nrg.xnat.security.XnatProviderManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.velocity.VelocityContext;
import org.hibernate.exception.DataException;
import org.nrg.xdat.XDAT;
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
import org.nrg.xnat.security.exceptions.NewAutoAccountNotAutoEnabledException;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.nrg.xnat.security.tokens.XnatAuthenticationToken;
import org.nrg.xnat.security.tokens.XnatDatabaseUsernamePasswordAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.nrg.xnat.initialization.SecurityConfig.ANONYMOUS_AUTH_PROVIDER_KEY;

@Service
@Slf4j
public class XnatProviderManager extends ProviderManager {
    @Autowired
    public XnatProviderManager(final SiteConfigPreferences preferences, final XdatUserAuthService userAuthService, final List<AuthenticationProvider> providers) {
        super(providers);

        _userAuthService = userAuthService;
        _eventPublisher = new AuthenticationAttemptEventPublisher(this, preferences);

        final Set<AnonymousAuthenticationProvider> anonymousProviders = Sets.newHashSet(Iterables.filter(providers, AnonymousAuthenticationProvider.class));
        if (anonymousProviders.isEmpty()) {
            log.warn("Didn't find an anonymous authentication provider. This may lead to weirdness later on, but I'm creating a stand-in just in case.");
            _anonymousAuthenticationProvider = new AnonymousAuthenticationProvider(ANONYMOUS_AUTH_PROVIDER_KEY);
        } else {
            if (anonymousProviders.size() > 1) {
                log.warn("Found multiple (" + anonymousProviders.size() + ") anonymous providers. Just using the first one, but that may not be the right thing to do.");
            }
            _anonymousAuthenticationProvider = anonymousProviders.iterator().next();
        }

        _xnatAuthenticationProviders.putAll(Maps.uniqueIndex(Iterables.filter(providers, XnatAuthenticationProvider.class), new Function<XnatAuthenticationProvider, String>() {
            @Nullable
            @Override
            public String apply(@Nullable final XnatAuthenticationProvider provider) {
                return provider != null ? provider.getProviderId() : null;
            }
        }));
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final Class<? extends Authentication> toTest    = authentication.getClass();
        final List<AuthenticationProvider>    providers = new ArrayList<>();

        // HACK: This is a hack to work around open XNAT auth issue. If this is a bare un/pw auth token, use anon auth.
        final Authentication converted;
        if (authentication.getClass() == UsernamePasswordAuthenticationToken.class && authentication.getName().equalsIgnoreCase("guest")) {
            providers.add(_anonymousAuthenticationProvider);
            converted = new AnonymousAuthenticationToken(_anonymousAuthenticationProvider.getKey(), authentication.getPrincipal(), Collections.<GrantedAuthority>singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        } else {
            converted = authentication;
            for (final AuthenticationProvider candidate : getProviders()) {
                // If the candidate doesn't support the token type, we're done here.
                if (!candidate.supports(toTest)) {
                    continue;
                }

                // Test whether it's an XNAT auth provider and it supports this token instance...
                if (candidate instanceof XnatAuthenticationProvider && !((XnatAuthenticationProvider) candidate).supports(authentication)) {
                    // If yes and no, then bail: XNAT auth provider may support the token CLASS, but not the token instance.
                    continue;
                }
                providers.add(candidate);
            }
        }

        if (providers.size() == 0) {
            final ProviderNotFoundException exception = new ProviderNotFoundException(_messageSource.getMessage("providerManager.providerNotFound", new Object[]{toTest.getName()}, "No authentication provider found for {0}"));
            _eventPublisher.publishAuthenticationFailure(exception, authentication);
            throw exception;
        }

        final Map<AuthenticationProvider, AuthenticationException> exceptionMap = new HashMap<>();
        for (final AuthenticationProvider provider : providers) {
            log.debug("Authentication attempt using " + provider.getClass().getName());

            try {
                final Authentication result = provider.authenticate(converted);
                if (result != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found a provider that worked for " + authentication.getName() + ": " + provider.getClass().getSimpleName());
                    }

                    copyDetails(authentication, result);
                    _eventPublisher.publishAuthenticationSuccess(authentication);
                    return result;
                }
            } catch (AccountStatusException exception) {
                log.warn("Error occurred authenticating login request with provider " + provider.getClass(), exception);
                exceptionMap.put(provider, exception);
            } catch (NewAutoAccountNotAutoEnabledException exception) {
                try {
                    AdminUtils.sendNewUserNotification(exception.getUser(), "", "", "", new VelocityContext());
                } catch (Exception embedded) {
                    log.error("Error occurred sending new user request email", embedded);
                }
                exceptionMap.put(provider, exception);
            } catch (AuthenticationServiceException exception) {
                log.error("Got a service exception for the provider " + provider.toString(), exception);
                exceptionMap.put(provider, exception);
            } catch (AuthenticationException exception) {
                exceptionMap.put(provider, exception);
            }
        }

        final AuthenticationException cause;
        final AuthenticationProvider  provider;
        if (exceptionMap.size() == 1) {
            provider = exceptionMap.entrySet().iterator().next().getKey();
            cause = exceptionMap.get(provider);
        } else {
            final Pair<AuthenticationProvider, AuthenticationException> pair = getMostImportantException(exceptionMap);
            provider = pair.getLeft();
            cause = pair.getRight();
        }
        log.info("Provider " + (provider != null ? provider.toString() : "<unknown>") + " failed to validate user " + authentication.getPrincipal(), cause);
        _eventPublisher.publishAuthenticationFailure(cause, authentication);
        throw cause;
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
            username = ((UserI) principal).getUsername();
        }

        if (StringUtils.isBlank(username)) {
            throw new RuntimeException("An error occurred trying to get user from authentication: no principal or user name was found.");
        }

        final String method;
        final String provider;
        if (authentication instanceof XnatAuthenticationToken) {
            provider = ((XnatAuthenticationToken) authentication).getProviderId();
            method = _xnatAuthenticationProviders.get(provider).getAuthMethod();
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
        if (CACHED_AUTH_METHODS.containsKey(username)) {
            return CACHED_AUTH_METHODS.get(username);
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
            CACHED_AUTH_METHODS.put(username, authMethod);
            return authMethod;
        }
    }

    private static UsernamePasswordAuthenticationToken buildUPToken(final AuthenticationProvider provider, final String username, final String password) {
        if (provider instanceof XnatAuthenticationProvider) {
            return (UsernamePasswordAuthenticationToken) ((XnatAuthenticationProvider) provider).createToken(username, password);
        } else {
            return new XnatDatabaseUsernamePasswordAuthenticationToken(username, password);
        }
    }

    private Pair<AuthenticationProvider, AuthenticationException> getMostImportantException(final Map<AuthenticationProvider, AuthenticationException> exceptionMap) {
        final ArrayList<AuthenticationException> exceptions = new ArrayList<>(exceptionMap.values());
        Collections.sort(exceptions, new Comparator<AuthenticationException>() {
            @Override
            public int compare(final AuthenticationException exception1, final AuthenticationException exception2) {
                return Integer.compare(getRank(exception1.getClass()), getRank(exception2.getClass()));
            }

            private int getRank(final Class<? extends AuthenticationException> clazz) {
                for (final Class<? extends AuthenticationException> test : RANKED_AUTH_EXCEPTIONS) {
                    if (test.isAssignableFrom(clazz)) {
                        return RANKED_AUTH_EXCEPTIONS.indexOf(test);
                    }
                }
                return 0;
            }
        });
        final AuthenticationException cause = exceptions.get(0);
        for (final Map.Entry<AuthenticationProvider, AuthenticationException> entry : exceptionMap.entrySet()) {
            if (entry.getValue().equals(cause)) {
                return new ImmutablePair<>(entry.getKey(), entry.getValue());
            }
        }
        return new ImmutablePair<>(null, cause);
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

        private AuthenticationAttemptEventPublisher(final XnatProviderManager manager, final SiteConfigPreferences preferences) {
            failedAttemptsManager = new FailedAttemptsManager(manager, preferences);
            lastSuccessfulLoginManager = new LastSuccessfulLoginManager(manager);
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
        private final XnatProviderManager _manager;

        LastSuccessfulLoginManager(final XnatProviderManager manager) {
            _manager = manager;
        }

        private void updateLastSuccessfulLogin(Authentication auth) {
            XdatUserAuth ua = _manager.getUserByAuth(auth);
            if (ua != null) {
                Date now = java.util.Calendar.getInstance(TimeZone.getDefault()).getTime();
                ua.setLastSuccessfulLogin(now);
                ua.setLastLoginAttempt(now);
                XDAT.getXdatUserAuthService().update(ua);
            }
        }
    }

    private static final class FailedAttemptsManager {
        private final XnatProviderManager   _manager;
        private final SiteConfigPreferences _preferences;

        FailedAttemptsManager(final XnatProviderManager manager, final SiteConfigPreferences preferences) {
            _manager = manager;
            _preferences = preferences;
        }

        /**
         * Increments failed Login count
         *
         * @param auth The authentication that failed.
         */
        private synchronized void addFailedLoginAttempt(final Authentication auth) {
            XdatUserAuth ua = _manager.getUserByAuth(auth);
            if (ua != null && !ua.getXdatUsername().equals("guest")) {
                if (XDAT.getSiteConfigPreferences().getMaxFailedLogins() > 0) {
                    ua.setFailedLoginAttempts(ua.getFailedLoginAttempts() + 1);
                    Date currTime = new Date();
                    ua.setLastLoginAttempt(currTime);
                    if (ua.getFailedLoginAttempts() == _preferences.getMaxFailedLogins()) {
                        ua.setLockoutTime(currTime);
                    }
                    XDAT.getXdatUserAuthService().update(ua);
                }

                if (StringUtils.isNotEmpty(ua.getXdatUsername())) {
                    Integer uid = Users.getUserid(ua.getXdatUsername());
                    if (uid != null) {
                        try {
                            if (ua.getFailedLoginAttempts().equals(XDAT.getSiteConfigPreferences().getMaxFailedLogins())) {
                                String expiration = TurbineUtils.getDateTimeFormatter().format(DateUtils.addMilliseconds(GregorianCalendar.getInstance().getTime(), 1000 * (int) SiteConfigPreferences.convertPGIntervalToSeconds(XDAT.getSiteConfigPreferences().getMaxFailedLoginsLockoutDuration())));
                                log.info("Locked out " + ua.getXdatUsername() + " user account until " + expiration);
                                if (Roles.isSiteAdmin(new XDATUser(ua.getXdatUsername()))) {
                                    AdminUtils.emailAllAdmins(ua.getXdatUsername() + " account temporarily disabled. This is an admin account.", "User " + ua.getXdatUsername() + " has been temporarily disabled due to excessive failed login attempts. The user's account will be automatically enabled at " + expiration + ".");
                                } else {
                                    AdminUtils.sendAdminEmail(ua.getXdatUsername() + " account temporarily disabled.", "User " + ua.getXdatUsername() + " has been temporarily disabled due to excessive failed login attempts. The user's account will be automatically enabled at " + expiration + ".");
                                }
                            }
                        } catch (Exception e) {
                            //ignore
                        }
                    }
                }
            }
        }

        public void clearCount(final Authentication auth) {
            if (XDAT.getSiteConfigPreferences().getMaxFailedLogins() > 0) {
                XdatUserAuth ua = _manager.getUserByAuth(auth);
                if (ua != null) {
                    ua.setFailedLoginAttempts(0);
                    ua.setLockoutTime(null);
                    XDAT.getXdatUserAuthService().update(ua);
                }
            }
        }
    }

    private interface XnatAuthenticationProviderMatcher {
        boolean matches(XnatAuthenticationProvider provider);
    }

    private static final Map<String, String>                            CACHED_AUTH_METHODS    = new ConcurrentHashMap<>(); // This will prevent 20,000 curl scripts from hitting the db every time
    private static final List<Class<? extends AuthenticationException>> RANKED_AUTH_EXCEPTIONS = Arrays.asList(BadCredentialsException.class,
                                                                                                               AuthenticationCredentialsNotFoundException.class,
                                                                                                               AuthenticationServiceException.class,
                                                                                                               ProviderNotFoundException.class,
                                                                                                               InsufficientAuthenticationException.class,
                                                                                                               AccountStatusException.class);


    private final MessageSourceAccessor                   _messageSource               = SpringSecurityMessageSource.getAccessor();
    private final Map<String, XnatAuthenticationProvider> _xnatAuthenticationProviders = new HashMap<>();

    private final XdatUserAuthService          _userAuthService;
    private final AuthenticationEventPublisher _eventPublisher;

    private AnonymousAuthenticationProvider _anonymousAuthenticationProvider;
}
