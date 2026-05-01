/*
 * notify: org.nrg.notify.configuration.BasicPlatformTestConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.configuration;

import org.nrg.framework.configuration.FrameworkConfig;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.notify.entities.ChannelRendererProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({OrmTestConfiguration.class, FrameworkConfig.class, NrgNotifyConfiguration.class})
public class BasicPlatformTestConfiguration {
    @Bean
    public ChannelRendererProvider rendererProvider() {
        return new ChannelRendererProvider();
    }
}
