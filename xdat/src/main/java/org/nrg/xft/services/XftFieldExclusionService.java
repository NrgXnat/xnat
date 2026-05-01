/*
 * core: org.nrg.xft.services.XftFieldExclusionService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.services;

import java.util.List;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xft.entities.XftFieldExclusion;
import org.nrg.xft.entities.XftFieldExclusionScope;

public interface XftFieldExclusionService extends BaseHibernateService<XftFieldExclusion> {

    /**
     * Gets all the exclusions that are scoped to the {@link XftFieldExclusionScope#System system level}.
     * @return A list of all the matching exclusion objects.
     */
    List<XftFieldExclusion> getSystemExclusions();

    /**
     * Gets all the exclusions that are scoped to the {@link XftFieldExclusionScope specified scope} and
     * indicated target.
     * @param scope The {@link XftFieldExclusionScope scope} to which the target ID is matched.
     * @param targetId The ID of the target scope.
     * @return A list of all the matching exclusion objects.
     */
    List<XftFieldExclusion> getExclusionsForScopedTarget(XftFieldExclusionScope scope, String targetId);
}
