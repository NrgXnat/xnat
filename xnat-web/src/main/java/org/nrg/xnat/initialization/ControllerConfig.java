package org.nrg.xnat.initialization;

import org.nrg.xapi.configuration.RestApiConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(RestApiConfig.class)
public class ControllerConfig {
}
