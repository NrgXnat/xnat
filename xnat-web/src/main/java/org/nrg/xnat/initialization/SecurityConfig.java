/*
 * web: org.nrg.xnat.initialization.SecurityConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization;

import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xnat.security.*;
import org.nrg.xnat.security.alias.AliasTokenAuthenticationProvider;
import org.nrg.xnat.security.config.AuthenticationProviderAggregator;
import org.nrg.xnat.security.config.AuthenticationProviderConfigurator;
import org.nrg.xnat.security.config.DatabaseAuthenticationProviderConfigurator;
import org.nrg.xnat.security.userdetailsservices.XnatDatabaseUserDetailsService;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.services.validation.DateValidation;
import org.nrg.xnat.utils.DefaultInteractiveAgentDetector;
import org.nrg.xnat.utils.InteractiveAgentDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.channel.ChannelDecisionManagerImpl;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.access.channel.InsecureChannelProcessor;
import org.springframework.security.web.access.channel.SecureChannelProcessor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.SimpleRedirectSessionInformationExpiredStrategy;

import javax.sql.DataSource;
import java.util.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    public SecurityConfig(final SiteConfigPreferences preferences, final XnatAppInfo appInfo, final JdbcTemplate template, final AliasTokenService aliasTokenService, final DateValidation dateValidation) {
        _preferences = preferences;
        _appInfo = appInfo;
        _template = template;
        _aliasTokenService = aliasTokenService;
        _dateValidation = dateValidation;
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        final XnatAuthenticationEntryPoint authenticationEntryPoint = loginUrlAuthenticationEntryPoint(_preferences, interactiveAgentDetector(_preferences));
        http.headers().frameOptions().sameOrigin()
            .httpStrictTransportSecurity().disable()
            .contentSecurityPolicy("frame-ancestors 'self'");
        http.httpBasic().authenticationEntryPoint(authenticationEntryPoint);
        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);
        http.csrf().disable();
        http.sessionManagement().sessionAuthenticationStrategy(sessionAuthenticationStrategy(sessionRegistry(), _preferences));
        http.addFilterAt(channelProcessingFilter(_preferences), ChannelProcessingFilter.class)
            .addFilterBefore(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(customBasicAuthenticationFilter(authenticationManager(), authenticationEntryPoint), BasicAuthenticationFilter.class)
            .addFilterBefore(xnatInitCheckFilter(_appInfo), RememberMeAuthenticationFilter.class)
            .addFilterAfter(expiredPasswordFilter(_preferences, _template, _aliasTokenService, _dateValidation), SecurityContextPersistenceFilter.class)
            .addFilterAt(concurrencyFilter(sessionRegistry()), ConcurrentSessionFilter.class)
            .addFilterAt(logoutFilter(sessionRegistry()), LogoutFilter.class);
    }

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
    public CompositeSessionAuthenticationStrategy sessionAuthenticationStrategy(final SessionRegistry sessionRegistry, final SiteConfigPreferences preferences) {
        return new CompositeSessionAuthenticationStrategy(Arrays.asList(new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry) {{
                                                                            setMaximumSessions(preferences.getConcurrentMaxSessions());
                                                                            setExceptionIfMaximumExceeded(true);
                                                                        }},
                                                                        new SessionFixationProtectionStrategy(),
                                                                        new RegisterSessionAuthenticationStrategy(sessionRegistry)));
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
    public XnatProviderManager customAuthenticationManager(final SiteConfigPreferences preferences, final AuthenticationProviderAggregator aggregator, final XdatUserAuthService userAuthService) {
        return new XnatProviderManager(preferences, userAuthService, aggregator);
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
    public XnatExpiredPasswordFilter expiredPasswordFilter(final SiteConfigPreferences preferences, final JdbcTemplate jdbcTemplate, final AliasTokenService aliasTokenService, final DateValidation dateValidation) {
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

    private final SiteConfigPreferences _preferences;
    private final XnatAppInfo           _appInfo;
    private final JdbcTemplate          _template;
    private final AliasTokenService     _aliasTokenService;
    private final DateValidation        _dateValidation;
}
