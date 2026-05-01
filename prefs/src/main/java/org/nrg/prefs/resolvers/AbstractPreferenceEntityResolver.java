/*
 * prefs: org.nrg.prefs.resolvers.AbstractPreferenceEntityResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.resolvers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.nrg.framework.scope.EntityId;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.services.PreferenceService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
abstract public class AbstractPreferenceEntityResolver implements PreferenceEntityResolver {
    protected AbstractPreferenceEntityResolver() {

    }

    @SuppressWarnings("unused")
    protected AbstractPreferenceEntityResolver(final PreferenceService service) {
        _service = service;
    }

    @Override
    public Preference resolve(final EntityId entityId, Object... parameters) {
        final List<EntityId> hierarchy      = getHierarchy(entityId);
        final String         toolId         = (String) parameters[0];
        final String         preferenceName = Arrays.stream(ArrayUtils.subarray(parameters, 1, parameters.length)).map(Object::toString).collect(Collectors.joining("."));
        for (final EntityId candidate : hierarchy) {
            final Preference preference = _service.getPreference(toolId, preferenceName, candidate.getScope(), candidate.getEntityId());
            if (preference != null) {
                return preference;
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    protected PreferenceService getService() {
        return _service;
    }

    protected void setService(final PreferenceService service) {
        _service = service;
    }

    private PreferenceService _service;
}
