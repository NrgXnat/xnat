package org.nrg.framework.services;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestContextServiceConfiguration {
    public static final String SINGLETON = "Singleton";
    public static final String SERVICE_1 = "Service 1";
    public static final String SERVICE_2 = "Service 2";
    public static final String SERVICE_3 = "Service 3";
    public static final String MANY_IMPL = "Many Impl";

    @Bean
    public ContextService contextService() {
        return new ContextService();
    }

    @Bean
    public SingletonService singletonService() {
        return new SingletonServiceImpl(SINGLETON);
    }

    @Bean
    public StandInService standInService1() {
        return new StandInServiceImpl(SERVICE_1);
    }

    @Bean
    public StandInService standInService2() {
        return new StandInServiceImpl(SERVICE_2);
    }

    @Bean
    public StandInService standInService3() {
        return new StandInServiceImpl(SERVICE_3);
    }

    @Bean
    public ManyImplServiceImpl manyImplService() {
        return new ManyImplServiceImpl(MANY_IMPL);
    }
}
