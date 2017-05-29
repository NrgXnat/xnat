/*
 * web: org.nrg.xnat.dicom.mizer.config.MizerServiceTestConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.dicom.mizer.config;

import org.nrg.dicom.mizer.service.MizerServiceConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by rherrick on 5/28/17.
 */
@Configuration
@Import(MizerServiceConfig.class)
@ComponentScan({"org.nrg.dcm.edit.mizer", "org.nrg.dicom.dicomedit.mizer", "org.nrg.dicom.mizer.service.impl"})
public class MizerServiceTestConfig {
}
