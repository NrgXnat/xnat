package org.nrg.prefs.resolvers;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.ArrayUtils;
import org.nrg.framework.scope.EntityId;
import org.nrg.framework.scope.EntityResolver;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.services.PreferenceService;

import javax.inject.Inject;
import java.util.List;

abstract public class AbstractPreferenceEntityResolver implements EntityResolver<Preference> {

    protected AbstractPreferenceEntityResolver(final PreferenceService service) {
        _service = service;
    }

    public Preference resolve(final EntityId entityId, Object... parameters) {
        final List<EntityId> hierarchy = getHierarchy(entityId);
        final String toolId = (String) parameters[0];
        final String preferenceName = Joiner.on(".").join(ArrayUtils.subarray(parameters, 1, parameters.length));
        for (final EntityId candidate : hierarchy) {
            final Preference preference = _service.getPreference(toolId, preferenceName, candidate.getScope(), candidate.getEntityId());
            if (preference != null) {
                return preference;
            }
        }
        return null;
    }

    protected PreferenceService getService() {
        return _service;
    }

    private PreferenceService _service;
}
