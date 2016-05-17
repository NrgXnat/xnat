package org.nrg.xnat.configuration;

import org.nrg.framework.services.NrgEventService;
import org.nrg.xft.event.listeners.XftItemEventListener;
import org.nrg.xnat.event.conf.EventPackages;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.Environment;
import reactor.bus.EventBus;

import java.util.Arrays;
import java.util.HashSet;

/**
 * The Class ReactorConfig.
 */
@Configuration
public class ReactorConfig {
    @Bean
    public NrgEventService xftEventService() {
        return new NrgEventService();
    }

    @Bean
    public XftItemEventListener xftItemEventListener(final EventBus eventBus) {
        return new XftItemEventListener(eventBus);
    }

    @Bean
    public EventPackages eventPackages() {
        // NOTE:  These should be treated as parent packages.  All sub-packages should be searched
        return new EventPackages(new HashSet<>(Arrays.asList(new String[]{"org.nrg.xnat.event", "org.nrg.xft.event", "org.nrg.xdat.event"})));
    }

    /**
     * Env.
     *
     * @return the environment
     */
    @Bean
    public Environment env() {
        return Environment.initializeIfEmpty().assignErrorJournal();
    }

    /**
     * Creates the event bus.
     *
     * @param env the env
     *
     * @return the event bus
     */
    @Bean
    public EventBus createEventBus(Environment env) {
        return EventBus.create(env, Environment.THREAD_POOL);
    }
}
