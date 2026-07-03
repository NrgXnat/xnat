package org.nrg.xnat.compute.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nrg.framework.constants.Scope;

import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class HardwareConfig {

    private Long id;
    private Hardware hardware;
    private Map<Scope, HardwareScope> scopes;

}
