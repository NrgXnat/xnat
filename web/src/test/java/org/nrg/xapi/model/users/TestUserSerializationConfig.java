/*
 * web: org.nrg.xapi.model.users.TestUserSerializationConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.model.users;

import org.nrg.framework.configuration.SerializerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SerializerConfig.class)
public class TestUserSerializationConfig {
}
