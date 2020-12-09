package org.nrg.xnat.services.system;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.nrg.framework.configuration.FrameworkConfig;
import org.nrg.framework.configuration.SerializerConfig;
import org.nrg.framework.datacache.SerializerRegistry;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.nrg.xnat.services.archive.impl.hibernate.DicomInboxImportRequestDAO;
import org.nrg.xnat.services.archive.impl.hibernate.HibernateDicomInboxImportRequestService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@Import({OrmTestConfiguration.class, FrameworkConfig.class, SerializerConfig.class})
@TestPropertySource(locations= "classpath:/test.properties")
public class TestDicomInboxImportRequestServiceConfig {
    @Bean
    public DicomInboxImportRequestDAO dicomInboxImportRequestDAO() {
        return new DicomInboxImportRequestDAO();
    }

    @Bean
    public DicomInboxImportRequestService dicomInboxImportRequestService() {
        return new HibernateDicomInboxImportRequestService();
    }

    @Bean
    public PrettyPrinter prettyPrinter() {
        return new DefaultPrettyPrinter() {{
            final DefaultIndenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
            indentObjectsWith(indenter);
            indentArraysWith(indenter);
        }};
    }
    @Bean
    public SerializerRegistry serializerRegistry() {
        return new SerializerRegistry();
    }
}
