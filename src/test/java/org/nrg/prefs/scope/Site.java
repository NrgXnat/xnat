package org.nrg.prefs.scope;

import org.nrg.framework.constants.Scope;

import java.util.List;

public class Site extends Entity {
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
