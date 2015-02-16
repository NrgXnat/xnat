package org.nrg.prefs.scope;

import org.nrg.framework.constants.Scope;

import java.util.List;

public class Subject extends Entity {
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
