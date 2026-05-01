/*
 * dicom-edit4: org.nrg.dcm.mizer.TestMizerConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.mizer;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.nrg.dcm.edit.mizer", "org.nrg.dicom.mizer.service.impl"})
public class TestMizerConfig {
}
