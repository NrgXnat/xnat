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
import org.nrg.dcm.scp.daos.DicomSCPInstanceService;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.preferences.HandlePetMr;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.services.cache.UserProjectCache;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Configuration
@ComponentScan({"org.nrg.dcm.scp", "org.nrg.dcm.edit.mizer", "org.nrg.dicom.dicomedit.mizer", "org.nrg.dicom.mizer.service.impl", "org.nrg.xnat.services.messaging.archive"})
public class DicomImportConfig {
    @Bean
    @Primary
    public DicomObjectIdentifier<XnatProjectdata> dicomObjectIdentifier(final MessageSource messageSource,
                                                                        final XnatUserProvider receivedFileUserProvider,
                                                                        final UserProjectCache userProjectCache) {
        final String name = messageSource.getMessage("dicomConfig.defaultObjectIdentifier", new Object[]{ClassicDicomObjectIdentifier.class.getSimpleName()}, "Default DICOM object identifier ({0})", Locale.getDefault());
        return new ClassicDicomObjectIdentifier(name, receivedFileUserProvider, userProjectCache);
    }

    @Bean
    public DicomFileNamer dicomFileNamer(final SiteConfigPreferences preferences) {
        return new TemplatizedDicomFileNamer(preferences.getDicomFileNameTemplate());
    }

    @Bean
    public List<String> sessionDataFactoryClasses() {
        return Collections.emptyList();
    }

    @Bean
    public List<String> excludedDicomImportFields() {
        return HandlePetMr.DEFAULT_EXCLUDED_FIELDS;
    }
}
