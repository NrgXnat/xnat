/*
 * prefs: org.nrg.prefs.resolvers.SimplePrefsEntityResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.resolvers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.scope.EntityId;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.services.PreferenceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is provided primarily to support unit and integration tests.
 */
@Component
public class SimplePrefsEntityResolver extends AbstractPreferenceEntityResolver implements InitializingBean {
    @Autowired
    public SimplePrefsEntityResolver(final PreferenceService service, final JsonNode siteMap) throws IOException {
        _service = service;

        final Site site = new ObjectMapper().treeToValue(siteMap, Site.class);
        _registry.put(site.getEntityId(), site);
        for (final Project project : site.getProjects()) {
            project.setParentEntityId(site.getEntityId());
            _registry.put(project.getEntityId(), project);
            for (final Subject subject : project.getSubjects()) {
                subject.setParentEntityId(project.getEntityId());
                _registry.put(subject.getEntityId(), subject);
                for (final Experiment experiment : subject.getExperiments()) {
                    experiment.setParentEntityId(subject.getEntityId());
                    _registry.put(experiment.getEntityId(), experiment);
                }
            }
        }
    }

    public Entity getEntity(final EntityId entityId) {
        return _registry.get(entityId);
    }

    @Override
    public List<EntityId> getHierarchy(final EntityId entityId) {
        EntityId current = new EntityId(entityId.getScope(), entityId.getEntityId());

        final List<EntityId> hierarchy = new ArrayList<>();
        switch (entityId.getScope()) {
            case Experiment:
                hierarchy.add(current);
                current = getEntity(current).getParentEntityId();

            case Subject:
                hierarchy.add(current);
                current = getEntity(current).getParentEntityId();

            case Project:
                hierarchy.add(current);
                current = getEntity(current).getParentEntityId();

            case Site:
                hierarchy.add(current);
                return hierarchy;

            default:
                throw new RuntimeException("Unknown scope " + entityId.getScope());
        }
    }

    @Override
    public Preference resolve(final EntityId entityId, Object... parameters) {
        final List<EntityId> hierarchy = getHierarchy(entityId);
        final String toolId = (String) parameters[0];
        final String preferenceName = (String) parameters[1];
        for (final EntityId candidate : hierarchy) {
            Preference preference = _service.getPreference(toolId, preferenceName, candidate.getScope(), candidate.getEntityId());
            if (preference != null) {
                return preference;
            }
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setService(_service);
    }

    public abstract static class Entity {
        public String getId() {
            return _id;
        }

        public void setId(final String id) {
            _id = id;
            _entityId = new EntityId(getScope(), _id);
        }

        public EntityId getEntityId() {
            return _entityId;
        }

        public EntityId getParentEntityId() {
            return _parentEntityId;
        }

        public void setParentEntityId(final EntityId parentEntityId) {
            _parentEntityId = parentEntityId;
        }

        abstract public Scope getScope();

        private String   _id;
        private EntityId _entityId;
        private EntityId _parentEntityId;
    }

    private static class Experiment extends Entity {
        @Override
        public Scope getScope() {
            return Scope.Experiment;
        }
    }

    private static class Project extends Entity {
        public List<Subject> getSubjects() {
            return _subjects;
        }

        @SuppressWarnings("unused")
        public void setSubjects(final List<Subject> subjects) {
            _subjects = subjects;
        }

        @Override
        public Scope getScope() {
            return Scope.Project;
        }

        private List<Subject> _subjects;
    }

    private static class Site extends Entity {
        public Site() {
            setId(null);
        }

        public List<Project> getProjects() {
            return _projects;
        }

        @SuppressWarnings("unused")
        public void setProjects(final List<Project> projects) {
            _projects = projects;
        }

        @SuppressWarnings("unused")
        public Project getProject(final String projectId) {
            for (Project project : _projects) {
                if (project.getId().equals(projectId)) {
                    return project;
                }
            }
            return null;
        }

        @Override
        public Scope getScope() {
            return Scope.Site;
        }

        private List<Project> _projects;
    }

    private static class Subject extends Entity {
        public List<Experiment> getExperiments() {
            return _experiments;
        }

        @SuppressWarnings("unused")
        public void setExperiments(final List<Experiment> experiments) {
            _experiments = experiments;
        }

        @Override
        public Scope getScope() {
            return Scope.Subject;
        }

        private List<Experiment> _experiments;
    }

    private final Map<EntityId, Entity> _registry = new HashMap<>();

    private final PreferenceService _service;
}
