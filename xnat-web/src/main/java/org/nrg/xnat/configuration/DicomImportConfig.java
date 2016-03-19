package org.nrg.xnat.configuration;

import org.nrg.dcm.DicomFileNamer;
import org.nrg.dcm.DicomSCPManager;
import org.nrg.dcm.id.ClassicDicomObjectIdentifier;
import org.nrg.dcm.id.TemplatizedDicomFileNamer;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.utils.XnatUserProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@ComponentScan("org.nrg.dcm.preferences")
public class DicomImportConfig {
    @Bean
    public DicomObjectIdentifier<XnatProjectdata> dicomObjectIdentifier(final XnatUserProvider provider) {
        final ClassicDicomObjectIdentifier identifier = new ClassicDicomObjectIdentifier();
        identifier.setUserProvider(provider);
        return identifier;
    }

    @Bean
    public DicomFileNamer dicomFileNamer() throws Exception {
        return new TemplatizedDicomFileNamer("${StudyInstanceUID}-${SeriesNumber}-${InstanceNumber}-${HashSOPClassUIDWithSOPInstanceUID}");
    }
    
    @Bean
    public DicomSCPManager dicomSCPManager(final XnatUserProvider provider) throws Exception {
        return new DicomSCPManager(provider);
    }

    @Bean
    public List<String> sessionDataFactoryClasses() {
        return new ArrayList<>();
    }

    @Bean
    public List<String> excludedDicomImportFields() {
        return Arrays.asList("SOURCE", "separatePetMr", "prearchivePath");
    }
}
