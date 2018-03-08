/*
 * web: org.nrg.xnat.processor.importer.ProcessorGradualDicomImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.processor.importer;

import org.apache.commons.lang3.ObjectUtils;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.dicomtools.filters.DicomFilterService;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.archive.operations.DicomImportOperation;
import org.nrg.xnat.archive.operations.ProcessorGradualDicomImportOperation;
import org.nrg.xnat.processors.ArchiveProcessor;
import org.nrg.xnat.processor.services.ArchiveProcessorInstanceService;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.processor.importer.ProcessorImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@SuppressWarnings("ThrowFromFinallyBlock")
@Service
@ImporterHandler(handler = ProcessorImporterHandlerA.GRADUAL_DICOM_IMPORTER)
public class ProcessorGradualDicomImporter extends ProcessorImporterHandlerA {
    public static final String SENDER_AE_TITLE_PARAM = "Sender-AE-Title";
    public static final String SENDER_ID_PARAM       = "Sender-ID";
    public static final String TSUID_PARAM           = "Transfer-Syntax-UID";

//    @Autowired
//    public ProcessorGradualDicomImporter(final List<ArchiveProcessor> processors, final DicomFilterService filterService, final DicomObjectIdentifier<XnatProjectdata> identifier, final DicomFileNamer namer, final MizerService mizer) {
//        super(identifier, namer, processors, filterService, mizer);
//    }

    @Autowired
    public ProcessorGradualDicomImporter(final DicomFilterService filterService, final DicomObjectIdentifier<XnatProjectdata> identifier, final DicomFileNamer namer, final MizerService mizer, final List<ArchiveProcessor> processors, final ArchiveProcessorInstanceService processorInstanceService) {
        super(identifier, namer, processors, filterService, mizer, processorInstanceService);
    }

    @Override
    public DicomImportOperation getOperation(final Object listenerControl, final UserI user, final FileWriterWrapperI writer, final Map<String, Object> params, final DicomObjectIdentifier<XnatProjectdata> identifier, final DicomFileNamer namer) {
        return new ProcessorGradualDicomImportOperation(listenerControl, user, writer, params, getProcessors(), getFilterService(), ObjectUtils.defaultIfNull(identifier, getIdentifier()), getMizer(), ObjectUtils.defaultIfNull(namer, getNamer()), getProcessorService());
    }
}
