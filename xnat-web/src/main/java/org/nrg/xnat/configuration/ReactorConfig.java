/*
 * web: org.nrg.xnat.configuration.ReactorConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import org.nrg.framework.services.NrgEventService;
import org.nrg.xft.event.listeners.XftItemEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.Environment;
import reactor.bus.EventBus;

/**
 * The Class ReactorConfig.
 */
@Configuration
public class ReactorConfig {
    @Bean
    public NrgEventService eventService(final EventBus eventBus) {
        return new NrgEventService(eventBus);
    }

    @Bean
    public XftItemEventListener xftItemEventListener(final EventBus eventBus) {
        return new XftItemEventListener(eventBus);
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
