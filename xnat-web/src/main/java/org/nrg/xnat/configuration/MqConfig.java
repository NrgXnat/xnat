/*
 * web: org.nrg.xnat.configuration.MqConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jms.annotation.EnableJms;

@Configuration
@EnableJms
@ImportResource("WEB-INF/conf/mq-context.xml")
@ComponentScan({"org.nrg.framework.messaging", "org.nrg.xnat.services.messaging"})
public class MqConfig {
}
