package org.nrg.prefs.scope;

import org.nrg.framework.constants.Scope;

import java.util.List;

public class Project extends Entity {
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
