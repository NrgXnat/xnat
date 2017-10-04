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
import org.nrg.dcm.id.ClassicDicomObjectIdentifier;
import org.nrg.dcm.id.TemplatizedDicomFileNamer;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.services.cache.UserProjectCache;
import org.nrg.xnat.utils.XnatUserProvider;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.*;

@Configuration
@ComponentScan({"org.nrg.dcm.scp", "org.nrg.dcm.edit.mizer", "org.nrg.dicom.dicomedit.mizer", "org.nrg.dicom.mizer.service.impl"})
public class DicomImportConfig {
    @Bean
    @Primary
    public DicomObjectIdentifier<XnatProjectdata> dicomObjectIdentifier(final MessageSource messageSource, final XnatUserProvider receivedFileUserProvider, final UserProjectCache userProjectCache) {
        final String name = messageSource.getMessage("dicomConfig.defaultObjectIdentifier", new Object[]{ClassicDicomObjectIdentifier.class.getSimpleName()}, "Default DICOM object identifier ({0})", Locale.getDefault());
        return new ClassicDicomObjectIdentifier(name, receivedFileUserProvider, userProjectCache);
    }

    @Bean
    public DicomFileNamer dicomFileNamer() throws Exception {
        return new TemplatizedDicomFileNamer("${StudyInstanceUID}-${SeriesNumber}-${InstanceNumber}-${HashSOPClassUIDWithSOPInstanceUID}");
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
