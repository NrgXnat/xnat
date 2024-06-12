package org.nrg.framework.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

@Configuration
@Import({TestConfigurableBeanImpl1Configuration.class, TestConfigurableBeanImpl2Configuration.class})
public class TestConfigurableBeanConfiguration {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public ConfigurableBeanManager beanManager(final List<ConfigurableBean> beans) {
        return new ConfigurableBeanManager(beans);
    }
}
