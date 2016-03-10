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
public @interface NrgPreferencesBean {
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
    String toolName() default "";

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
     * Indicates the ID of the tool's {@link PreferenceEntityResolver entity resolver}. This allows the resolver to be
     * injected at run-time.
     *
     * @return The ID for the tool's entity resolver implementation.
     */
    Class<? extends PreferenceEntityResolver>[] resolver() default {};

    /**
     * Any custom {@link PreferenceTransformer transformer classes} required by the preferences service to transform
     * complex data types into serialized values that can be stored and back again.
     * @return An array of transformer classes, if specified.
     */
    Class<? extends PreferenceTransformer>[] transformers() default {};
}
