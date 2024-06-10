package org.nrg.xnat.compute.models;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(position = 0) private Long id;
    @ApiModelProperty(position = 1) private Hardware hardware;
    @ApiModelProperty(position = 2) private Map<Scope, HardwareScope> scopes;

}
