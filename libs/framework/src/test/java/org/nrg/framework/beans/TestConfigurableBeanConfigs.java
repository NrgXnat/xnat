package org.nrg.framework.beans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
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
        assertNotNull(_beanManager);
        final List<ConfigurableBean> beans = _beanManager.getBeans();
        assertNotNull(beans);
        assertEquals(6, beans.size());
        assertTrue(beans.contains(BEAN1_INSTANCE1));
        assertTrue(beans.contains(BEAN1_INSTANCE2));
        assertTrue(beans.contains(BEAN1_INSTANCE3));
        assertTrue(beans.contains(BEAN2_INSTANCE1));
        assertTrue(beans.contains(BEAN2_INSTANCE2));
        assertTrue(beans.contains(BEAN2_INSTANCE3));
    }
}
