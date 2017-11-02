package org.nrg.xnat.initialization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {
    public SecurityWebApplicationInitializer() {
        super();
        log.info("Initializing the web application security infrastructure.");
    }

    @Override
    protected boolean enableHttpSessionEventPublisher() {
        return true;
    }
}
