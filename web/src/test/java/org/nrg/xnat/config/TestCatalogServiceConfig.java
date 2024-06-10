package org.nrg.xnat.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import org.mockito.Mockito;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.services.NrgEventService;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.framework.services.SerializerService;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.prefs.configuration.NrgPrefsConfiguration;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.resolvers.SimplePrefsEntityResolver;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.test.utils.TestBeans;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.services.cache.UserDataCache;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.services.archive.RemoteFilesService;
import org.nrg.xnat.services.archive.impl.legacy.DefaultCatalogService;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import java.io.IOException;
import reactor.bus.EventBus;

@Configuration
@Import({OrmTestConfiguration.class, NrgPrefsConfiguration.class})
public class TestCatalogServiceConfig {
    @Bean
    public JsonNode siteMap() throws IOException {
        return TestBeans.getDefaultTestSiteMap();
    }

    @Bean
    public PreferenceEntityResolver defaultResolver(final PreferenceService service, final JsonNode siteMap) throws IOException {
        return new SimplePrefsEntityResolver(service, siteMap);
    }

    @Bean
    public EventBus eventBus() {
        return EventBus.create();
    }

    @Bean
    public NrgEventServiceI eventService(final EventBus eventBus) {
        return new NrgEventService(eventBus);
    }

    @Bean
    public SiteConfigPreferences siteConfigPreferences(final NrgPreferenceService preferenceService, final NrgEventServiceI eventService) {
        return new SiteConfigPreferences(preferenceService, eventService, null, null);
    }

    @Bean
    public CatalogService catalogService(final SiteConfigPreferences preferences,
                                         final NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                         final CacheManager cacheManager,
                                         final UserDataCache userDataCache) {
        return new DefaultCatalogService(preferences, namedParameterJdbcTemplate, cacheManager, userDataCache);
    }

    @Bean
    public DefaultCatalogService catalogServiceNoRemote(final SiteConfigPreferences preferences,
                                                        final NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                                        final CacheManager cacheManager,
                                                        final UserDataCache userDataCache) {
        // return type DefaultCatalogService so we can re-set RemoteFilesService to null
        return new DefaultCatalogService(preferences, namedParameterJdbcTemplate, cacheManager, userDataCache);
    }

    @Bean
    public RemoteFilesService remoteFilesService() {
        return Mockito.mock(RemoteFilesService.class);
    }

    @Bean
    public CacheManager cacheManager() {
        return Mockito.mock(CacheManager.class);
    }

    @Bean
    public UserDataCache userDataCache() {
        return Mockito.mock(UserDataCache.class);
    }

    @Bean
    public PermissionsServiceI permissionsService() {
        return Mockito.mock(PermissionsServiceI.class);
    }

    @Bean
    public ContextService contextService(final ApplicationContext applicationContext) {
        final ContextService contextService = new ContextService();
        contextService.setApplicationContext(applicationContext);
        return contextService;
    }

    @Bean
    public SerializerService serializerService(Jackson2ObjectMapperBuilder objectMapperBuilder)
            throws SAXNotSupportedException, SAXNotRecognizedException, ParserConfigurationException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        return new SerializerService(objectMapperBuilder, DocumentBuilderFactory.newInstance(),
                spf, TransformerFactory.newInstance(), (SAXTransformerFactory) SAXTransformerFactory.newInstance());
    }

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .failOnEmptyBeans(false)
                .featuresToEnable(JsonParser.Feature.ALLOW_SINGLE_QUOTES, JsonParser.Feature.ALLOW_YAML_COMMENTS)
                .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS, SerializationFeature.WRITE_NULL_MAP_VALUES)
                .modulesToInstall(new Hibernate4Module(), new GuavaModule());
    }
}
