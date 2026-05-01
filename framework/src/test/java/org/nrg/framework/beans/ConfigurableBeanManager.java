package org.nrg.framework.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigurableBeanManager {
    @Autowired
    public ConfigurableBeanManager(final List<ConfigurableBean> beans) {
        _beans = beans;
    }

    public List<ConfigurableBean> getBeans() {
        return _beans;
    }

    private final List<ConfigurableBean> _beans;
}
