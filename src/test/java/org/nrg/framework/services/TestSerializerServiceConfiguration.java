/*
 * framework: org.nrg.framework.services.TestSerializerServiceConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services;

import org.nrg.framework.configuration.SerializerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SerializerConfig.class)
public class TestSerializerServiceConfiguration {
}
