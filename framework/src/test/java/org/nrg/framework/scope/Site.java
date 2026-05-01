/*
 * framework: org.nrg.framework.scope.Site
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.scope;

import org.nrg.framework.constants.Scope;

import java.util.List;

public class Site extends Wired {
    public Site() {
        setId(null);
    }

    public List<Project> getProjects() {
        return _projects;
    }

    public void setProjects(final List<Project> projects) {
        _projects = projects;
    }

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
