package org.nrg.framework.beans;

import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;

/**
 * Defines a class that can be used as a Spring configuration class that also initializes beans through a
 * set of initialization parameters (including bean name). This works around Spring's lack of means to create
 * multiple beans or services dynamically.
 */
public interface ConfigurableBeanConfiguration extends ImportBeanDefinitionRegistrar {
    /**
     * Called by the implementing class to give the configuration a chance to set the parameters for the beans
     * to be created by this configuration.
     */
    void setBeanInitializationParameters();

    /**
     * Adds the name and parameters for an instance of the bean to be created.
     *
     * @param beanName   The name of the bean. This should be unique in the application context.
     * @param parameters The parameters to pass to the bean constructor.
     */
    void addBeanInitializationParameters(final String beanName, final Object... parameters);
}
