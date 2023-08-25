package org.nrg.xnat.compute.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.nrg.xnat.compute.services.impl",
                "org.nrg.xnat.compute.services",
                "org.nrg.xnat.compute.rest",
                "org.nrg.xnat.compute.repositories"})
public class ComputeConfig {
}
