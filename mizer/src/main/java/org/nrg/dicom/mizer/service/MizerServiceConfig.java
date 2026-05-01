package org.nrg.dicom.mizer.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration file for the {@link MizerService}. In this implementation, {@link MizerService} is a Spring Service
 * and {@link Mizer Mizers} are Spring Components that are automatically discovered and injected into the the Service.
 */
@Configuration
@ComponentScan
public class MizerServiceConfig {

}
