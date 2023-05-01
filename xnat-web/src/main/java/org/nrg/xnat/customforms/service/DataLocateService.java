package org.nrg.xnat.customforms.service;

import org.nrg.xnat.entities.CustomVariableFormAppliesTo;

public interface DataLocateService {

    boolean hasDataBeenAcquired(final CustomVariableFormAppliesTo customVariableFormAppliesTo);
}
