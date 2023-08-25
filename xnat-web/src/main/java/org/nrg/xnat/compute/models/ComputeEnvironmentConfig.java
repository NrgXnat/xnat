package org.nrg.xnat.compute.models;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(position = 0) private Long id;
    @ApiModelProperty(position = 1) private Set<ConfigType> configTypes;
    @ApiModelProperty(position = 2) private ComputeEnvironment computeEnvironment;
    @ApiModelProperty(position = 3) private Map<Scope, ComputeEnvironmentScope> scopes;
    @ApiModelProperty(position = 4) private ComputeEnvironmentHardwareOptions hardwareOptions;

    public enum ConfigType {
        JUPYTERHUB,
        CONTAINER_SERVICE,
        GENERAL
    }
}
