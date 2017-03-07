/*
 * web: org.nrg.xnat.configuration.DicomImportConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import org.nrg.dcm.DicomFileNamer;
import org.nrg.dcm.DicomSCPManager;
import org.nrg.dcm.id.ClassicDicomObjectIdentifier;
import org.nrg.dcm.id.TemplatizedDicomFileNamer;
import org.nrg.dcm.preferences.DicomSCPPreference;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.utils.XnatUserProvider;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.*;

@Configuration
@ComponentScan("org.nrg.dcm.preferences")
public class DicomImportConfig {
    @Bean
    @Primary
    public DicomObjectIdentifier<XnatProjectdata> dicomObjectIdentifier(final MessageSource messageSource, final XnatUserProvider receivedFileUserProvider) {
        final String name = messageSource.getMessage("dicomConfig.defaultObjectIdentifier", new Object[]{ClassicDicomObjectIdentifier.class.getSimpleName()}, "Default DICOM object identifier ({0})", Locale.getDefault());
        return new ClassicDicomObjectIdentifier(name, receivedFileUserProvider);
    }

    @Bean
    public DicomFileNamer dicomFileNamer() throws Exception {
        return new TemplatizedDicomFileNamer("${StudyInstanceUID}-${SeriesNumber}-${InstanceNumber}-${HashSOPClassUIDWithSOPInstanceUID}");
    }

    @Bean
    public DicomSCPManager dicomSCPManager(final DicomSCPPreference dicomScpPreferences, final SiteConfigPreferences siteConfigPreferences, final DicomObjectIdentifier<XnatProjectdata> primaryDicomObjectIdentifier, final Map<String, DicomObjectIdentifier<XnatProjectdata>> dicomObjectIdentifiers) throws Exception {
        return new DicomSCPManager(dicomScpPreferences, siteConfigPreferences, primaryDicomObjectIdentifier, dicomObjectIdentifiers);
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
