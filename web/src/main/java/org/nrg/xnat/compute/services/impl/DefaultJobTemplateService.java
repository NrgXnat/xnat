package org.nrg.xnat.compute.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.models.*;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
import org.nrg.xnat.compute.services.ConstraintConfigService;
import org.nrg.xnat.compute.services.HardwareConfigService;
import org.nrg.xnat.compute.services.JobTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultJobTemplateService implements JobTemplateService {

    private final ComputeEnvironmentConfigService computeEnvironmentConfigService;
    private final HardwareConfigService hardwareConfigService;
    private final ConstraintConfigService constraintConfigService;

    @Autowired
    public DefaultJobTemplateService(final ComputeEnvironmentConfigService computeEnvironmentConfigService,
                                     final HardwareConfigService hardwareConfigService,
                                     final ConstraintConfigService constraintConfigService) {
        this.computeEnvironmentConfigService = computeEnvironmentConfigService;
        this.hardwareConfigService = hardwareConfigService;
        this.constraintConfigService = constraintConfigService;
    }

    /**
     * Returns true if the specified compute environment config and hardware config are available for the provided
     * execution scope and the hardware config is allowed by the compute environment config.
     *
     * @param computeEnvironmentConfigId the compute environment config id
     * @param hardwareConfigId           the hardware config id
     * @return true if the specified compute environment config and hardware config are available for the provided
     * execution scope and the hardware config is allowed by the compute environment config
     */
    @Override
    public boolean isAvailable(Long computeEnvironmentConfigId, Long hardwareConfigId, Map<Scope, String> executionScope) {
        if (computeEnvironmentConfigId == null || hardwareConfigId == null || executionScope == null) {
            throw new IllegalArgumentException("One or more parameters is null");
        }

        boolean isComputeEnvironmentConfigAvailable = computeEnvironmentConfigService.isAvailable(computeEnvironmentConfigId, executionScope);
        boolean isHardwareConfigAvailable = hardwareConfigService.isAvailable(hardwareConfigId, executionScope);

        if (!isComputeEnvironmentConfigAvailable || !isHardwareConfigAvailable) {
            return false;
        }

        ComputeEnvironmentConfig computeEnvironmentConfig = computeEnvironmentConfigService
                .retrieve(computeEnvironmentConfigId)
                .orElseThrow(() -> new IllegalArgumentException("ComputeEnvironmentConfig with id " + computeEnvironmentConfigId + " does not exist"));

        if (computeEnvironmentConfig.getHardwareOptions().isAllowAllHardware()) {
            return true;
        }

        return computeEnvironmentConfig.getHardwareOptions()
                .getHardwareConfigs().stream()
                .map(HardwareConfig::getId)
                .anyMatch(id -> id.equals(hardwareConfigId));
    }

    /**
     * Returns a job template complete with compute environment, hardware, and constraints.
     *
     * @param computeEnvironmentConfigId the compute environment config id
     * @param hardwareConfigId           the hardware config id
     * @param executionScope             the execution scope to verify the compute environment config and hardware are
     *                                   available
     * @return a job template complete with compute environment, hardware, and constraints
     */
    @Override
    public JobTemplate resolve(Long computeEnvironmentConfigId, Long hardwareConfigId, Map<Scope, String> executionScope) {
        if (computeEnvironmentConfigId == null || hardwareConfigId == null || executionScope == null) {
            throw new IllegalArgumentException("One or more parameters is null");
        }

        if (!isAvailable(computeEnvironmentConfigId, hardwareConfigId, executionScope)) {
            throw new IllegalArgumentException("JobTemplate resolution failed for computeEnvironmentConfigId " + computeEnvironmentConfigId + " and hardwareConfigId " + hardwareConfigId + " and executionScope " + executionScope);
        }

        ComputeEnvironmentConfig computeEnvironmentConfig = computeEnvironmentConfigService
                .retrieve(computeEnvironmentConfigId)
                .orElseThrow(() -> new IllegalArgumentException("ComputeEnvironmentConfig with id " + computeEnvironmentConfigId + " does not exist"));

        HardwareConfig hardwareConfig = hardwareConfigService
                .retrieve(hardwareConfigId)
                .orElseThrow(() -> new IllegalArgumentException("HardwareConfig with id " + hardwareConfigId + " does not exist"));

        List<Constraint> constraints = constraintConfigService.getAvailable(executionScope).stream()
                .map(ConstraintConfig::getConstraint)
                .collect(Collectors.toList());

        JobTemplate jobTemplate = JobTemplate.builder()
                .computeEnvironment(computeEnvironmentConfig.getComputeEnvironment())
                .hardware(hardwareConfig.getHardware())
                .constraints(constraints)
                .build();

        return jobTemplate;
    }
}
