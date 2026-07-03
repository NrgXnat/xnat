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
public class ConstraintConfig {

    private Long id;
    private Constraint constraint;
    private Map<Scope, ConstraintScope> scopes;

}
