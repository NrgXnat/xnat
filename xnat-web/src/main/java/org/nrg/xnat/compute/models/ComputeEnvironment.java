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
public class ComputeEnvironment {

    private String name;
    private String image;
    private String command;
    private List<EnvironmentVariable> environmentVariables;
    private List<Mount> mounts;

}
