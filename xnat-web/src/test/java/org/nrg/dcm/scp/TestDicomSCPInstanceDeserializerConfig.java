package org.nrg.dcm.scp;

import org.nrg.framework.configuration.SerializerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SerializerConfig.class)
public class TestDicomSCPInstanceDeserializerConfig {
}
