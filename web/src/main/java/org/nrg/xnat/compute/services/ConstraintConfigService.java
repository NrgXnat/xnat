package org.nrg.xnat.compute.services;

import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnat.compute.models.ConstraintConfig;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ConstraintConfigService {

    Optional<ConstraintConfig> retrieve(Long id);
    List<ConstraintConfig> getAll();
    List<ConstraintConfig> getAvailable(Map<Scope, String> executionScope);
    ConstraintConfig create(ConstraintConfig config);
    ConstraintConfig update(ConstraintConfig config) throws NotFoundException;
    void delete(Long id);

}
