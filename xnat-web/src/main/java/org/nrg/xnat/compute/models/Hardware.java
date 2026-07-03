package org.nrg.xnat.compute.models;

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

    private String name;
    private Double cpuLimit;
    private Double cpuReservation;
    private String memoryLimit;
    private String memoryReservation;
    private List<Constraint> constraints;
    private List<EnvironmentVariable> environmentVariables;
    private List<GenericResource> genericResources;

}
