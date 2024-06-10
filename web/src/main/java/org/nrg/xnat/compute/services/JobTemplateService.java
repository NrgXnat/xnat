package org.nrg.xnat.compute.services;

import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.models.JobTemplate;

import java.util.Map;

public interface JobTemplateService {

    boolean isAvailable(Long computeEnvironmentConfigId, Long hardwareConfigId, Map<Scope, String> executionScope);
    JobTemplate resolve(Long computeEnvironmentConfigId, Long hardwareConfigId, Map<Scope, String> executionScope);

}
