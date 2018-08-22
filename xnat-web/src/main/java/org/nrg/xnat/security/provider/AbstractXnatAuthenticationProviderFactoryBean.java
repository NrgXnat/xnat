package org.nrg.xnat.security.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.AbstractFactoryBean;

@Slf4j
public abstract class AbstractXnatAuthenticationProviderFactoryBean extends AbstractFactoryBean<XnatAuthenticationProvider> implements XnatAuthenticationProviderFactoryBean {
    @Override
    abstract public Class<?> getObjectType();

    @Override
    protected XnatAuthenticationProvider createInstance() throws Exception {
        return null;
    }
}
