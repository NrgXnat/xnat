package org.nrg.xdat.preferences;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.configuration.SerializerConfig;
import org.nrg.framework.services.SerializerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Configuration
@Import(SerializerConfig.class)
public class PreferenceAccessTestConfig {
    public static final String TEST_PREFERENCE_ACCESS_TOOL_ID = "testPreferenceAccessToolId";

    @Bean
    public PreferenceAccess testPreferenceAccess(final SerializerService serializer) throws IOException {
        return new TestPreferenceAccess(serializer);
    }

    @Component
    @Slf4j
    private static class TestPreferenceAccess extends AbstractJsonBasedPreferenceAccess {
        public TestPreferenceAccess(final SerializerService serializer) throws IOException {
            super(serializer, "classpath:META-INF/xnat/security/test-preference-access.yaml");
        }

        @Override
        public String getPreferenceTool() {
            return TEST_PREFERENCE_ACCESS_TOOL_ID;
        }

    }
}
