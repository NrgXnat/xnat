/*
 * framework: org.nrg.framework.services.ContextService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services;

import org.apache.commons.lang3.ArrayUtils;
import org.nrg.framework.exceptions.NotFoundException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.servlet.ServletContext;

@Service
public class ContextService implements NrgService, ApplicationContextAware, ServletContextAware {
    private static ApplicationContext _applicationContext;

    private ServletContext _servletContext;

    /**
     * Returns the existing instance of the ContextService.
     *
     * @return The existing instance of the ContextService.
     */
    public static ContextService getInstance() {
        if (_applicationContext != null) {
            return _applicationContext.getBean(ContextService.class);
        }
        return new ContextService();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        _applicationContext = applicationContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServletContext(final ServletContext servletContext) {
        _servletContext = servletContext;
    }

    /**
     * Handles updates to the application context.
     *
     * @param event The context refreshed event. This adds the new context to the contexts available to this service.
     */
    @EventListener
    public void handleContextRefreshedEvent(final ContextRefreshedEvent event) {
        setApplicationContext(event.getApplicationContext());
    }

    /**
     * Gets a bean with the indicated name. If no bean with that name is found, this method throws {@link
     * NoSuchBeanDefinitionException}.
     *
     * @param name The name of the bean to be retrieved.
     *
     * @return An object from the context.
     *
     * @throws NoSuchBeanDefinitionException When a bean of the indicated type can't be found.
     */
    public Object getBean(final String name) throws NoSuchBeanDefinitionException {
        return _applicationContext == null ? null : _applicationContext.getBean(name);
    }

    /**
     * Gets a bean of the indicated type. If no bean of that type is found, this method throws {@link
     * NoSuchBeanDefinitionException}.
     *
     * @param <T>  The type of the bean to be retrieved.
     * @param type The class of the bean to be retrieved.
     *
     * @return An object of the type.
     *
     * @throws NoSuchBeanDefinitionException When a bean of the indicated type can't be found.
     */
    public <T> T getBean(final Class<T> type) throws NoSuchBeanDefinitionException {
        return _applicationContext == null ? null : _applicationContext.getBean(type);
    }

    /**
     * Gets the bean with the indicated name and type.
     *
     * @param <T>  The type of the bean to be retrieved.
     * @param name The name of the bean to be retrieved.
     * @param type The class of the bean to be retrieved.
     *
     * @return An object of the type.
     *
     * @throws NoSuchBeanDefinitionException When a bean of the indicated type can't be found.
     */
    public <T> T getBean(final String name, final Class<T> type) throws NoSuchBeanDefinitionException {
        return _applicationContext == null ? null : _applicationContext.getBean(name, type);
    }

    /**
     * Gets a bean of the indicated type. If no bean of that type is found, null is returned.
     *
     * @param <T>  The type of the bean to be retrieved.
     * @param type The class of the bean to be retrieved.
     *
     * @return An object of the type.
     */
    public <T> T getBeanSafely(final Class<T> type) {
        try {
            return getBean(type);
        } catch (NoSuchBeanDefinitionException ignored) {
            // This is OK, just means the bean doesn't exist in the current context. Carry on.
        }
        // If we didn't find a valid bean of the type, return null.
        return null;
    }

    /**
     * Gets a bean of the indicated type. If no bean of that type is found and no suppliers
     * are specified, null is returned. If one or more suppliers are specified, this method
     * calls each supplier in turn until one returns a non-null value or there are no more
     * suppliers. If no supplier returns a non-null value, this method then returns null.
     *
     * @param <T>       The type of the bean to be retrieved.
     * @param type      The class of the bean to be retrieved.
     * @param suppliers One or more suppliers to be called if the initial attempt fails.
     *
     * @return A bean with the specified type or null if none can be found.
     */
    @SafeVarargs
    public final <T> T getBeanSafely(final Class<T> type, final Supplier<T> first, final Supplier<T>... suppliers) {
        try {
            return getBean(type);
        } catch (NoSuchBeanDefinitionException ignored) {
            // This is OK, just means the bean doesn't exist in the current context. Carry on.
        }
        // If we didn't find a valid bean of the type, call the supplier or return null.
        return callSuppliers(first, suppliers);
    }

    /**
     * Gets the bean with the indicated name and type. If no bean with that name and type is found, null is returned.
     *
     * @param <T>  The type of the bean to be retrieved.
     * @param name The name of the bean to be retrieved.
     * @param type The class of the bean to be retrieved.
     *
     * @return An object of the type.
     */
    public <T> T getBeanSafely(final String name, final Class<T> type) {
        try {
            return getBean(name, type);
        } catch (NoSuchBeanDefinitionException ignored) {
            // This is OK, just means the bean doesn't exist in the current context. Carry on.
        }
        // If we didn't find a valid bean of the type, return null.
        return null;
    }

    /**
     * Gets a bean with the indicated name and type. If no bean with that name and type is
     * found and no suppliers are specified, null is returned. If one or more suppliers are
     * specified, this method calls each supplier in turn until one returns a non-null value
     * or there are no more suppliers. If no supplier returns a non-null value, this method
     * then returns null.
     *
     * @param <T>       The type of the bean to be retrieved.
     * @param name      The name of the bean to be retrieved.
     * @param type      The class of the bean to be retrieved.
     * @param suppliers One or more suppliers to be called if the initial attempt fails.
     *
     * @return A bean with the specified name and type or null if none can be found.
     */
    @SafeVarargs
    public final <T> T getBeanSafely(final String name, final Class<T> type, final Supplier<T> first, final Supplier<T>... suppliers) {
        try {
            return getBean(name, type);
        } catch (NoSuchBeanDefinitionException ignored) {
            // This is OK, just means the bean doesn't exist in the current context. Carry on.
        }
        // If we didn't find a valid bean of the type, call the supplier or return null.
        return callSuppliers(first, suppliers);
    }

    /**
     * Gets all beans with the indicated type.
     *
     * @param type The class of the bean to be retrieved.
     * @param <T>  The parameterized class of the bean to be retrieved.
     *
     * @return An object of the type.
     */
    public <T> Map<String, T> getBeansOfType(final Class<T> type) {
        if (_applicationContext == null) {
            return null;
        }
        final BeanFactory beanFactory = _applicationContext.getParentBeanFactory();
        final Map<String, T> candidate = beanFactory != null && ListableBeanFactory.class.isAssignableFrom(beanFactory.getClass()) ? ((ListableBeanFactory) beanFactory).getBeansOfType(type) : _applicationContext.getBeansOfType(type);
        return !candidate.isEmpty() ? candidate : new HashMap<>();
    }

    public URI getConfigurationLocation(final String configuration) throws NotFoundException {
        return getAppRelativeLocation("WEB-INF", "conf", configuration);
    }

    public InputStream getConfigurationStream(final String configuration) {
        return getAppRelativeStream("WEB-INF", "conf", configuration);
    }

    public URI getAppRelativeLocation(final String... relativePaths) throws NotFoundException {
        try {
            final String path     = joinPaths(relativePaths);
            final URL    resource = _servletContext.getResource(path);
            if (resource == null) {
                throw new NotFoundException("Couldn't find resource at path " + path);
            }
            return resource.toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            return null;
        }
    }

    private InputStream getAppRelativeStream(final String... relativePaths) {
        return _servletContext.getResourceAsStream(joinPaths(relativePaths));
    }

    private static String joinPaths(final String... elements) {
        return String.join("/", elements);
    }

    @SafeVarargs
    private static <T> T callSuppliers(final Supplier<T> first, final Supplier<T>... suppliers) {
        final Supplier<T>[] aggregated = ArrayUtils.addFirst(suppliers, first);
        if (aggregated.length > 0) {
            for (final Supplier<T> supplier : aggregated) {
                final T bean = supplier.get();
                if (bean != null) {
                    return bean;
                }
            }
        }
        return null;
    }
}
