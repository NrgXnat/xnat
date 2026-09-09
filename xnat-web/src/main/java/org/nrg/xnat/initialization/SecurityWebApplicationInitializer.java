package org.nrg.xnat.initialization;

import jakarta.servlet.DispatcherType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

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

    /**
     * Jakarta cutover: keep the Spring Security 5.7 dispatcher set. Spring Security 6 added
     * FORWARD and INCLUDE to the default, which routes every RequestDispatcher.include through
     * the filter chain — and ChannelProcessingFilter treats an already-committed response as
     * "decide() just redirected" and returns without invoking the rest of the chain. Any include
     * performed after the outer page commits (every /page/* JSP content include — the page header
     * alone exceeds the 8K buffer) was silently dropped, blanking the admin/settings SPAs.
     * XNAT never filtered includes/forwards before the cutover; preserve that behavior.
     */
    @Override
    protected EnumSet<DispatcherType> getSecurityDispatcherTypes() {
        return EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR, DispatcherType.ASYNC);
    }
}
