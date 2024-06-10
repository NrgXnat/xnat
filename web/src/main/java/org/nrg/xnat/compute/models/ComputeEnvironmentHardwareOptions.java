package org.nrg.xnat.compute.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ComputeEnvironmentHardwareOptions {

    private boolean allowAllHardware;
    private Set<HardwareConfig> hardwareConfigs;

}
