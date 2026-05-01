package org.nrg.xdat.preferences;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.services.SerializerService;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class SiteConfigAccess extends AbstractJsonBasedPreferenceAccess {
    public SiteConfigAccess(final SerializerService serializer) throws IOException {
        super(serializer, "classpath:META-INF/xnat/security/site-config-access.yaml");
    }

    @Override
    public String getPreferenceTool() {
        return SiteConfigPreferences.SITE_CONFIG_TOOL_ID;
    }
}
