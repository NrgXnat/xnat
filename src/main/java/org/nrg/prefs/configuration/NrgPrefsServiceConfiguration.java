package org.nrg.prefs.configuration;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.prefs.annotations.NrgPreferencesBean;
import org.nrg.prefs.beans.PreferencesBean;
import org.nrg.prefs.transformers.PreferenceTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.*;

@Configuration
@ComponentScan({"org.nrg.prefs.services.impl.hibernate", "org.nrg.prefs.repositories"})
public class NrgPrefsServiceConfiguration implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware, InitializingBean {
    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
        findPreferencesProperties();
        for (final String id : _clazzes.keySet()) {
            BeanDefinition beanDefinition = new RootBeanDefinition(_clazzes.get(id), Autowire.BY_TYPE.value(), true);
            // beanDefinition.setLazyInit(true);
            registry.registerBeanDefinition(id, beanDefinition);
        }
    }

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory factory) throws BeansException {

    }

    @Bean
    public HibernateEntityPackageList nrgPrefsHibernateEntityPackageList() {
        return new HibernateEntityPackageList("org.nrg.prefs.entities");
    }

    private void findPreferencesProperties() {
        try {
            final PathMatchingResourcePatternResolver resolver  = new PathMatchingResourcePatternResolver();
            final Resource[]                          resources = resolver.getResources("classpath*:META-INF/xnat/**/*-preferences.properties");
            for (final Resource resource : resources) {
                final Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                final String toolId = properties.getProperty("toolId");
                final String toolClassName = properties.getProperty("toolClass");
                try {
                    Class<?> toolClass = Class.forName(toolClassName);
                    if (PreferencesBean.class.isAssignableFrom(toolClass)) {
                        _clazzes.put(toolId, toolClass);
                    } else {
                        _log.error("The class {} was specified as a preferences bean class, but does not implement the {} interface.", toolClassName, PreferencesBean.class.getName());
                    }
                } catch (ClassNotFoundException e) {
                    _log.error("Couldn't find class specified for a preferences bean: " + toolClassName);
                }
                final String transformers = properties.getProperty("transformers");
                if (StringUtils.isNotBlank(transformers)) {
                    for (final String transformer : transformers.split(",")) {
                        try {
                            Class<?> transformerClass = Class.forName(transformer);
                            if (PreferenceTransformer.class.isAssignableFrom(transformerClass)) {
                                _clazzes.put(transformerClass.getSimpleName(), transformerClass);
                            } else {
                                _log.error("The class {} was specified as a preferences transformer class, but does not implement the {} interface.", transformer, PreferenceTransformer.class);
                            }
                        } catch (ClassNotFoundException e) {
                            _log.error("Couldn't find class specified for a preferences bean transformer: " + transformer);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred trying to locate XNAT preferences definitions.");
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(NrgPrefsServiceConfiguration.class);

    private final Map<String, Class<?>> _clazzes = new HashMap<>();

    private ApplicationContext _context;

    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        _context = context;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final String[] beans = _context.getBeanNamesForAnnotation(NrgPreferencesBean.class);
        _log.debug("Found {} beans with the NrgPreferencesBean annotation.", beans == null ? 0 : beans.length);
    }
}
