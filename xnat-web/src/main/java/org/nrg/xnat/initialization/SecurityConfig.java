/*
 * web: org.nrg.xnat.initialization.SecurityConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static org.nrg.xdat.security.helpers.Users.DEFAULT_GUEST_USERNAME;
import static org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xnat.security.*;
import org.nrg.xnat.security.preferences.SecurityPreferences;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.nrg.xnat.security.provider.XnatDatabaseAuthenticationProvider;
import org.nrg.xnat.security.provider.XnatMulticonfigAuthenticationProvider;
import org.nrg.xnat.security.userdetailsservices.XnatDatabaseUserDetailsService;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.services.validation.DateValidation;
import org.nrg.xnat.utils.DefaultInteractiveAgentDetector;
import org.nrg.xnat.utils.InteractiveAgentDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.ReflectionSaltSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.ChannelAttributeFactory;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.access.channel.ChannelDecisionManagerImpl;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.access.channel.InsecureChannelProcessor;
import org.springframework.security.web.access.channel.SecureChannelProcessor;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.session.SimpleRedirectSessionInformationExpiredStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.RequestContextFilter;

import java.util.*;
import javax.servlet.SessionCookieConfig;
import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@ComponentScan({"org.nrg.xnat.security.alias", "org.nrg.xnat.security.preferences", "org.nrg.xnat.security.provider"})
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    public SecurityConfig(final SiteConfigPreferences preferences, final XnatAppInfo appInfo, final AliasTokenService aliasTokenService, final XdatUserAuthService userAuthService, final DateValidation dateValidation, final MessageSource messageSource, final NamedParameterJdbcTemplate template, final DataSource dataSource, final SecurityPreferences securityPreferences) {
        _preferences = preferences;
        _appInfo = appInfo;
        _aliasTokenService = aliasTokenService;
        _userAuthService = userAuthService;
        _dateValidation = dateValidation;
        _template = template;
        _dataSource = dataSource;
        _securityPreferences = securityPreferences;

        _dbAuthProviderName = messageSource.getMessage("authProviders.localdb.defaults.name", EMPTY_OBJECT_ARRAY, "Database", Locale.getDefault());
    }

    @Autowired
    public void setAuthenticationProviders(final List<AuthenticationProvider> providers) {
        final List<AuthenticationProvider> expanded = expand(providers);
        if (!containsDbAuthProvider(expanded)) {
            _providers.add(xnatDatabaseAuthenticationProvider());
        }
        _providers.addAll(expanded);
    }

    @Autowired(required = false)
    public void setXnatSecurityExtensions(final List<XnatSecurityExtension> extensions) {
        _extensions.addAll(extensions);
    }

    @Bean
    public XnatProviderManager customAuthenticationManager() {
        return new XnatProviderManager(_preferences, eventPublisher(), _userAuthService, _providers);
    }

    @Bean
    public AuthenticationEventPublisher eventPublisher() {
        return new XnatAuthenticationEventPublisher(_userAuthService, _preferences, _providers);
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return new XnatDatabaseUserDetailsService(_dataSource);
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
    public InteractiveAgentDetector interactiveAgentDetector() {
        return new DefaultInteractiveAgentDetector(_preferences);
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
    @Primary
    public CompositeSessionAuthenticationStrategy sessionAuthenticationStrategy() {
        final SessionRegistry                                sessionRegistry                                = sessionRegistry();
        final SessionFixationProtectionStrategy              sessionFixationProtectionStrategy              = new SessionFixationProtectionStrategy();
        final RegisterSessionAuthenticationStrategy          registerSessionAuthenticationStrategy          = new RegisterSessionAuthenticationStrategy(sessionRegistry);
        final ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlAuthenticationStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);
        concurrentSessionControlAuthenticationStrategy.setMaximumSessions(_preferences.getConcurrentMaxSessions());
        concurrentSessionControlAuthenticationStrategy.setExceptionIfMaximumExceeded(true);
        return new CompositeSessionAuthenticationStrategy(Arrays.asList(sessionFixationProtectionStrategy, registerSessionAuthenticationStrategy, concurrentSessionControlAuthenticationStrategy));
    }

    @Bean
    public XnatLogoutSuccessHandler logoutSuccessHandler() {
        return new XnatLogoutSuccessHandler(_preferences.getRequireLogin(), "/", "/app/template/Login.vm");
    }

    @Bean
    public XnatAuthenticationFilter customAuthenticationFilter() {
        return new XnatAuthenticationFilter();
    }

    @Bean
    public XnatExpiredPasswordFilter expiredPasswordFilter(final SiteConfigPreferences preferences, final NamedParameterJdbcTemplate jdbcTemplate, final AliasTokenService aliasTokenService, final DateValidation dateValidation) {
        XnatExpiredPasswordFilter filter =  new XnatExpiredPasswordFilter(preferences, jdbcTemplate, aliasTokenService, dateValidation);
        filter.setChangePasswordPath("/app/template/XDATScreen_UpdateUser.vm");
        filter.setChangePasswordDestination("/app/action/ModifyPassword");
        filter.setLogoutDestination("/app/action/LogoutUser");
        filter.setLoginPath("/app/template/Login.vm");
        filter.setLoginDestination("/app/action/XDATLoginUser");
        filter.setInactiveAccountPath("/app/template/InactiveAccount.vm");
        filter.setInactiveAccountDestination("/app/action/XnatInactiveAccount");
        filter.setEmailVerificationPath("/app/template/VerifyEmail.vm");
        filter.setEmailVerificationDestination("/data/services/sendEmailVerification");
        return filter;
    }

    @Bean
    public ChannelProcessingFilter channelProcessingFilter() {
        final ChannelDecisionManagerImpl decisionManager = new ChannelDecisionManagerImpl();
        decisionManager.setChannelProcessors(Arrays.asList(new SecureChannelProcessor(), new InsecureChannelProcessor()));

        final LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> map = new LinkedHashMap<>();
        map.put(new AntPathRequestMatcher("/**"), ChannelAttributeFactory.createChannelAttributes(_preferences.getSecurityChannel()));

        final ChannelProcessingFilter filter = new ChannelProcessingFilter();
        filter.setChannelDecisionManager(decisionManager);
        filter.setSecurityMetadataSource(new DefaultFilterInvocationSecurityMetadataSource(map));

        return filter;
    }

    @Bean
    public XnatInitCheckFilter xnatInitCheckFilter(final XnatAppInfo appInfo) {
        return new XnatInitCheckFilter(appInfo);
    }

    @Bean
    public AuthenticationProvider xnatDatabaseAuthenticationProvider() {
        final ReflectionSaltSource saltSource = new ReflectionSaltSource();
        saltSource.setUserPropertyToUse("salt");

        final XnatDatabaseAuthenticationProvider sha2DatabaseAuthProvider = new XnatDatabaseAuthenticationProvider(_dbAuthProviderName, _aliasTokenService);
        sha2DatabaseAuthProvider.setUserDetailsService(userDetailsService());
        sha2DatabaseAuthProvider.setPasswordEncoder(Users.getEncoder());
        sha2DatabaseAuthProvider.setSaltSource(saltSource);
        return sha2DatabaseAuthProvider;
    }

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder builder) throws Exception {
        if (builder == null) {
            return;
        }

        final AuthenticationProvider dbAuthProvider = xnatDatabaseAuthenticationProvider();
        builder.parentAuthenticationManager(customAuthenticationManager());
        builder.authenticationProvider(dbAuthProvider);

        for (final AuthenticationProvider provider : _providers) {
            if (!provider.equals(dbAuthProvider)) {
                builder.authenticationProvider(provider);
            }
        }

        if (!_extensions.isEmpty()) {
            for (final XnatSecurityExtension extension : _extensions) {
                log.info("Now processing the security extension {} for authentication manager configuration", extension.getAuthMethod());
                extension.configure(builder);
            }
        }
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        // Set whether session cookie should be set to secure only based on the site URL. This can only be done during application start-up, so
        // changing to the site URL to use https won't change the secure setting until the application has been restarted. Cookies should ALWAYS
        // be http-only so we can just set that now and be done with it.
        final String  siteUrl              = _template.queryForObject(QUERY_SITE_URL, EmptySqlParameterSource.INSTANCE, String.class);
        final boolean isSecure             = StringUtils.startsWithIgnoreCase(siteUrl, "https");
        final boolean allowInsecureCookies = _securityPreferences.getAllowInsecureCookies();
        if (allowInsecureCookies) {
            log.info("Found site URL '{}' (empty string indicates uninitialized site URL), protocol indicates session cookie configuration {} be set to secure, allowInsecureCookies is true so setting to false regardless", siteUrl, isSecure ? "should" : "should not");
        } else {
            log.info("Found site URL '{}' (empty string indicates uninitialized site URL), setting session cookie configuration set to {}", siteUrl, isSecure);
        }

        final SessionCookieConfig config = XnatWebAppInitializer.getServletContext().getSessionCookieConfig();
        config.setSecure(isSecure && !allowInsecureCookies);
        config.setHttpOnly(true);

        // This is basically what super.configure() does, minus httpBasic().
        http.authorizeRequests().anyRequest().authenticated().and().formLogin();

        final InteractiveAgentDetector     detector                 = interactiveAgentDetector();
        final XnatAuthenticationEntryPoint authenticationEntryPoint = loginUrlAuthenticationEntryPoint(_preferences, detector);

        http.apply(new XnatBasicAuthConfigurer<>(authenticationEntryPoint, _aliasTokenService));

        http.sessionManagement()
            .sessionCreationPolicy(IF_REQUIRED)
            .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
            .maximumSessions(_preferences.getConcurrentMaxSessions())
            .maxSessionsPreventsLogin(true)
            .sessionRegistry(sessionRegistry())
            .expiredSessionStrategy(new SimpleRedirectSessionInformationExpiredStrategy("/app/template/Login.vm", redirectStrategy(_preferences, detector)));

        http.headers().frameOptions().sameOrigin().cacheControl().disable().contentSecurityPolicy("frame-ancestors 'self'")
            .and().httpStrictTransportSecurity().disable()
            .and().exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
            .and().csrf().disable()
            .anonymous().key(Users.ANONYMOUS_AUTH_PROVIDER_KEY).principal(DEFAULT_GUEST_USERNAME)
            .and().logout().invalidateHttpSession(true).logoutSuccessHandler(logoutSuccessHandler()).logoutUrl("/app/action/LogoutUser");

        // If we can get the default channel processing filter as a bean, we could remove this.
        http.addFilter(channelProcessingFilter())
            .addFilterAfter(new RequestContextFilter(), ChannelProcessingFilter.class)
            .addFilterBefore(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(xnatInitCheckFilter(_appInfo), RememberMeAuthenticationFilter.class)
            .addFilterAfter(expiredPasswordFilter(_preferences, _template, _aliasTokenService, _dateValidation), BasicAuthenticationFilter.class);

        if (_extensions.size() > 0) {
            for (final XnatSecurityExtension extension : _extensions) {
                log.info("Now processing the security extension {} for HTTP security configuration", extension.getAuthMethod());
                extension.configure(http);
            }
        }
    }

    @Bean
    public RedirectStrategy redirectStrategy(final SiteConfigPreferences preferences, final InteractiveAgentDetector detector) {
        return new XnatRedirectStrategy(preferences, detector);
    }

    private boolean containsDbAuthProvider(final List<AuthenticationProvider> providers) {
        return providers.stream().anyMatch(provider -> provider instanceof XnatAuthenticationProvider && StringUtils.equals(_dbAuthProviderName, ((XnatAuthenticationProvider) provider).getName()));
    }

    private static List<AuthenticationProvider> expand(final List<AuthenticationProvider> providers) {
        final List<AuthenticationProvider> expanded = new ArrayList<>();
        for (final AuthenticationProvider provider : providers) {
            if (XnatMulticonfigAuthenticationProvider.class.isAssignableFrom(provider.getClass())) {
                expanded.add(provider);
                expanded.addAll(((XnatMulticonfigAuthenticationProvider) provider).getProviders());
            } else {
                expanded.add(provider);
            }
        }
        return expanded;
    }

    private static final String QUERY_SITE_URL = "SELECT coalesce(value, '') FROM xhbm_preference p LEFT JOIN xhbm_tool t ON p.tool = t.id WHERE t.tool_id = 'siteConfig' AND p.name = 'siteUrl'";

    private final SiteConfigPreferences      _preferences;
    private final XnatAppInfo                _appInfo;
    private final AliasTokenService          _aliasTokenService;
    private final XdatUserAuthService        _userAuthService;
    private final DateValidation             _dateValidation;
    private final NamedParameterJdbcTemplate _template;
    private final DataSource                 _dataSource;
    private final SecurityPreferences        _securityPreferences;
    private final String                     _dbAuthProviderName;

    private final List<AuthenticationProvider> _providers  = new ArrayList<>();
    private final List<XnatSecurityExtension>  _extensions = new ArrayList<>();
}
