/*
 * framework: org.nrg.framework.beans.Beans
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.beans;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.framework.utilities.RepeatingString;
import org.nrg.framework.utilities.StreamUtils;
import org.springframework.core.env.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.nrg.framework.beans.XnatDataModelBean.PLUGIN_DATA_MODEL_PREFIX;

public class Beans {
    public static <T> T getInitializedBean(final Properties properties, final Class<? extends T> clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        return getInitializedBean(properties.keySet().stream().map(Object::toString).collect(Collectors.toMap(Function.identity(), properties::get)), clazz);
    }

    public static <T> T getInitializedBean(final Map<String, Object> properties, final Class<? extends T> clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        final T bean = clazz.newInstance();
        for (final String key : properties.keySet()) {
            BeanUtils.setProperty(bean, key, properties.get(key));
        }
        return bean;
    }

    public static Set<String> discoverNamespaces(final Properties properties) {
        return discoverNamespaces(properties, 1);
    }

    public static Set<String> discoverNamespaces(final Properties properties, final int extractionLevel) {
        final Set<String> namespaces = new HashSet<>();
        final Pattern     pattern    = Pattern.compile(getNamespacePattern(extractionLevel));
        for (final String property : properties.stringPropertyNames()) {
            final Matcher matcher = pattern.matcher(property);
            if (matcher.find()) {
                namespaces.add(matcher.group("namespace"));
            }
        }
        return namespaces;
    }

    public static Properties getNamespacedProperties(final Environment environment, final String namespace, final boolean truncate) {
        if (environment instanceof AbstractEnvironment) {
            final AbstractEnvironment abstractEnvironment = (AbstractEnvironment) environment;
            return getNamespacedProperties(abstractEnvironment.getPropertySources(), namespace, truncate);
        }
        return new Properties();
    }

    public static Properties getNamespacedProperties(final Properties properties, final String namespace, final boolean truncate) {
        return getNamespacedProperties(Collections.singletonList(properties), namespace, truncate);
    }

    public static Properties getNamespacedProperties(final Collection<Properties> properties, final String namespace, final boolean truncate) {
        final String resolved = StringUtils.defaultIfBlank(namespace, "namespacedProperties");
        return getNamespacedProperties(properties.stream().map(props -> new PropertiesPropertySource(resolved, props)).collect(Collectors.toList()), namespace, truncate);
    }

    public static Properties getNamespacedProperties(final Iterable<PropertySource<?>> propertySources, final String namespace, final boolean truncate) {
        assert StringUtils.isNotBlank(namespace) : "You must provide a value for namespace";
        final String     regex      = "^" + StringUtils.removeEnd(RegExUtils.replaceAll(namespace, "\\.", "\\\\."), ".") + "(\\.[A-z0-9_\\.-]+)?";
        final Properties properties = new Properties();
        for (final PropertySource<?> propertySource : propertySources) {
            properties.putAll(getMatchingProperties(propertySource, regex));
        }
        if (!truncate) {
            return properties;
        }
        final Properties truncated = new Properties();
        final int        offset    = namespace.length() + 1;
        for (final String key : properties.stringPropertyNames()) {
            final String namespacedKey = StringUtils.equals(namespace, key) ? "default" : key.substring(offset);
            truncated.put(namespacedKey, properties.getProperty(key));
        }
        return truncated;
    }

    public static Map<Class<?>, Class<?>> getMixIns() throws NrgServiceException {
        final Map<Class<?>, Class<?>> mixIns = new HashMap<>();
        try {
            for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/serializers/*-mixin.properties")) {
                final Properties properties;
                try {
                    properties = PropertiesLoaderUtils.loadProperties(resource);
                } catch (IOException e) {
                    throw new NrgServiceException(NrgServiceError.Unknown, "An error occurred attempting to read in mixin properties file " + resource.getFilename(), e);
                }
                for (final String target : properties.stringPropertyNames()) {
                    final String   mixIn = properties.getProperty(target);
                    final Class<?> targetClass;
                    try {
                        targetClass = Class.forName(target);
                    } catch (ClassNotFoundException e) {
                        throw new NrgServiceException(NrgServiceError.ConfigurationError, "Could not find class " + target, e);
                    }
                    final Class<?> mixInClass;
                    try {
                        mixInClass = Class.forName(mixIn);
                    } catch (ClassNotFoundException e) {
                        throw new NrgServiceException(NrgServiceError.ConfigurationError, "Could not find class " + mixIn, e);
                    }
                    mixIns.put(targetClass, mixInClass);
                }
            }
        } catch (IOException e) {
            throw new NrgServiceException(NrgServiceError.Unknown, "An error occurred attempting to discover mixin properties files on the classpath.", e);
        }
        return mixIns;

    }

    /**
     * Gets a map of properties by namespace. This differs from the variations of {@link #getNamespacedProperties(Iterable, String, boolean)} in that it
     * gets properties by the specified namespace, then breaks those down by the next detected property namespace and returns multiple properties objects,
     * each stored under the second namespace. For example, take a properties file like this:
     * <p>
     * {@code
     * alpha.one.first=xxx
     * alpha.one.second=xxx
     * alpha.one.third=xxx
     * alpha.two.first=xxx
     * alpha.two.second=xxx
     * alpha.two.third=xxx
     * }
     * <p>
     * Then you call:
     * <p>
     * {@code
     * getNamespacedPropertiesMap(properties, "alpha", true, true);
     * }
     * <p>
     * That will return a map with two keys, <b>one</b> and <b>two</b>. Each value will be a properties
     * object containing:
     * <p>
     * {@code
     * first=xxx
     * second=xxx
     * third=xxx
     * }
     * <p>
     * Note that properties that don't match the namespace are ignored, so if you had properties with names starting with <b>beta</b>, those
     * will not be in your results at all.
     * <p>
     * The <b>truncateFirst</b> parameter indicates whether the top-level namespace should be preserved. The <b>truncateSecond</b> parameter
     * indicates whether the discovered namespaces should be preserved. With the example above, the end result ends up looking like this:
     * <p>
     * When both parameters are <b>true</b>:
     * <p>
     * {@code
     * one -> first=xxx
     * }
     * <p>
     * When <b>truncateFirst</b> is <b>true</b> and <b>truncateSecond</b> is <b>false</b>:
     * <p>
     * {@code
     * one -> one.first=xxx
     * }
     * <p>
     * When <b>truncateFirst</b> is <b>false</b> and <b>truncateSecond</b> is <b>true</b>:
     * <p>
     * {@code
     * alpha.one -> first=xxx
     * }
     * <p>
     * When both parameters are <b>false</b>:
     * <p>
     * {@code
     * alpha.one -> alpha.one.first=xxx
     * }
     *
     * @param properties        The properties to extract.
     * @param topLevelNamespace The top-level namespace to extract.
     * @param extractionLevel   Indicates how many '.'-separated tokens should be considered for the next-level namespace.
     * @param truncateFirst     Whether the top-level namespace should be preserved in the extracted properties.
     * @param truncateSecond    Whether the discovered next-level namespaces should be preserved in the extracted properties.
     *
     * @return The various properties objects separated by namespace.
     */
    public static Map<String, Properties> getNamespacedPropertiesMap(final Properties properties, final String topLevelNamespace, final int extractionLevel, final boolean truncateFirst, final boolean truncateSecond) {
        final Map<String, Properties> propertiesMap = new HashMap<>();
        if (StringUtils.isNotBlank(topLevelNamespace)) {
            final Properties  namespacedProperties = getNamespacedProperties(properties, topLevelNamespace, truncateFirst);
            final int         offset               = truncateFirst ? 0 : StringUtils.split(topLevelNamespace, ".").length;
            final Set<String> namespaces           = discoverNamespaces(namespacedProperties, extractionLevel + offset);
            for (final String namespace : namespaces) {
                propertiesMap.put(namespace, getNamespacedProperties(namespacedProperties, namespace, truncateSecond));
            }
        } else {
            for (final String discovered : discoverNamespaces(properties)) {
                propertiesMap.put(discovered, getNamespacedProperties(properties, discovered, truncateFirst));
            }
        }
        return propertiesMap;
    }

    public static Map<String, Properties> getNamespacedPropertiesMap(final Properties properties, final String namespace, final int extractionLevel) {
        return getNamespacedPropertiesMap(properties, namespace, extractionLevel, true, true);
    }

    public static Map<String, Properties> getNamespacedPropertiesMap(final Properties properties, final String namespace) {
        return getNamespacedPropertiesMap(properties, namespace, 1, true, true);
    }

    public static Map<String, Properties> getNamespacedPropertiesMap(final Properties properties) {
        return getNamespacedPropertiesMap(properties, null, 1, true, true);
    }

    public static List<XnatDataModelBean> getXnatDataModelBeansFromProperties(final Properties properties) {
        final Map<String, Properties> collected = new HashMap<>();
        properties.stringPropertyNames()
                  .stream()
                  .filter(DATA_MODEL_PROPERTY_PREDICATE)
                  .forEach(property -> {
                      final String[] atoms    = property.split("\\.", 4);
                      final String   dataType = atoms[1] + ":" + atoms[2];
                      if (!collected.containsKey(dataType)) {
                          collected.put(dataType, new Properties());
                      }
                      collected.get(dataType).setProperty(atoms[3], properties.getProperty(property));
                  });
        return collected.keySet().stream().map(type -> new XnatDataModelBean(type, collected.get(type))).collect(Collectors.toList());
    }

    private static Properties getMatchingProperties(final PropertySource<?> propertySource, final String regex) {
        return new Properties() {{
            if (propertySource instanceof CompositePropertySource) {
                final CompositePropertySource compositePropertySource = (CompositePropertySource) propertySource;
                for (final PropertySource<?> containedPropertySource : compositePropertySource.getPropertySources()) {
                    putAll(getMatchingProperties(containedPropertySource, regex));
                }
            } else if (propertySource instanceof EnumerablePropertySource<?>) {
                EnumerablePropertySource<?> containedPropertySource = (EnumerablePropertySource<?>) propertySource;
                for (final String propertyName : containedPropertySource.getPropertyNames()) {
                    if (propertyName.matches(regex)) {
                        put(propertyName, containedPropertySource.getProperty(propertyName).toString());
                    }
                }
            }
        }};
    }

    private static String getNamespacePattern(final int count) {
        return String.format(EXTRACT_NAMESPACE_PATTERN, StringUtils.join(new RepeatingString(NAMESPACE_PATTERN, count), "\\."));
    }

    private static final Predicate<String> DATA_MODEL_PROPERTY_PREDICATE = StreamUtils.predicateFromPatterns(Collections.singletonList(Pattern.compile("^" + StringUtils.replace(PLUGIN_DATA_MODEL_PREFIX, ".", "\\.") + ".*$")));
    private static final String            EXTRACT_NAMESPACE_PATTERN     = "^(?<namespace>%s)(\\.|=).*$";
    private static final String            NAMESPACE_PATTERN             = "[^.]+";
}
