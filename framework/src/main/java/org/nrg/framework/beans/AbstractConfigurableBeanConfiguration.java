package org.nrg.framework.beans;

import org.apache.commons.lang3.ObjectUtils;
import org.nrg.framework.generics.AbstractParameterizedWorker;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides the basic functionality to create and register bean definitions in a configurable bean configuration.
 *
 * Note that, although this refers to beans, almost <i>anything</i> in Spring is considered a bean, so this can include
 * services, data repositories, and so forth.
 *
 * @param <T> The type of the configurable bean.
 */
public abstract class AbstractConfigurableBeanConfiguration<T> extends AbstractParameterizedWorker<T> implements ConfigurableBeanConfiguration {
    /**
     * Creates a configuration that can create instances of the parameterized bean class. Any beans created through
     * this configuration can be found as beans of that type, e.g. <b>context.getBean(Foo.class)</b>.
     */
    protected AbstractConfigurableBeanConfiguration() {
        _presentationClass = null;
    }

    /**
     * Creates a configuration that can create instances of the parameterized bean class, but presents them as beans of
     * the specified presentation class. For example, if you have a class <b>Foo</b> and a subclass of <b>Foo</b>
     * named <b>Bar</b>, you might invoke this method like this:
     *
     * <pre class="code">public class BarConfigurableBeanConfiguration extends AbstractConfigurableBeanConfiguration&lt;Bar&gt; {
     *     public BarConfigurableBeanConfiguration() {
     *         super(Foo.class);
     *     }
     * }</pre>
     *
     * This is especially useful when you have a number of different implementations of the same interface that you want
     * to treat the same way in the application context.
     *
     * @param presentationClass The class that the bean should be presented as in the application context.
     */
    protected AbstractConfigurableBeanConfiguration(final Class<? super T> presentationClass) {
        _presentationClass = presentationClass;
    }

    /**
     * Called by this abstract class to give the implementing class a chance to set the initialization parameters for your
     * configurable beans by calling {@link #addBeanInitializationParameters(String, Object...)}.
     */
    @Override
    public abstract void setBeanInitializationParameters();

    /**
     * Adds the parameters for an instance of the configurable bean.
     *
     * @param beanName   The name of the bean. This should be unique in the application context.
     * @param parameters The parameters to pass to the bean constructor.
     */
    @Override
    public void addBeanInitializationParameters(final String beanName, final Object... parameters) {
        _beanInitializationParameters.put(beanName, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerBeanDefinitions(final AnnotationMetadata metadata, final BeanDefinitionRegistry registry) {
        setBeanInitializationParameters();
        for (final Map.Entry<String, Object[]> entry : _beanInitializationParameters.entrySet()) {
            final String                beanName = entry.getKey();
            final GenericBeanDefinition bean     = createBeanDefinition(getParameterizedType(), beanName, entry.getValue());
            registry.registerBeanDefinition(beanName, bean);
        }
    }

    private GenericBeanDefinition createBeanDefinition(final Class<?> beanClass, final String beanName, final Object[] parameters) {
        final GenericBeanDefinition bean = new GenericBeanDefinition();
        bean.setBeanClass(beanClass);
        if (parameters.length > 0) {
            final ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
            for (final Object parameter : parameters) {
                constructorArgumentValues.addGenericArgumentValue(parameter);
            }
            bean.setConstructorArgumentValues(constructorArgumentValues);
        }
        bean.setAutowireCandidate(true);
        bean.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
        bean.addQualifier(new AutowireCandidateQualifier(ObjectUtils.defaultIfNull(_presentationClass, getParameterizedType()), beanName));
        return bean;
    }


    private final Class<? super T>   _presentationClass;
    private final Map<String, Object[]> _beanInitializationParameters = new HashMap<>();
}
