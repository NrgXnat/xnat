package org.nrg.xnat.services.system;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import org.nrg.framework.beans.Beans;
import org.nrg.framework.configuration.FrameworkConfig;
import org.nrg.framework.datacache.SerializerRegistry;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.services.SerializerService;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.xnat.services.archive.impl.hibernate.DicomInboxImportRequestDAO;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.nrg.xnat.services.archive.impl.hibernate.HibernateDicomInboxImportRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

@Configuration
@Import({OrmTestConfiguration.class, FrameworkConfig.class})
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
    public Jackson2ObjectMapperBuilder objectMapperBuilder() throws NrgServiceException {
        return new Jackson2ObjectMapperBuilder()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .failOnEmptyBeans(false)
                .mixIns(mixIns())
                .featuresToEnable(JsonParser.Feature.ALLOW_SINGLE_QUOTES, JsonParser.Feature.ALLOW_YAML_COMMENTS)
                .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS, SerializationFeature.WRITE_NULL_MAP_VALUES)
                .modulesToInstall(_jacksonModules);
    }

    @Bean
    public Module hibernateModule() {
        return new Hibernate4Module();
    }

    @Bean
    public Map<Class<?>, Class<?>> mixIns() throws NrgServiceException {
        return Beans.getMixIns();
    }

    @Bean
    public SerializerService serializerService(final Jackson2ObjectMapperBuilder objectMapperBuilder) {
        return new SerializerService(objectMapperBuilder);
    }

    @Bean
    public SerializerRegistry serializerRegistry() {
        return new SerializerRegistry();
    }

    @Autowired
    private Module[] _jacksonModules;
}
