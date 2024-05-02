package org.nrg.xnat.configuration;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import org.nrg.xnat.micrometer.utils.MetricsUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micrometer.core.instrument.binder.tomcat.TomcatMetrics;
import java.lang.reflect.Field;
import org.springframework.core.env.Environment;

import javax.servlet.ServletContext;
import java.util.concurrent.ExecutorService;

import java.util.Collections;

import static org.nrg.xnat.services.XnatAppInfo.PROPERTY_XNAT_METRICS_ENABLED;

// Also see  org.nrg.xnat.initialization.tasks.SetupDatabaseMetrics

@Configuration
@Slf4j
public class MicrometerConfiguration {


    @Bean
    public MeterRegistry meterRegistry(final ExecutorService executorService, final ServletContext servletContext, final Environment environment) {
        MeterRegistry meterRegistry =  new CompositeMeterRegistry();

        if (MetricsUtils.isMetricsEnabled(environment)) {
            meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        }
        new ClassLoaderMetrics().bindTo(meterRegistry);
        new JvmMemoryMetrics().bindTo(meterRegistry);
        new JvmGcMetrics().bindTo(meterRegistry);
        new UptimeMetrics().bindTo(meterRegistry);
        new ProcessorMetrics().bindTo(meterRegistry);
        new JvmThreadMetrics().bindTo(meterRegistry);
        new JvmHeapPressureMetrics().bindTo(meterRegistry);
        new JvmCompilationMetrics().bindTo(meterRegistry);
        new ExecutorServiceMetrics(executorService, "executorService", Collections.emptyList()).bindTo(meterRegistry);
        new FileDescriptorMetrics().bindTo(meterRegistry);
        return meterRegistry;
    }

    @Bean
    public TimedAspect timedAspect(final MeterRegistry registry) {
        return new TimedAspect(registry);
    }



}
