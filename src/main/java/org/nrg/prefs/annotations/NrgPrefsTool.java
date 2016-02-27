package org.nrg.prefs.annotations;

import org.nrg.framework.scope.EntityResolver;
import org.nrg.prefs.beans.NrgPreferences;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.services.NrgPrefsService;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * This annotation marks an object as a {@link Tool tool} in the {@link NrgPrefsService NRG preferences service}. If the
 * tool does not already exist in the service, it will be created. This requires that the {@link #toolId() tool ID} and
 * the {@link #toolName() name} be unique on the system. You must also specify an implementation of the {@link
 * NrgPreferences NRG preferences} class and an {@link EntityResolver entity resolver}.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface NrgPrefsTool {
    /**
     * Specifies the ID for this tool in the preferences service. This ID must be unique.
     *
     * @return The ID for the tool.
     */
    String toolId();

    /**
     * Specifies the name for this tool in the preferences service. This name must be unique.
     *
     * @return The name for the tool.
     */
    String toolName();

    /**
     * Provides a description of the tool.
     *
     * @return The description of the tool.
     */
    String description() default "";

    /**
     * Sets the list of preferences and their default values for the preferences tool.
     *
     * @return The list of {@link NrgPrefValue annotations} with preference names and default values for this tool.
     */
    NrgPrefValue[] preferences() default {};

    /**
     * Indicates whether the set of preferences for the tool is strictly defined by the {@link #preferences()} list or
     * if ad-hoc preferences can be added. The default is false.
     *
     * @return Whether the preferences are limited to those defined by the {@link #preferences()} list.
     */
    boolean strict() default false;

    /**
     * Indicates the class to use for the tool's {@link NrgPreferences preferences class}. This must be an
     * implementation of the {@link NrgPreferences} interface.
     *
     * @return The class for the tool's preferences class.
     */
    Class<? extends NrgPreferences> preferencesClass();

    /**
     * Indicates the ID of the tool's {@link PreferenceEntityResolver entity resolver}. This allows the resolver to be
     * injected at run-time.
     *
     * @return The ID for the tool's entity resolver implementation.
     */
    String resolverId() default "";

    /**
     * Indicates the property to be called to set the {@link NrgPreferences} object indicated by this annotation's
     * {@link #preferencesClass()} attribute. The property must take a single parameter of type {@link NrgPreferences}. If a
     * value isn't specified for this attribute, the default property is <strong>preferences</strong> (i.e. the
     * preferences object is set via the <strong>setPreferences(NrgPreferences)</strong> method).
     *
     * @return The property to use to set the {@link NrgPreferences} object for the tool instance.
     */
    String property() default "preferences";
}
