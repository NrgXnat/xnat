/*
 * web: org.nrg.xnat.dicom.mizer.config.MizerServiceTestConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.dicom.mizer.config;

import org.nrg.dicom.dicomedit.DE6ScriptFactory;
import org.nrg.dicom.dicomedit.ScriptApplicatorFactory;
import org.nrg.dicom.mizer.service.MizerServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(MizerServiceConfig.class)
@ComponentScan({"org.nrg.dcm.edit.mizer", "org.nrg.dicom.dicomedit.mizer", "org.nrg.dicom.mizer.service.impl"})
public class MizerServiceTestConfig {
    @Bean
    public DE6ScriptFactory de6ScriptFactory() {
        return new DE6ScriptFactory();
    }

    @Bean
    public ScriptApplicatorFactory scriptApplicatorFactory(DE6ScriptFactory scriptFactory) {
        return new ScriptApplicatorFactory(scriptFactory);
    }
}
