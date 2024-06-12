/*
 * framework: org.nrg.framework.scope.Project
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.scope;

import org.nrg.framework.constants.Scope;

import java.util.List;

public class Project extends Wired {
    public List<Subject> getSubjects() {
        return _subjects;
    }

    public void setSubjects(final List<Subject> subjects) {
        _subjects = subjects;
    }

    @Override
    public Scope getScope() {
        return Scope.Project;
    }

    private List<Subject> _subjects;
}
