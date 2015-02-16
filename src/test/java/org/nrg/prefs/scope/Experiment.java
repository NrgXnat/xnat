package org.nrg.prefs.scope;

import org.nrg.framework.constants.Scope;

public class Experiment extends Entity {
    @Override
    public Scope getScope() {
        return Scope.Experiment;
    }
}
