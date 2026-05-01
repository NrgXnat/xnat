/*
 * dicom-edit4: org.nrg.dcm.mizer.TestMizerConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.service.impl;

import org.nrg.dicom.mizer.service.Mizer;
import org.nrg.dicom.mizer.service.impl.test.TestMizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.nrg.dicom.mizer.service.impl"})
public class TestMizerConfig {

    @Bean
    public Mizer createTestMizer() {
        return new TestMizer();
    }

}
