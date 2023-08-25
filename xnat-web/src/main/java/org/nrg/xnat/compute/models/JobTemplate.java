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
public class JobTemplate {

    private ComputeEnvironment computeEnvironment;
    private Hardware hardware;
    private List<Constraint> constraints;

}
