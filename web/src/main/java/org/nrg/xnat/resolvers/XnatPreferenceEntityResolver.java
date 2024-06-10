/*
 * web: org.nrg.xnat.resolvers.XnatPreferenceEntityResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.resolvers;

import org.nrg.framework.constants.Scope;
import org.nrg.framework.scope.EntityId;
import org.nrg.prefs.resolvers.AbstractPreferenceEntityResolver;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;

import java.util.ArrayList;
import java.util.List;

public class XnatPreferenceEntityResolver extends AbstractPreferenceEntityResolver {

    public XnatPreferenceEntityResolver(final PreferenceService service) {
        super(service);
    }

    @Override
    public List<EntityId> getHierarchy(final EntityId entityId) {
        return getHierarchy(entityId, new ArrayList<EntityId>());
    }

    private List<EntityId> getHierarchy(final EntityId entityId, final List<EntityId> hierarchy) {
        XnatExperimentdata experiment = null;
        XnatSubjectdata subject = null;
        switch (entityId.getScope()) {
            case Experiment:
                experiment = XnatExperimentdata.getXnatExperimentdatasById(entityId.getEntityId(), null, false);
                if (experiment == null) {
                    return hierarchy;
                }
                hierarchy.add(new EntityId(Scope.Experiment, experiment.getId()));

            case Subject:
                if (experiment == null) {
                    subject = XnatSubjectdata.getXnatSubjectdatasById(entityId.getEntityId(), null, false);
                } else {
                    subject = new XnatSubjectdata(experiment.getParent());
                }
                if (subject == null) {
                    return hierarchy;
                }
                hierarchy.add(new EntityId(Scope.Subject, subject.getId()));

            case Project:
                final XnatProjectdata project;
                if (subject == null) {
                    project = XnatProjectdata.getXnatProjectdatasById(entityId.getEntityId(), null, false);
                } else {
                    project = new XnatProjectdata(subject.getParent());
                }
                if (project == null) {
                    return hierarchy;
                }
                hierarchy.add(new EntityId(Scope.Project, project.getId()));

            case Site:
                hierarchy.add(EntityId.Default);
                return hierarchy;

            default:
                throw new RuntimeException("Unknown scope " + entityId.getScope());
        }
    }
}
