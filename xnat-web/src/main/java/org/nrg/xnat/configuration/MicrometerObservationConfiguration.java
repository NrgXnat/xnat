package org.nrg.xnat.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.micrometer.web.handler.XnatMicrometerHandlerInterceptorAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;

@Configuration
@Slf4j
public class MicrometerObservationConfiguration {


    @Autowired
    public MicrometerObservationConfiguration(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Bean
    ObservationRegistry observationRegistry() {
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        observationRegistry.observationConfig().observationHandler(new DefaultMeterObservationHandler(meterRegistry));
        return observationRegistry;
    }

    @Bean
    ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }

    @Bean
    public XnatMicrometerHandlerInterceptorAdapter xnatMicrometerHandlerInterceptorAdapter(final ObservationRegistry observationRegistry) {
        return new XnatMicrometerHandlerInterceptorAdapter(observationRegistry);
    }

    private final MeterRegistry meterRegistry;

}
