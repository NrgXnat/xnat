package org.nrg.framework.beans;

import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfigurableBeanImpl1Configuration extends AbstractConfigurableBeanConfiguration<ConfigurableBeanImpl1> {
    public TestConfigurableBeanImpl1Configuration() {
        super(ConfigurableBean.class);
    }

    @Override
    public void setBeanInitializationParameters() {
        addBeanInitializationParameters("configurableBeanImpl1Instance1", "Army Geddon", 333);
        addBeanInitializationParameters("configurableBeanImpl1Instance2", "Sue Ellen", 4242);
        addBeanInitializationParameters("configurableBeanImpl1Instance3", "Man Slaughter", 1234);
    }
}
