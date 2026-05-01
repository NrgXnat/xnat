package org.nrg.framework.beans;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfigurableBeanConfiguration.class)
public class TestConfigurableBeanConfigs {
    @Autowired
    private ConfigurableBeanManager _beanManager;

    private static final ConfigurableBean BEAN1_INSTANCE1 = new ConfigurableBeanImpl1("Army Geddon", 333);
    private static final ConfigurableBean BEAN1_INSTANCE2 = new ConfigurableBeanImpl1("Sue Ellen", 4242);
    private static final ConfigurableBean BEAN1_INSTANCE3 = new ConfigurableBeanImpl1("Man Slaughter", 1234);
    private static final ConfigurableBean BEAN2_INSTANCE1 = new ConfigurableBeanImpl2("Flora Saint", 999);
    private static final ConfigurableBean BEAN2_INSTANCE2 = new ConfigurableBeanImpl2("Pete Zapie", 8);
    private static final ConfigurableBean BEAN2_INSTANCE3 = new ConfigurableBeanImpl2("Dell Vintooit", 521);

    @Test
    public void testConfigurableBeanManager() {
        assertThat(_beanManager).isNotNull();
        final List<ConfigurableBean> beans = _beanManager.getBeans();
        assertThat(beans).isNotNull()
                         .isNotEmpty()
                         .hasSize(6)
                         .containsExactlyInAnyOrder(BEAN1_INSTANCE1, BEAN1_INSTANCE2, BEAN1_INSTANCE3, BEAN2_INSTANCE1, BEAN2_INSTANCE2, BEAN2_INSTANCE3);
    }
}
