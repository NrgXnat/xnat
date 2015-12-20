package org.nrg.prefs.annotations;

import org.nrg.framework.scope.EntityResolver;
import org.nrg.prefs.beans.NrgPreferences;
import org.nrg.prefs.entities.Preference;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NrgPrefsTool {
    String toolId();
    String toolName();
    String description() default "";
    Class<? extends NrgPreferences>[] preferences();
    Class<? extends EntityResolver<Preference>>[] resolvers();
}
