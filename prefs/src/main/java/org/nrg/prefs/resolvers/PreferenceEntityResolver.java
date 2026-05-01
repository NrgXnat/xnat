/*
 * prefs: org.nrg.prefs.resolvers.PreferenceEntityResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.resolvers;

import org.nrg.framework.scope.EntityId;
import org.nrg.framework.scope.EntityResolver;
import org.nrg.prefs.entities.Preference;

public interface PreferenceEntityResolver extends EntityResolver<Preference> {
    Preference resolve(EntityId entityId, Object... parameters);
}
