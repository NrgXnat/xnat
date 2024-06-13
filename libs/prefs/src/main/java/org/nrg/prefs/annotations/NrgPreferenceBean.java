/*
 * prefs: org.nrg.prefs.annotations.NrgPreferenceBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.annotations;

import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.transformers.PreferenceTransformer;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Marks a class as an NRG preferences object.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface NrgPreferenceBean {
    /**
     * Specifies the ID for this tool in the preferences service. This ID must be unique.
     *
     * @return The ID for the tool.
     */
    String toolId();

    /**
     * Specifies the name for this tool in the preferences service. This name must be unique. You can specify a key in
     * a message resource bundle instead by setting the {@link #toolNameKey() toolNameKey attribute} instead.
     *
     * @return The name for the tool.
     */
    String toolName();

    /**
     * Specifies the message resource key containing the tool name in the preferences service. This name must be unique.
     * You can specify the tool name directly instead by setting the {@link #toolName() toolName attribute} instead.
     *
     * @return The message resource key for the tool name.
     */
    String toolNameKey() default "";

    /**
     * Provides a description of the tool.
     *
     * @return The description of the tool.
     */
    /**
     * Provides a description of the tool. You can specify a key in a message resource bundle instead by setting the
     * {@link #descriptionKey()}  descriptionKey attribute} instead.
     *
     * @return The description of the tool.
     */
    String description() default "";

    /**
     * Provides a description of the tool. You can provide the description directly instead by setting the {@link
     * #description()} description attribute} instead.
     *
     * @return The message resource key for the tool description.
     */
    String descriptionKey() default "";

    /**
     * Indicates whether the preferences for the tool are limited to the preference keys specified by the preferences
     * bean or whether free-form preferences can be added to the tool.
     *
     * @return Returns true if the tool preferences are limited to configured preferences, false otherwise.
     */
    boolean strict() default true;

    /**
     * Specifies the URI for a properties resource that can be used to initialize the values for the preference bean.
     * This can work in conjunction with or in place of initializing preference values using the {@link
     * NrgPreference#defaultValue()} attribute. Note that the property keys in the specified resource should match
     * either the "propertized" name of the property method (e.g. the property for setting <b>getFoo()</b> would just be
     * <b>foo</b>) or the value for the annotation's {@link NrgPreference#property()} attribute.
     *
     * @return The URI path for the initializing properties file. This is a blank string if a resource isn't specified.
     */
    String properties() default "";

    /**
     * Indicates the ID of the tool's {@link PreferenceEntityResolver entity resolver}. This allows the resolver to be
     * injected at run-time.
     *
     * @return The ID for the tool's entity resolver implementation.
     */
    Class<? extends PreferenceEntityResolver>[] resolver() default {};

    /**
     * Any custom {@link PreferenceTransformer transformer classes} required by the preferences service to transform
     * complex data types into serialized values that can be stored and back again.
     *
     * @return An array of transformer classes, if specified.
     */
    Class<? extends PreferenceTransformer>[] transformers() default {};
}
