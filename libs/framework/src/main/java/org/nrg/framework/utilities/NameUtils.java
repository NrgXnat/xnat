/*
 * framework: org.nrg.framework.utilities.NameUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Provides a number of common utilities for converting names to various formats and conventions.
 */
public class NameUtils {
    /**
     * Convert from Java class naming convention (all words started with capital letters) to bean/ID convention
     * (lowercase initial letter, camel-capped on word boundaries). Thus SiteAdmin would become siteAdmin. Note that,
     * although you can specify the fully-qualified package and class name, this method discards the package and only
     * converts the class name.
     *
     * @param className The name of the class to be converted to standard bean ID.
     *
     * @return The class name converted to bean ID.
     */
    public static String convertClassNameToBeanId(final String className) {
        final List<String> atoms = Arrays.asList(className.split("\\."));
        return StringUtils.uncapitalize(atoms.get(atoms.size() - 1));
    }

    /**
     * Convert from Java class naming convention (all words started with capital letters) to bean/ID convention
     * (lowercase initial letter, camel-capped on word boundaries). Thus SiteAdmin would become siteAdmin. Note that,
     * although you can specify the fully-qualified package and class name, this method discards the package and only
     * converts the class name.
     *
     * @param classNames The name of the classes to be converted to standard bean IDs.
     *
     * @return The class names converted to bean IDs.
     */
    public static List<String> convertClassNamesToBeanIds(final List<String> classNames) {
        return Lists.transform(Lists.transform(classNames, new Function<String, String>() {
            @Nullable
            @Override
            public String apply(@Nullable final String input) {
                if (StringUtils.isBlank(input)) {
                    return input;
                }
                return Iterables.getLast(Arrays.asList(input.split("\\.")), null);
            }
        }), UncapitalizeFunction.getInstance());
    }

    /**
     * Convert from resource naming convention (all lowercase with words separated by dashes) to bean/ID convention
     * (lowercase initial letter, camel-capped on word boundaries). Thus site-admin would become siteAdmin.
     *
     * @param resourceName The resource name to convert.
     *
     * @return The resource name converted to a bean ID.
     */
    public static String convertResourceNameToBeanId(final String resourceName) {
        final List<String> atoms = Arrays.asList(resourceName.split("-"));
        if (atoms.size() == 1) {
            return StringUtils.uncapitalize(atoms.get(0));
        }
        return StringUtils.lowerCase(atoms.get(0)) + Joiner.on("").join(Lists.transform(atoms.subList(1, atoms.size()), CapitalizeFunction.getInstance()));
    }

    /**
     * Converts the list of strings using the {@link #convertResourceNameToBeanId(String)} method to transform each
     * string in the list.
     *
     * @param resourceNames The resource names to convert.
     *
     * @return The list of resource names converted to bean IDs.
     */
    public static List<String> convertResourceNamesToBeanIds(final List<String> resourceNames) {
        return Lists.transform(resourceNames, ResourceNamesToBeanIdsFunction.getInstance());
    }

    /**
     * Convert from bean/ID convention (lowercase initial letter, camel-capped on word boundaries) to resource naming
     * convention (all lowercase with words separated by dashes). Thus siteAdmin would become site-admin.
     *
     * @param beanId The bean ID to convert.
     *
     * @return The bean ID converted to a resource name.
     */
    public static String convertBeanIdToResourceName(final String beanId) {
        final List<String> atoms = Arrays.asList(beanId.split("(?=[A-Z])"));
        if (atoms.size() == 1) {
            return atoms.get(0);
        }
        return Joiner.on("-").join(Lists.transform(atoms, LowercaseFunction.getInstance()));
    }

    /**
     * Converts the list of strings using the {@link #convertResourceNameToBeanId(String)} method to transform each
     * string in the list.
     *
     * @param beanIds The bean IDs to convert.
     *
     * @return The list of bean IDs converted to resource names.
     */
    public static List<String> convertBeanIdsToResourceNames(final List<String> beanIds) {
        return Lists.transform(beanIds, BeanIdsToResourceNamesFunction.getInstance());
    }

    public static class CapitalizeFunction implements Function<String, String> {
        public static CapitalizeFunction getInstance() {
            return _instance;
        }

        @Nullable
        @Override
        public String apply(@Nullable final String input) {
            if (StringUtils.isBlank(input)) {
                return input;
            }
            return StringUtils.capitalize(input);
        }

        private static final CapitalizeFunction _instance = new CapitalizeFunction();
    }

    public static class UncapitalizeFunction implements Function<String, String> {
        public static UncapitalizeFunction getInstance() {
            return _instance;
        }

        @Nullable
        @Override
        public String apply(@Nullable final String input) {
            if (StringUtils.isBlank(input)) {
                return input;
            }
            return StringUtils.uncapitalize(input);
        }

        private static final UncapitalizeFunction _instance = new UncapitalizeFunction();
    }

    public static class LowercaseFunction implements Function<String, String> {
        public static LowercaseFunction getInstance() {
            return _instance;
        }

        @Nullable
        @Override
        public String apply(@Nullable final String input) {
            if (StringUtils.isBlank(input)) {
                return input;
            }
            return StringUtils.lowerCase(input);
        }

        private static final LowercaseFunction _instance = new LowercaseFunction();
    }

    public static class ResourceNamesToBeanIdsFunction implements Function<String, String> {
        public static ResourceNamesToBeanIdsFunction getInstance() {
            return _instance;
        }

        @Nullable
        @Override
        public String apply(@Nullable final String input) {
            if (StringUtils.isBlank(input)) {
                return input;
            }
            return convertResourceNameToBeanId(input);
        }

        private static final ResourceNamesToBeanIdsFunction _instance = new ResourceNamesToBeanIdsFunction();
    }

    public static class BeanIdsToResourceNamesFunction implements Function<String, String> {
        public static BeanIdsToResourceNamesFunction getInstance() {
            return _instance;
        }

        @Nullable
        @Override
        public String apply(@Nullable final String input) {
            if (StringUtils.isBlank(input)) {
                return input;
            }
            return convertBeanIdToResourceName(input);
        }

        private static final BeanIdsToResourceNamesFunction _instance = new BeanIdsToResourceNamesFunction();
    }
}
