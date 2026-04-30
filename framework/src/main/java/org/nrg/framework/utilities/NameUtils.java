/*
 * framework: org.nrg.framework.utilities.NameUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides a number of common utilities for converting names to various formats and conventions.
 */
public class NameUtils {
    /**
     * Convert from Java class naming convention (all words started with capital letters) to bean/ID convention
     * (lowercase initial letter, camel-capped on word boundaries). Thus, SiteAdmin would become siteAdmin. Note that,
     * although you can specify the fully-qualified package and class name, this method discards the package and only
     * converts the class name.
     *
     * @param className The name of the class to be converted to standard bean ID.
     *
     * @return The class name converted to bean ID.
     */
    @SuppressWarnings("unused")
    public static String convertClassNameToBeanId(final String className) {
        final List<String> atoms = Arrays.asList(className.split("\\."));
        return StringUtils.uncapitalize(atoms.getLast());
    }

    /**
     * Convert from Java class naming convention (all words started with capital letters) to bean/ID convention
     * (lowercase initial letter, camel-capped on word boundaries). Thus, SiteAdmin would become siteAdmin. Note that,
     * although you can specify the fully-qualified package and class name, this method discards the package and only
     * converts the class name.
     *
     * @param classNames The name of the classes to be converted to standard bean IDs.
     *
     * @return The class names converted to bean IDs.
     */
    public static List<String> convertClassNamesToBeanIds(final List<String> classNames) {
        return classNames.stream().map(className -> StringUtils.uncapitalize(StringUtils.substringAfterLast(className, "."))).collect(Collectors.toList());
    }

    /**
     * Convert from resource naming convention (all lowercase with words separated by dashes) to bean/ID convention
     * (lowercase initial letter, camel-capped on word boundaries). Thus, site-admin would become siteAdmin.
     *
     * @param resourceName The resource name to convert.
     *
     * @return The resource name converted to a bean ID.
     */
    public static String convertResourceNameToBeanId(final String resourceName) {
        if (StringUtils.isBlank(resourceName)) {
            return resourceName;
        }
        final String[] atoms = resourceName.split("-");
        return atoms.length == 1
               ? StringUtils.uncapitalize(atoms[0])
               : StringUtils.lowerCase(atoms[0]) + Arrays.stream(ArrayUtils.subarray(atoms, 1, atoms.length)).map(StringUtils::capitalize).collect(Collectors.joining(""));
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
        return resourceNames.stream().map(NameUtils::convertResourceNameToBeanId).collect(Collectors.toList());
    }

    /**
     * Convert from bean/ID convention (lowercase initial letter, camel-capped on word boundaries) to resource naming
     * convention (all lowercase with words separated by dashes). Thus, siteAdmin would become site-admin.
     *
     * @param beanId The bean ID to convert.
     *
     * @return The bean ID converted to a resource name.
     */
    public static String convertBeanIdToResourceName(final String beanId) {
        if (StringUtils.isBlank(beanId)) {
            return beanId;
        }
        final String[] atoms = beanId.split("(?=[A-Z])");
        if (atoms.length == 1) {
            return atoms[0];
        }
        return Arrays.stream(atoms).map(StringUtils::lowerCase).collect(Collectors.joining("-"));
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
        return beanIds.stream().map(NameUtils::convertBeanIdToResourceName).collect(Collectors.toList());
    }
}
