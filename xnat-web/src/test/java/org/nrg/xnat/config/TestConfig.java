package org.nrg.xnat.config;

import org.mockito.Mockito;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.services.archive.RemoteFilesService;
import org.nrg.xnat.services.archive.impl.legacy.DefaultCatalogService;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class TestConfig {
    @Bean
    public CatalogService catalogService(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                         CacheManager cacheManager) {
        return new DefaultCatalogService(namedParameterJdbcTemplate, cacheManager);
    }

    @Bean
    public DefaultCatalogService catalogServiceNoRemote(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                                        CacheManager cacheManager) {
        // return type DefaultCatalogService so we can re-set RemoteFilesService to null
        return new DefaultCatalogService(namedParameterJdbcTemplate, cacheManager);
    }


    @Bean
    public RemoteFilesService remoteFilesService() {
        return Mockito.mock(RemoteFilesService.class);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
        return Mockito.mock(NamedParameterJdbcTemplate.class);
    }
    @Bean
    public CacheManager cacheManager() {
        return Mockito.mock(CacheManager.class);
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
}
