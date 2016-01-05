package org.nrg.prefs.resolvers;

import org.nrg.framework.scope.EntityId;
import org.nrg.framework.scope.EntityResolver;
import org.nrg.prefs.entities.Preference;

public interface PreferenceEntityResolver extends EntityResolver<Preference> {
    Preference resolve(EntityId entityId, Object... parameters);
}
