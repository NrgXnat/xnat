package org.nrg.xnat.services.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import reactor.bus.EventBus;

@Configuration
public class UserProjectCacheTestConfig {
    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactory() {
        return new EhCacheManagerFactoryBean() {{
            setConfigLocation(new ClassPathResource("userprojectcachetests-ehcache.xml"));
            setAcceptExisting(true);
            setShared(true);
        }};
    }

    @Bean
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManagerFactory().getObject());
    }

    @Bean
    public EventBus eventBus() {
        return EventBus.create();
    }

    @Bean
    public UserProjectCache userProjectCache() {
        return new UserProjectCache(cacheManager(), eventBus());
    }
}
