/*
 * web: org.nrg.xnat.configuration.DicomImportConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import lombok.extern.slf4j.Slf4j;
import org.nrg.dcm.id.ClassicDicomObjectIdentifier;
import org.nrg.dicom.mizer.service.StagingDirectoryResolver;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.preferences.HandlePetMr;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.helpers.merge.anonymize.SameVolumeStagingDirectoryResolver;
import org.nrg.xnat.services.cache.UserProjectCache;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Configuration
@ComponentScan({"org.nrg.dcm.scp", "org.nrg.dcm.id", "org.nrg.dcm.edit.mizer", "org.nrg.dicom.dicomedit.mizer", "org.nrg.dicom.mizer.service.impl", "org.nrg.xnat.services.messaging.archive"})
@Slf4j
public class DicomImportConfig {
    public DicomImportConfig() {
        log.info("Creating DicomImportConfig");
    }

    @Bean
    @Primary
    public DicomObjectIdentifier<XnatProjectdata> dicomObjectIdentifier(final MessageSource messageSource,
                                                                        final XnatUserProvider receivedFileUserProvider,
                                                                        final UserProjectCache userProjectCache) {
        final String name = messageSource.getMessage("dicomConfig.defaultObjectIdentifier", new Object[]{ClassicDicomObjectIdentifier.class.getSimpleName()}, "Default DICOM object identifier ({0})", Locale.getDefault());
        return new ClassicDicomObjectIdentifier(name, receivedFileUserProvider, userProjectCache);
    }

    /**
     * Where in-place anonymization stages its output. Staging on the volume the file is on lets the
     * mizer put the anonymized file in place with a rename instead of a copy. See
     * {@link SameVolumeStagingDirectoryResolver} for why the staging directory sits at the data root.
     */
    @Bean
    public StagingDirectoryResolver stagingDirectoryResolver(final SiteConfigPreferences preferences) {
        return new SameVolumeStagingDirectoryResolver(() -> Arrays.asList(preferences.getArchivePath(), preferences.getPrearchivePath()));
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
