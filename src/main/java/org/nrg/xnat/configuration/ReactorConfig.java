/*
 * web: org.nrg.xnat.configuration.ReactorConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.services.DataTypeAwareEventService;
import org.nrg.xft.event.listeners.XftItemEventHandler;
import org.nrg.xnat.event.XnatEventService;
import org.nrg.xnat.event.util.UncaughtExceptionHandler;
import org.nrg.xnat.preferences.AsyncOperationsPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import reactor.Environment;
import reactor.bus.EventBus;
import reactor.core.Dispatcher;
import reactor.core.dispatch.WorkQueueDispatcher;

/**
 * The Class ReactorConfig.
 */
@Configuration
@ComponentScan("org.nrg.xft.event.methods")
@Slf4j
public class ReactorConfig {
    private final AsyncOperationsPreferences preferences;
    public static final String REACTOR_DISPATCHER_THREAD_FACTORY = "reactorDispatcher";

    @Autowired
    public ReactorConfig(AsyncOperationsPreferences preferences) {
        this.preferences = preferences;
    }

    @Bean
    public DataTypeAwareEventService eventService(final EventBus eventBus) {
        return new XnatEventService(eventBus);
    }

    @Bean
    public XftItemEventHandler xftItemEventHandler(final EventBus eventBus) {
        return new XftItemEventHandler(eventBus);
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
        return EventBus.create(env, reactorDispatcher());
    }

    @Bean
    public Dispatcher reactorDispatcher() {
        Double ringBufferSize = Math.pow(2, preferences.getReactorWorkQueueDispatcherRingBufferSizePower());
        return new WorkQueueDispatcher(REACTOR_DISPATCHER_THREAD_FACTORY,
                preferences.getReactorWorkQueueDispatcherPoolSize(),
                ringBufferSize.intValue(),
                new UncaughtExceptionHandler());
    }
}
