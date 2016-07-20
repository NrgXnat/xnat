package org.nrg.notify.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.nrg.notify.daos", "org.nrg.notify.services.impl"})
public class NrgNotifyConfiguration {
}
