package org.nrg.prefs.beans;

import org.nrg.framework.scope.EntityResolver;
import org.nrg.prefs.entities.Preference;

public interface NrgPreferences {
    EntityResolver<Preference> getResolver();
}
