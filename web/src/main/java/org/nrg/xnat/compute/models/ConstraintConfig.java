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
public class ConstraintConfig {

    @ApiModelProperty(position = 0) private Long id;
    @ApiModelProperty(position = 1) private Constraint constraint;
    @ApiModelProperty(position = 2) private Map<Scope, ConstraintScope> scopes;

}
