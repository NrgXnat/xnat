/*
 * web: org.nrg.xapi.configuration.RestApiConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.snapshot.rest;

import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.snapshot.generator.SnapshotResourceGenerator;
import org.nrg.xnat.snapshot.generator.impl.SnapshotResourceGeneratorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
//@ComponentScan(value = {"org.nrg.xapi", "org.nrg.xnat.snapshot.convert"})
public class SnapshotGenerationApiConfig {
    @Autowired
    CatalogService catalogService;

    @Bean
    public SnapshotResourceGenerator getSnapshotResourceGenerator() throws IOException {
        return new SnapshotResourceGeneratorImpl( catalogService);
    }

}
