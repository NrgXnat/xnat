package org.nrg.xnat.compute.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Hardware {

    @ApiModelProperty(position = 0) private String name;
    @ApiModelProperty(position = 1) private Double cpuLimit;
    @ApiModelProperty(position = 2) private Double cpuReservation;
    @ApiModelProperty(position = 3) private String memoryLimit;
    @ApiModelProperty(position = 4) private String memoryReservation;
    @ApiModelProperty(position = 5) private List<Constraint> constraints;
    @ApiModelProperty(position = 6) private List<EnvironmentVariable> environmentVariables;
    @ApiModelProperty(position = 7) private List<GenericResource> genericResources;

}
