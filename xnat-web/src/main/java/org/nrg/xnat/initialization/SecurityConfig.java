/*
 * web: org.nrg.xnat.initialization.SecurityConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.UserGroupManager;
import org.nrg.xdat.security.UserGroupServiceI;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xnat.security.*;
import org.nrg.xnat.security.alias.AliasTokenAuthenticationProvider;
import org.nrg.xnat.security.config.AuthenticationProviderAggregator;
import org.nrg.xnat.security.config.AuthenticationProviderConfigurator;
import org.nrg.xnat.security.config.DatabaseAuthenticationProviderConfigurator;
import org.nrg.xnat.security.config.LdapAuthenticationProviderConfigurator;
import org.nrg.xnat.security.userdetailsservices.XnatDatabaseUserDetailsService;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.services.validation.DateValidation;
import org.nrg.xnat.utils.DefaultInteractiveAgentDetector;
import org.nrg.xnat.utils.InteractiveAgentDetector;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.channel.ChannelDecisionManagerImpl;
import org.springframework.security.web.access.channel.InsecureChannelProcessor;
import org.springframework.security.web.access.channel.SecureChannelProcessor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.*;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.SimpleRedirectSessionInformationExpiredStrategy;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

@Configuration
@EnableWebSecurity
@ImportResource("WEB-INF/conf/xnat-security.xml")
@Slf4j
public class SecurityConfig {
    @Bean
    public UnanimousBased unanimousBased() {
        final RoleVoter voter = new RoleVoter();
        voter.setRolePrefix("ROLE_");
        final List<AccessDecisionVoter<?>> voters = new ArrayList<>();
        voters.add(voter);
        voters.add(new AuthenticatedVoter());
        return new UnanimousBased(voters);
    }

    @Bean
    public OnXnatLogin logUserLogin() {
        return new OnXnatLogin();
    }

    @Bean
    public AuthenticationFailureHandler authFailure() {
        return new XnatUrlAuthenticationFailureHandler("/app/template/Login.vm?failed=true", "/app/template/PostRegister.vm");
    }

    @Bean
    public InteractiveAgentDetector interactiveAgentDetector(final SiteConfigPreferences preferences) {
        return new DefaultInteractiveAgentDetector(preferences);
    }

    @Bean
    public XnatAuthenticationEntryPoint loginUrlAuthenticationEntryPoint(final SiteConfigPreferences preferences, final InteractiveAgentDetector detector) {
        return new XnatAuthenticationEntryPoint("/app/template/Login.vm", preferences, detector);
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public ConcurrentSessionFilter concurrencyFilter(final SessionRegistry sessionRegistry) {
        return new ConcurrentSessionFilter(sessionRegistry, new SimpleRedirectSessionInformationExpiredStrategy("/app/template/Login.vm"));
    }

    @Bean
    @Primary
    public CompositeSessionAuthenticationStrategy sas(final SessionRegistry sessionRegistry, final SiteConfigPreferences preferences) {
        ArrayList<SessionAuthenticationStrategy> authStrategies = new ArrayList<>();

        final ConcurrentSessionControlAuthenticationStrategy strategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);
        strategy.setMaximumSessions(preferences.getConcurrentMaxSessions());
        strategy.setExceptionIfMaximumExceeded(true);
        authStrategies.add(strategy);

        SessionFixationProtectionStrategy fixationProtectionStrategy = new SessionFixationProtectionStrategy();
        authStrategies.add(fixationProtectionStrategy);

        RegisterSessionAuthenticationStrategy registerSessionAuthenticationStrategy = new RegisterSessionAuthenticationStrategy(sessionRegistry);
        authStrategies.add(registerSessionAuthenticationStrategy);

        return new CompositeSessionAuthenticationStrategy(authStrategies);
    }

    @Bean
    public LogoutFilter logoutFilter(final SessionRegistry sessionRegistry) {
        final XnatLogoutSuccessHandler logoutSuccessHandler = new XnatLogoutSuccessHandler();
        logoutSuccessHandler.setOpenXnatLogoutSuccessUrl("/");
        logoutSuccessHandler.setSecuredXnatLogoutSuccessUrl("/app/template/Login.vm");
        final SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
        securityContextLogoutHandler.setInvalidateHttpSession(true);
        final XnatLogoutHandler xnatLogoutHandler = new XnatLogoutHandler(sessionRegistry);
        final LogoutFilter      filter            = new LogoutFilter(logoutSuccessHandler, securityContextLogoutHandler, xnatLogoutHandler);
        filter.setFilterProcessesUrl("/app/action/LogoutUser");
        return filter;
    }

    @Bean
    public FilterSecurityInterceptorBeanPostProcessor filterSecurityInterceptorBeanPostProcessor(final SiteConfigPreferences preferences, final XnatAppInfo appInfo) {
        return new FilterSecurityInterceptorBeanPostProcessor(preferences, appInfo);
    }

    @Bean
    public TranslatingChannelProcessingFilter channelProcessingFilter(final SiteConfigPreferences preferences) {
        final ChannelDecisionManagerImpl decisionManager = new ChannelDecisionManagerImpl();
        decisionManager.setChannelProcessors(Arrays.asList(new SecureChannelProcessor(), new InsecureChannelProcessor()));
        final TranslatingChannelProcessingFilter filter = new TranslatingChannelProcessingFilter();
        filter.setChannelDecisionManager(decisionManager);
        filter.setRequiredChannel(preferences.getSecurityChannel());
        return filter;
    }

    @Bean
    public AliasTokenAuthenticationProvider aliasTokenAuthenticationProvider(final AliasTokenService aliasTokenService, final XdatUserAuthService userAuthService) {
        return new AliasTokenAuthenticationProvider(aliasTokenService, userAuthService);
    }

    @Bean
    public DatabaseAuthenticationProviderConfigurator dbConfigurator(final XnatDatabaseUserDetailsService userDetailsService) {
        return new DatabaseAuthenticationProviderConfigurator(userDetailsService);
    }

    @Bean
    public LdapAuthenticationProviderConfigurator ldapConfigurator(final XdatUserAuthService service, final SiteConfigPreferences preferences) {
        return new LdapAuthenticationProviderConfigurator(service, preferences);
    }

    @Bean
    public AuthenticationProviderAggregator providerAggregator(final List<AuthenticationProvider> providers, final List<AuthenticationProviderConfigurator> configurators, final ConfigPaths configFolderPaths) {
        final Map<String, AuthenticationProviderConfigurator> configuratorMap = new HashMap<>();
        for (final AuthenticationProviderConfigurator configurator : configurators) {
            configuratorMap.put(configurator.getConfiguratorId(), configurator);
        }

        return new AuthenticationProviderAggregator(providers, configuratorMap, configFolderPaths);
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    @Primary
    public XnatProviderManager customAuthenticationManager(final SiteConfigPreferences preferences, final AuthenticationProviderAggregator aggregator, final XdatUserAuthService userAuthService, final AnonymousAuthenticationProvider anonymousAuthenticationProvider, final DataSource dataSource, final MessageSource messageSource) {
        return new XnatProviderManager(preferences, aggregator, userAuthService, anonymousAuthenticationProvider, dataSource, messageSource);
    }

    @Bean
    public XnatAuthenticationFilter customAuthenticationFilter() {
        return new XnatAuthenticationFilter();
    }

    @Bean
    public XnatBasicAuthenticationFilter customBasicAuthenticationFilter(final AuthenticationManager authenticationManager,
                                                                         final AuthenticationEntryPoint entryPoint) {
        return new XnatBasicAuthenticationFilter(authenticationManager, entryPoint);
    }

    @Bean
    public XnatExpiredPasswordFilter expiredPasswordFilter(final SiteConfigPreferences preferences, final NamedParameterJdbcTemplate jdbcTemplate, final AliasTokenService aliasTokenService, final DateValidation dateValidation) {
        return new XnatExpiredPasswordFilter(preferences, jdbcTemplate, aliasTokenService, dateValidation) {{
            setChangePasswordPath("/app/template/XDATScreen_UpdateUser.vm");
            setChangePasswordDestination("/app/action/ModifyPassword");
            setLogoutDestination("/app/action/LogoutUser");
            setLoginPath("/app/template/Login.vm");
            setLoginDestination("/app/action/XDATLoginUser");
            setInactiveAccountPath("/app/template/InactiveAccount.vm");
            setInactiveAccountDestination("/app/action/XnatInactiveAccount");
            setEmailVerificationPath("/app/template/VerifyEmail.vm");
            setEmailVerificationDestination("/data/services/sendEmailVerification");
        }};
    }

    @Bean
    public XnatInitCheckFilter xnatInitCheckFilter(final XnatAppInfo appInfo) {
        return new XnatInitCheckFilter(appInfo);
    }

    @Bean
    public XnatDatabaseUserDetailsService customDatabaseService(final XdatUserAuthService userAuthService, final DataSource dataSource) {
        return new XnatDatabaseUserDetailsService(userAuthService, dataSource);
    }

    @Bean
    public UserGroupServiceI userGroupService(final SiteConfigPreferences preferences, final GroupsAndPermissionsCache cache) {
        final String         servicePackage = StringUtils.defaultIfBlank((String) preferences.get("security.userGroupService.package"), "org.nrg.xdat.groups.custom");
        final List<Class<?>> classes        = new ArrayList<>();
        try {
            classes.addAll(Reflection.getClassesForPackage(servicePackage));
        } catch (ClassNotFoundException | IOException e) {
            log.warn("An error occurred trying to find classes in the specified user group service package: " + servicePackage);
        }
        if (!classes.isEmpty()) {
            for (final Class<?> clazz : classes) {
                if (UserGroupServiceI.class.isAssignableFrom(clazz)) {
                    try {
                        return (UserGroupServiceI) clazz.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        log.error("An error occurred trying to instantiate the class " + clazz.getName(), e);
                    }
                }
            }
        }

        // Use configured default implementation if provided
        final String className = (String) preferences.get("security.userGroupService.default");
        if (StringUtils.isNotBlank(className)) {
            try {
                return (UserGroupServiceI) Class.forName(className).newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                log.error("", e);
            }
        }

        // OK use our provided default.
        return new UserGroupManager(cache);
    }
}
