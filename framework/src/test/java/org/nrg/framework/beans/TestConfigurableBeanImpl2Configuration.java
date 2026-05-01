package org.nrg.framework.beans;

import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfigurableBeanImpl2Configuration extends AbstractConfigurableBeanConfiguration<ConfigurableBeanImpl2> {
    public TestConfigurableBeanImpl2Configuration() {
        super(ConfigurableBean.class);
    }

    @Override
    public void setBeanInitializationParameters() {
        addBeanInitializationParameters("configurableBeanImpl2Instance1", "Flora Saint", 999);
        addBeanInitializationParameters("configurableBeanImpl2Instance2", "Pete Zapie", 8);
        addBeanInitializationParameters("configurableBeanImpl2Instance3", "Dell Vintooit", 521);
    }
}
