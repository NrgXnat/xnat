package org.nrg.xnat.initialization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xnat.security.*;
import org.nrg.xnat.security.alias.AliasTokenAuthenticationProvider;
import org.nrg.xnat.security.config.AuthenticationProviderAggregator;
import org.nrg.xnat.security.config.AuthenticationProviderConfigurator;
import org.nrg.xnat.security.config.DatabaseAuthenticationProviderConfigurator;
import org.nrg.xnat.security.config.LdapAuthenticationProviderConfigurator;
import org.nrg.xnat.security.userdetailsservices.XnatDatabaseUserDetailsService;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
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

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Configuration
@EnableWebSecurity
@ImportResource("WEB-INF/conf/xnat-security.xml")
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
    public XnatAuthenticationEntryPoint loginUrlAuthenticationEntryPoint(final SiteConfigPreferences preferences) {
        final XnatAuthenticationEntryPoint entryPoint = new XnatAuthenticationEntryPoint("/app/template/Login.vm", preferences);
        entryPoint.setDataPaths(Arrays.asList("/xapi/**", "/data/**", "/REST/**", "/fs/**"));
        entryPoint.setInteractiveAgents(Arrays.asList(".*MSIE.*", ".*Mozilla.*", ".*AppleWebKit.*", ".*Opera.*"));
        return entryPoint;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public ConcurrentSessionFilter concurrencyFilter(final SessionRegistry sessionRegistry) {
        return new ConcurrentSessionFilter(sessionRegistry, "/app/template/Login.vm");
    }

    @Bean
    @Primary
    public CompositeSessionAuthenticationStrategy sas(final SessionRegistry sessionRegistry, final SiteConfigPreferences preferences) throws SiteConfigurationException {
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
    public FilterSecurityInterceptorBeanPostProcessor filterSecurityInterceptorBeanPostProcessor(final SerializerService serializer, final SiteConfigPreferences preferences) throws IOException {
        final Resource resource = RESOURCE_LOADER.getResource("classpath:META-INF/xnat/security/configured-urls.yaml");
        try (final InputStream inputStream = resource.getInputStream()) {
            final HashMap<String, ArrayList<String>>         urlMap        = serializer.deserializeYaml(inputStream, TYPE_REFERENCE);
            final FilterSecurityInterceptorBeanPostProcessor postProcessor = new FilterSecurityInterceptorBeanPostProcessor(preferences);
            postProcessor.setOpenUrls(urlMap.get("openUrls"));
            postProcessor.setAdminUrls(urlMap.get("adminUrls"));
            return postProcessor;
        }
    }

    @Bean
    public TranslatingChannelProcessingFilter channelProcessingFilter(final SiteConfigPreferences preferences) throws SiteConfigurationException {
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
    public DatabaseAuthenticationProviderConfigurator dbConfigurator(final XnatDatabaseUserDetailsService userDetailsService, final SiteConfigPreferences preferences) {
        return new DatabaseAuthenticationProviderConfigurator(userDetailsService, preferences);
    }

    @Bean
    public LdapAuthenticationProviderConfigurator ldapConfigurator(final XdatUserAuthService service, final SiteConfigPreferences preferences) {
        return new LdapAuthenticationProviderConfigurator(service, preferences);
    }

    @Bean
    public AuthenticationProviderAggregator providerAggregator(final List<AuthenticationProvider> providers, final List<AuthenticationProviderConfigurator> configurators) {
        final Map<String, AuthenticationProviderConfigurator> configuratorMap = new HashMap<>();
        for (final AuthenticationProviderConfigurator configurator : configurators) {
            configuratorMap.put(configurator.getConfiguratorId(), configurator);
        }

        return new AuthenticationProviderAggregator(providers, configuratorMap);
    }

    @Bean
    @Primary
    public XnatProviderManager customAuthenticationManager(final AuthenticationProviderAggregator aggregator, final XdatUserAuthService userAuthService, @SuppressWarnings("SpringJavaAutowiringInspection") final AnonymousAuthenticationProvider anonymousAuthenticationProvider, final DataSource dataSource) {
        return new XnatProviderManager(aggregator, userAuthService, anonymousAuthenticationProvider, dataSource);
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
    public XnatExpiredPasswordFilter expiredPasswordFilter(final SiteConfigPreferences preferences, final JdbcTemplate jdbcTemplate, final AliasTokenService aliasTokenService) {
        return new XnatExpiredPasswordFilter(preferences, jdbcTemplate, aliasTokenService) {{
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
    public XnatInitCheckFilter xnatInitCheckFilter(final SerializerService serializer, final XnatAppInfo appInfo) throws IOException {
        final Resource resource = RESOURCE_LOADER.getResource("classpath:META-INF/xnat/security/initialization-urls.yaml");
        try (final InputStream inputStream = resource.getInputStream()) {
            final XnatInitCheckFilter filter = new XnatInitCheckFilter(appInfo);
            final JsonNode            paths  = serializer.deserializeYaml(inputStream);
            filter.setConfigurationPath(paths.get("configPath").asText());
            filter.setNonAdminErrorPath(paths.get("nonAdminErrorPath").asText());
            filter.setInitializationPaths(nodeToList(paths.get("initPaths")));
            filter.setExemptedPaths(nodeToList(paths.get("exemptedPaths")));
            return filter;
        }
    }

    @Bean
    public XnatDatabaseUserDetailsService customDatabaseService(final XdatUserAuthService userAuthService, final DataSource dataSource) {
        return new XnatDatabaseUserDetailsService(userAuthService, dataSource);
    }

    protected List<String> nodeToList(final JsonNode node) {
        final List<String> list = new ArrayList<>();
        if (node.isArray()) {
            final ArrayNode arrayNode = (ArrayNode) node;
            for (final JsonNode item : arrayNode) {
                list.add(item.asText());
            }
        } else if (node.isTextual()) {
            list.add(node.asText());
        } else {
            list.add(node.toString());
        }
        return list;
    }

    private static final ResourceLoader                                    RESOURCE_LOADER = new DefaultResourceLoader();
    private static final TypeReference<HashMap<String, ArrayList<String>>> TYPE_REFERENCE  = new TypeReference<HashMap<String, ArrayList<String>>>() {
    };
}