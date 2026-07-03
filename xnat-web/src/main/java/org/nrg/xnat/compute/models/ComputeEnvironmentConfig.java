package org.nrg.xnat.compute.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nrg.framework.constants.Scope;

import java.util.Map;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ComputeEnvironmentConfig {

    private Long id;
    private Set<ConfigType> configTypes;
    private ComputeEnvironment computeEnvironment;
    private Map<Scope, ComputeEnvironmentScope> scopes;
    private ComputeEnvironmentHardwareOptions hardwareOptions;

    public enum ConfigType {
        JUPYTERHUB,
        CONTAINER_SERVICE,
        GENERAL
    }
}
