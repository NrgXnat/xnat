/*
 * notify: org.nrg.notify.configuration.NrgNotifyConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.nrg.notify.daos", "org.nrg.notify.services.impl"})
public class NrgNotifyConfiguration {
}
