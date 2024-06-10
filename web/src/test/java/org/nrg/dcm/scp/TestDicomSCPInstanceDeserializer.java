package org.nrg.dcm.scp;

import org.assertj.core.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.services.SerializerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestDicomSCPInstanceDeserializerConfig.class)
public class TestDicomSCPInstanceDeserializer {
    private static final String INSTANCE_ENABLED_TRUE  = "{\"port\": 8104, \"id\": 1, \"aeTitle\": \"XNAT\", \"enabled\": true}";
    private static final String INSTANCE_ENABLED_FALSE = "{\"port\": 8104, \"id\": 1, \"aeTitle\": \"XNAT\", \"enabled\": false}";
    private static final String INSTANCE_NO_ENABLED    = "{\"port\": 8104, \"id\": 1, \"aeTitle\": \"XNAT\"}";
    private static final String INSTANCE_WHITELIST     = "{\"port\": 8104, \"id\": 1, \"aeTitle\": \"XNAT\", \"enabled\": true, \"whitelist\": [\"FOO\",\"SNAFU\"]}";

    @Test
    public void testEnabledTrueByDefault() throws IOException {
        final DicomSCPInstance instanceEnabledTrue  = _serializer.deserializeJson(INSTANCE_ENABLED_TRUE, DicomSCPInstance.class);
        final DicomSCPInstance instanceEnabledFalse = _serializer.deserializeJson(INSTANCE_ENABLED_FALSE, DicomSCPInstance.class);
        final DicomSCPInstance instanceNoEnabled    = _serializer.deserializeJson(INSTANCE_NO_ENABLED, DicomSCPInstance.class);
        final DicomSCPInstance instanceWhitelist    = _serializer.deserializeJson(INSTANCE_WHITELIST, DicomSCPInstance.class);
        assertThat(instanceEnabledTrue).isNotNull().hasFieldOrPropertyWithValue("enabled", true);
        assertThat(instanceEnabledFalse).isNotNull().hasFieldOrPropertyWithValue("enabled", false);
        assertThat(instanceNoEnabled).isNotNull().hasFieldOrPropertyWithValue("enabled", true);
        assertThat(instanceWhitelist).isNotNull().hasFieldOrPropertyWithValue("enabled", true);
    }

    @Test
    public void testSerialize() {
        try {
            final DicomSCPInstance instance = new DicomSCPInstance();
            instance.setWhitelist( Stream.of("foo", "bar").collect(Collectors.toList()));

            String json = _serializer.toJson( instance);

            assertThat( json).isNotNull().contains("enabled");
        } catch (IOException e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Autowired
    private SerializerService _serializer;
}
