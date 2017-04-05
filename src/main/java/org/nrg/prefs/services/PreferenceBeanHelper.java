/*
 * prefs: org.nrg.prefs.services.PreferenceBeanHelper
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.services;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NotConcreteTypeException;
import org.nrg.framework.exceptions.NotParameterizedTypeException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.entities.PreferenceInfo;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;

import static org.nrg.framework.utilities.Reflection.*;
import static org.reflections.ReflectionUtils.withAnnotation;

/**
 * Utility methods for working with preference beans.
 */
public class PreferenceBeanHelper {

    /**
     * Walks the methods on the submitted class annotated with {@link NrgPreference} and extracts {@link PreferenceInfo}
     * objects for each preference setting.
     *
     * @param clazz The {@link AbstractPreferenceBean preference bean class} to process.
     *
     * @return The preferences found on the class, stored by the preference property or name.
     */
    public static Map<String, PreferenceInfo> getPreferenceInfoMap(final Class<? extends AbstractPreferenceBean> clazz) {
        final String uri = clazz.getAnnotation(NrgPreferenceBean.class).properties();
        final Properties defaults = new Properties();
        if (StringUtils.isNotBlank(uri)) {
            try {
                for (final Resource resource : BasicXnatResourceLocator.getResources(uri)) {
                    try (final InputStream input = resource.getInputStream()) {
                        defaults.load(input);
                    }
                }
            } catch (FileNotFoundException e) {
                _log.info("Didn't find the properties bundle specified by the URI {} on the class {}, proceeding without initializing.", uri, clazz.getName());
            } catch (IOException e) {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Unable to load the properties bundle specified by the URI " + uri + " on the class " + clazz.getName(), e);
            }
        }
        final Map<String, PreferenceInfo> preferences = new HashMap<>();
        @SuppressWarnings("unchecked") final Set<Method> properties = ReflectionUtils.getAllMethods(clazz, withAnnotation(NrgPreference.class));
        for (final Method method : properties) {
            final NrgPreference annotation = method.getAnnotation(NrgPreference.class);
            final String name;
            final String property;
            final Class<?> type;
            final Type genericType;
            final Method getter;
            final Method setter;
            if (isGetter(method)) {
                name = propertize(method.getName());
                property = annotation.property();
                type = method.getReturnType();
                genericType = method.getGenericReturnType();
                getter = method;
                setter = getSetter(clazz, name);
            } else if (isSetter(method)) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You can't annotate the " + method.getName() + "() method with " + parameterTypes.length + " parameters: it must have one and only one parameter.");
                }
                name = propertize(method.getName());
                property = annotation.property();
                type = parameterTypes[0];
                genericType = method.getGenericParameterTypes()[0];
                setter = method;
                getter = getGetter(clazz, name);
            } else {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The " + method.getName() + "() method doesn't appear to be a getter or a setter, but is annotated anyway. Only getter and setter methods should be annotated.");
            }

            final boolean isArray = type.isArray();
            final boolean isList = List.class.isAssignableFrom(type);
            final boolean isMap = Map.class.isAssignableFrom(type);
            final boolean isProperties = Properties.class.isAssignableFrom(type);

            // If this is a list or a map, then the type should be the type of map and the generic type should be the
            // parameterized type. If they're equal, that means it's just a List or Map with no type set, which means we
            // can't determine what type of object is inside the list or map.
            if ((isList || isMap) && type.equals(genericType) && !isProperties) {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The " + method.getName() + "() method must use a parameterized " + (isList ? "list" : "map") + " type so that I can determine the type of preference in the collection.");
            }

            // We'll use the property if that's specified, but if not we'll used the propertized name.
            final String propertyName = StringUtils.defaultIfBlank(property, name);

            // Here we get the default value, favoring the property value over the annotated value.
            final String defaultValue = defaults.getProperty(propertyName, annotation.defaultValue());

            final PreferenceInfo info = new PreferenceInfo(name, property, defaultValue, annotation.key(), type, getter, setter, annotation.aliases());

            if (isArray || isList) {
                final Class<?> itemType;
                try {
                    itemType = isArray ? type.getComponentType() : getClassesFromParameterizedType(genericType).get(0);
                } catch (NotParameterizedTypeException e) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The " + method.getName() + "() method has a list that is not parameterized, i.e. is just a List rather than List<String>. This is not currently supported.");
                } catch (NotConcreteTypeException e) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The " + method.getName() + "() method has a list that is parameterized to contain a parameterized type, e.g. List<List<String>>. This is not currently supported.");
                }
                if (!BeanUtils.isSimpleValueType(itemType) && StringUtils.isBlank(annotation.key())) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "When specifying an array or list of complex types as a preference setting, you must also specify the key property on the complex type to use to store the preference data, e.g. 'key=\"id\", where 'id' corresponds to a 'getId()' method on the complex type.");
                }
                info.setItemType(itemType);
            } else if (isProperties) {
                info.setItemType(String.class);
            } else if (isMap) {
                final List<Class<?>> classes;
                try {
                    classes = getClassesFromParameterizedType(genericType);
                } catch (NotParameterizedTypeException e) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The " + method.getName() + "() method has a map that is not parameterized, i.e. is just a Map rather than Map<String, String>. This is not currently supported.");
                } catch (NotConcreteTypeException e) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The " + method.getName() + "() method has a map that is parameterized to contain a parameterized type, e.g. Map<String, List<String>>. This is not currently supported.");
                }
                final Class<?> keyType = classes.get(0);
                if (!keyType.equals(String.class)) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The " + method.getName() + "() method has a map that uses a non-String key type. This is not currently supported.");
                }
                final Class<?> valueType = classes.get(1);
                info.setItemType(valueType);
            } else {
                info.setItemType(null);
            }
            preferences.put(info.getProperty(), info);
        }
        return preferences;
    }

    /**
     * Iterates through the collection of preferences and populates the submitted maps. The aliases map is populated
     * with any aliases specified in the preferences and the corresponding preference object. This allows for easily
     * locating the current preference information for any preference alias. Note that the resulting map may include
     * individual preference objects multiple times if that preference has more than one alias. The aliased preferences
     * is populated with the name of preference that contains aliases, along with a list of all aliases. This allows for
     * easily checking whether a preference already exists under one of the given aliases.
     *
     * @param preferences           The preferences to sort by aliases.
     * @param aliases               A map of all preferences that have aliases, keyed by the alias names.
     * @param aliasedPreferences    A map of preference names with a list of applicable aliases.
     */
    public static void getAliases(final Collection<PreferenceInfo> preferences, final Map<String, String> aliases, final Map<String, List<String>> aliasedPreferences) {
        for (final PreferenceInfo preference : preferences) {
            for (final String alias : preference.getAliases()) {
                aliases.put(alias, preference.getProperty());
                final String property = preference.getProperty();
                if (!aliasedPreferences.containsKey(property)) {
                    aliasedPreferences.put(property, Lists.<String>newArrayList());
                }
                aliasedPreferences.get(property).add(alias);
            }
        }
    }

    public static String propertize(final String name) {
        final Matcher matcher = PATTERN_PROPERTY.matcher(name);
        if (!matcher.matches()) {
            return name;
        }
        return StringUtils.uncapitalize(matcher.group("property"));
    }

    private static final Logger _log = LoggerFactory.getLogger(PreferenceBeanHelper.class);
}
