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
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.dicomtools.filters.DicomFilterService;
import org.nrg.xnat.processor.importer.ProcessorGradualDicomImporter;
import org.nrg.xnat.processors.ArchiveProcessor;
import org.nrg.xnat.processor.services.ArchiveProcessorInstanceService;
import org.springframework.context.annotation.*;


import java.util.*;

import org.nrg.dicom.mizer.service.MizerService;

@Configuration
@ComponentScan({"org.nrg.dcm.scp", "org.nrg.dcm.edit.mizer", "org.nrg.dicom.dicomedit.mizer", "org.nrg.dicom.mizer.service.impl"})
public class ProcessorImporterConfig {
    @Bean
    @Primary
    public ProcessorGradualDicomImporter processorGradualDicomImporter(final DicomFilterService filterService, final DicomObjectIdentifier<XnatProjectdata> identifier, final DicomFileNamer namer, final MizerService mizer, final List<ArchiveProcessor> processors, final ArchiveProcessorInstanceService processorInstanceService) {
        return new ProcessorGradualDicomImporter(filterService, identifier, namer, mizer, processors, processorInstanceService);
    }
}
