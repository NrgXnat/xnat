/*
 * framework: org.nrg.framework.annotations.XnatPlugin
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.annotations;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Indicates that the annotated class is a plugin configuration class. When referenced within XNAT, this class will be
 * loaded as a Spring configuration class. Any packages referenced in the {@link #entityPackages()} attribute will be
 * passed onto the XNAT session factory to be scanned for persistent entity classes.
 */
@Configuration
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface XnatPlugin {
    String PLUGIN_CLASS             = "class";
    String PLUGIN_ID                = "id";
    String PLUGIN_NAMESPACE         = "namespace";
    String PLUGIN_NAME              = "name";
    String PLUGIN_VERSION           = "version";
    String PLUGIN_DESCRIPTION       = "description";
    String PLUGIN_BEAN_NAME         = "beanName";
    String PLUGIN_ENTITY_PACKAGES   = "entityPackages";
    String PLUGIN_OPEN_URLS         = "openUrls";
    String PLUGIN_ADMIN_URLS        = "adminUrls";
    String PLUGIN_LOG_CONFIGURATION = "logConfiguration";

    /**
     * Key for the log4j logging configuration file.
     *
     * @deprecated Use {@link #PLUGIN_LOG_CONFIGURATION} instead.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    String PLUGIN_LOG4J_PROPERTIES = "log4jPropertiesFile";

    /**
     * In combination with the {@link #namespace()} value, indicates the unique ID for this plugin.
     *
     * @return The ID for the plugin.
     */
    String value();

    /**
     * In combination with the {@link #value()} value, indicates the unique ID for this plugin.
     *
     * @return The namespace for the plugin.
     */
    String namespace() default "";

    /**
     * The readable name for this plugin.
     *
     * @return The name of the plugin.
     */
    String name();

    /**
     * The version of this plugin.
     *
     * @return The version of the plugin.
     */
    String version() default "";

    /**
     * A description for this plugin.
     *
     * @return The plugin description.
     */
    String description() default "";

    /**
     * The name to use as a bean name for this plugin when instantiated.
     *
     * @return The desired bean name.
     */
    String beanName() default "";

    /**
     * Any packages that should be scanned for persistent entity classes, e.g. Hibernate or JPA entities.
     *
     * @return A list of packages to be scanned.
     */
    String[] entityPackages() default {};

    /**
     * A list of open URLs defined in this plugin
     *
     * @return A list of open URLs.
     */
    String[] openUrls() default {};

    /**
     * A list of admin URLs defined in this plugin
     *
     * @return A list of admin URLs.
     */
    String[] adminUrls() default {};

    /**
     * Defines all data models provided by the plugin.
     *
     * @return An array of data models provided by the plugin.
     */
    XnatDataModel[] dataModels() default {};

    /**
     * Path to the log configuration file for your plugin.
     *
     * @return A path to the configuration file to add.
     */
    @AliasFor(attribute = "log4jPropertiesFile")
    String logConfigurationFile() default "";

    /**
     * A file containing Log4j properties you want to add.
     *
     * @return A path to the file containing properties to add.
     *
     * @deprecated Use {@link #logConfigurationFile()} instead.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @AliasFor(attribute = "logConfigurationFile")
    String log4jPropertiesFile() default "";
}
