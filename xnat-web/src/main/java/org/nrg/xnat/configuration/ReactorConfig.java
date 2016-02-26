package org.nrg.xnat.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import reactor.Environment;
import reactor.bus.EventBus;

/**
 * The Class ReactorConfig.
 */
@Configuration
@ComponentScan({"org.nrg.xnat.event.listeners, org.nrg.xft.event, org.nrg.xft.event.listeners"})
public class ReactorConfig {

    /**
     * Env.
     *
     * @return the environment
     */
    @Bean
    Environment env() {
        return Environment.initializeIfEmpty()
                          .assignErrorJournal();
    }

    /**
     * Creates the event bus.
     *
     * @param env the env
     * @return the event bus
     */
    @Bean
    EventBus createEventBus(Environment env) {
	    return EventBus.create(env, Environment.THREAD_POOL);
    }

}
