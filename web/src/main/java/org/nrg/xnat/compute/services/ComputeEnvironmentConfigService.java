package org.nrg.xnat.compute.services;

import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnat.compute.models.ComputeEnvironmentConfig;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ComputeEnvironmentConfigService {

    boolean exists(Long id);
    Optional<ComputeEnvironmentConfig> retrieve(Long id);
    List<ComputeEnvironmentConfig> getAll();
    List<ComputeEnvironmentConfig> getByType(ComputeEnvironmentConfig.ConfigType type);
    List<ComputeEnvironmentConfig> getAvailable(Map<Scope, String> executionScope);
    List<ComputeEnvironmentConfig> getAvailable(ComputeEnvironmentConfig.ConfigType type, Map<Scope, String> executionScope);
    boolean isAvailable(Long id, Map<Scope, String> executionScope);
    ComputeEnvironmentConfig create(ComputeEnvironmentConfig computeEnvironmentConfig);
    ComputeEnvironmentConfig update(ComputeEnvironmentConfig computeEnvironmentConfig) throws NotFoundException;
    void delete(Long id) throws NotFoundException;

}
