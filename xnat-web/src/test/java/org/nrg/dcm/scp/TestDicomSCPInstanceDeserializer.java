package org.nrg.dcm.scp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.services.SerializerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestDicomSCPInstanceDeserializerConfig.class)
public class TestDicomSCPInstanceDeserializer {
    private static final String INSTANCE_ENABLED_TRUE  = "{\"port\": 8104, \"id\": 1, \"aeTitle\": \"XNAT\", \"enabled\": true}";
    private static final String INSTANCE_ENABLED_FALSE = "{\"port\": 8104, \"id\": 1, \"aeTitle\": \"XNAT\", \"enabled\": false}";
    private static final String INSTANCE_NO_ENABLED    = "{\"port\": 8104, \"id\": 1, \"aeTitle\": \"XNAT\"}";

    @Test
    public void testEnabledTrueByDefault() throws IOException {
        final DicomSCPInstance instanceEnabledTrue  = _serializer.deserializeJson(INSTANCE_ENABLED_TRUE, DicomSCPInstance.class);
        final DicomSCPInstance instanceEnabledFalse = _serializer.deserializeJson(INSTANCE_ENABLED_FALSE, DicomSCPInstance.class);
        final DicomSCPInstance instanceNoEnabled    = _serializer.deserializeJson(INSTANCE_NO_ENABLED, DicomSCPInstance.class);
        assertThat(instanceEnabledTrue).isNotNull().hasFieldOrPropertyWithValue("enabled", true);
        assertThat(instanceEnabledFalse).isNotNull().hasFieldOrPropertyWithValue("enabled", false);
        assertThat(instanceNoEnabled).isNotNull().hasFieldOrPropertyWithValue("enabled", true);
    }

    @Autowired
    private SerializerService _serializer;
}
