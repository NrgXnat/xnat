/*
 * web: org.nrg.xnat.security.XnatProviderManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.velocity.VelocityContext;
import org.hibernate.exception.DataException;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xnat.security.exceptions.NewAutoAccountNotAutoEnabledException;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.nrg.xnat.security.provider.XnatMulticonfigAuthenticationProvider;
import org.nrg.xnat.security.tokens.XnatDatabaseUsernamePasswordAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.nrg.xdat.security.helpers.Users.AUTHORITIES_ANONYMOUS;

@SuppressWarnings("SqlResolve")
@Service
@Slf4j
public class XnatProviderManager extends ProviderManager {
    @Autowired(required = false)
    public void setMultipleAuthProviders(final List<XnatMulticonfigAuthenticationProvider> providers) {
        if (providers != null) {
            for (XnatMulticonfigAuthenticationProvider multiProvider : providers) {
                for (String pid : multiProvider.getProviderIds()) {
                    _xnatAuthenticationProviders.put(pid, multiProvider.getProvider(pid));
                }
            }
        }
    }

    @Autowired
    public XnatProviderManager(final SiteConfigPreferences preferences, final AuthenticationEventPublisher eventPublisher, final XdatUserAuthService userAuthService, final List<AuthenticationProvider> providers) {
        super(providers);

        _preferences     = preferences;
        _userAuthService = userAuthService;
        _eventPublisher  = eventPublisher;
        _xnatAuthenticationProviders.putAll(providers.stream().filter(XnatAuthenticationProvider.class::isInstance)
                                                     .map(XnatAuthenticationProvider.class::cast)
                                                     .collect(Collectors.toMap(XnatAuthenticationProvider::getProviderId,
                                                                               Function.identity())));
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final Class<? extends Authentication>  toTest    = authentication.getClass();
        final List<XnatAuthenticationProvider> providers = new ArrayList<>();

        // HACK: This is a hack to work around open XNAT auth issue. If this is a bare un/pw auth token, use anon auth.
        final Authentication converted;
        if (authentication.getClass() == UsernamePasswordAuthenticationToken.class && authentication.getName().equalsIgnoreCase("guest")) {
            converted = new AnonymousAuthenticationToken(ANONYMOUS_AUTH_PROVIDER_KEY, authentication.getPrincipal(), AUTHORITIES_ANONYMOUS);
        } else {
            converted = authentication;
            for (final XnatAuthenticationProvider xnatAuthenticationProvider : _xnatAuthenticationProviders.values()) {
                // If the candidate doesn't support the token type, we're done here.
                if (!xnatAuthenticationProvider.supports(toTest)) {
                    continue;
                }

                // Now check whether the provider is enabled and supports the token instance.
                if (_preferences.getEnabledProviders().contains(xnatAuthenticationProvider.getProviderId()) && xnatAuthenticationProvider.supports(authentication)) {
                    providers.add(xnatAuthenticationProvider);
                }
            }
        }

        if (providers.isEmpty()) {
            final ProviderNotFoundException exception = new ProviderNotFoundException(_messageSource.getMessage("providerManager.providerNotFound", new Object[]{toTest.getName()}, "No authentication provider found for {0}"));
            _eventPublisher.publishAuthenticationFailure(exception, authentication);
            throw exception;
        }

        final Map<AuthenticationProvider, AuthenticationException> exceptionMap = new HashMap<>();
        for (final XnatAuthenticationProvider provider : providers) {
            log.debug("Authentication attempt using {}", provider.getClass().getName());

            try {
                final Authentication result = provider.authenticate(converted);
                if (result != null) {
                    log.debug("Found a provider that worked for {}: {}", authentication.getName(), provider.getClass().getSimpleName());
                    copyDetails(authentication, result);
                    _eventPublisher.publishAuthenticationSuccess(authentication);
                    return result;
                }
            } catch (DisabledException exception) {
                log.warn("User {} tried to log into the system but that account is disabled", converted.getName());
                exceptionMap.put(provider, exception);
            } catch (AccountStatusException exception) {
                log.warn("Error occurred authenticating login request with provider {}", provider.getClass(), exception);
                exceptionMap.put(provider, exception);
            } catch (NewAutoAccountNotAutoEnabledException exception) {
                try {
                    AdminUtils.sendNewUserNotification(exception.getUser(), "", "", "", new VelocityContext());
                } catch (Exception embedded) {
                    log.error("Error occurred sending new user request email", embedded);
                }
                exceptionMap.put(provider, exception);
            } catch (AuthenticationServiceException exception) {
                log.error("Got a service exception for the provider {}", provider, exception);
                exceptionMap.put(provider, exception);
            } catch (AuthenticationException exception) {
                exceptionMap.put(provider, exception);
            }
        }

        final AuthenticationException cause;
        final AuthenticationProvider  provider;
        if (exceptionMap.size() == 1) {
            provider = exceptionMap.entrySet().iterator().next().getKey();
            cause    = exceptionMap.get(provider);
        } else {
            final Pair<AuthenticationProvider, AuthenticationException> pair = getMostImportantException(exceptionMap);
            provider = pair.getLeft();
            cause    = pair.getRight();
        }
        log.info("Provider '{}' failed to validate user {}: {}", provider != null ? provider.toString() : "<unknown>", authentication.getPrincipal(), cause.getMessage());
        _eventPublisher.publishAuthenticationFailure(cause, authentication);
        throw cause;
    }

    public UsernamePasswordAuthenticationToken buildUPTokenForAuthMethod(final String authMethod, final String username, final String password) {
        return buildUPToken(findAuthenticationProviderByAuthMethod(authMethod), username, password);
    }

    public UsernamePasswordAuthenticationToken buildUPTokenForProviderName(final String providerName, final String username, final String password) {
        return buildUPToken(findAuthenticationProviderByProviderName(providerName), username, password);
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
                    // The list may contain localdb auth method even when password is empty and some other authentication method is used (MRH)
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

    public XnatAuthenticationProvider getProvider(final String authMethod, final String providerId) {
        // First check that we have the provider ID.
        if (_xnatAuthenticationProviders.containsKey(providerId)) {
            // Now get the provider...
            final XnatAuthenticationProvider provider = _xnatAuthenticationProviders.get(providerId);
            // And check that it also matches the auth method. If so...
            if (StringUtils.equalsIgnoreCase(authMethod, provider.getAuthMethod())) {
                // Return it.
                return provider;
            }
        }
        // If it wasn't both of those things, return null.
        return null;
    }

    public Map<String, XnatAuthenticationProvider> getVisibleEnabledProviders() {
        return getFilteredEnabledProviders(XnatAuthenticationProvider::isVisible);
    }

    public Map<String, XnatAuthenticationProvider> getLinkedEnabledProviders() {
        return getFilteredEnabledProviders(XnatAuthenticationProvider::hasLink);
    }

    private Map<String, XnatAuthenticationProvider> getFilteredEnabledProviders(final Predicate<XnatAuthenticationProvider> filter) {
        final List<String> enabled = _preferences.getEnabledProviders();
        final Map<String, XnatAuthenticationProvider> configured = enabled.stream()
                                                                          .map(_xnatAuthenticationProviders::get)
                                                                          .filter(Objects::nonNull)
                                                                          .collect(Collectors.toMap(XnatAuthenticationProvider::getProviderId, Function.identity()));
        if (enabled.size() > configured.size()) {
            final Sets.SetView<String> difference = Sets.difference(new HashSet<>(enabled), configured.keySet());
            log.warn("{} provider IDs are enabled, but don't have configured definitions: {}", difference.size(), String.join(", ", difference));
        }
        final Map<String, XnatAuthenticationProvider> filteredProviders = configured.values().stream()
                                                                                    .filter(filter)
                                                                                    .collect(Collectors.toMap(XnatAuthenticationProvider::getProviderId, Function.identity(), (k1, k2) -> k2, LinkedHashMap::new));
        if (log.isDebugEnabled()) {
            log.debug("Added {} provider IDs to the list of filtered authentication providers: {}", filteredProviders.keySet().size(), String.join(", ", filteredProviders.keySet()));
        }
        return filteredProviders;
    }

    private Pair<AuthenticationProvider, AuthenticationException> getMostImportantException(final Map<AuthenticationProvider, AuthenticationException> exceptionMap) {
        final ArrayList<AuthenticationException> exceptions = new ArrayList<>(exceptionMap.values());
        exceptions.sort(new Comparator<AuthenticationException>() {
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
        return findAuthenticationProvider(provider -> provider.getAuthMethod().equalsIgnoreCase(authMethod));
    }

    private XnatAuthenticationProvider findAuthenticationProviderByProviderName(final String providerName) {
        return findAuthenticationProvider(provider -> providerName.equalsIgnoreCase(provider.getProviderId()));
    }

    private XnatAuthenticationProvider findAuthenticationProvider(final XnatAuthenticationProviderMatcher matcher) {
        for (final XnatAuthenticationProvider xnatAuthenticationProvider : _xnatAuthenticationProviders.values()) {
            if (matcher.matches(xnatAuthenticationProvider)) {
                return xnatAuthenticationProvider;
            }
        }
        return null;
    }

    private void copyDetails(Authentication source, Authentication destination) {
        if ((destination instanceof AbstractAuthenticationToken) && (destination.getDetails() == null)) {
            final AbstractAuthenticationToken token = (AbstractAuthenticationToken) destination;
            token.setDetails(source.getDetails());
        }
    }

    private static UsernamePasswordAuthenticationToken buildUPToken(final AuthenticationProvider provider, final String username, final String password) {
        return provider instanceof XnatAuthenticationProvider
               ? (UsernamePasswordAuthenticationToken) ((XnatAuthenticationProvider) provider).createToken(username, password)
               : new XnatDatabaseUsernamePasswordAuthenticationToken(username, password);
    }

    private interface XnatAuthenticationProviderMatcher {
        boolean matches(XnatAuthenticationProvider provider);
    }

    private static final String                                         ANONYMOUS_AUTH_PROVIDER_KEY = "xnat-anonymous-provider-key";
    private static final Map<String, String>                            CACHED_AUTH_METHODS         = new ConcurrentHashMap<>(); // This will prevent 20,000 curl scripts from hitting the db every time
    private static final List<Class<? extends AuthenticationException>> RANKED_AUTH_EXCEPTIONS      = Arrays.asList(BadCredentialsException.class,
                                                                                                                    AuthenticationCredentialsNotFoundException.class,
                                                                                                                    AuthenticationServiceException.class,
                                                                                                                    ProviderNotFoundException.class,
                                                                                                                    InsufficientAuthenticationException.class,
                                                                                                                    AccountStatusException.class);

    private final MessageSourceAccessor                   _messageSource               = SpringSecurityMessageSource.getAccessor();
    private final Map<String, XnatAuthenticationProvider> _xnatAuthenticationProviders = new HashMap<>();

    private final SiteConfigPreferences        _preferences;
    private final XdatUserAuthService          _userAuthService;
    private final AuthenticationEventPublisher _eventPublisher;
}
