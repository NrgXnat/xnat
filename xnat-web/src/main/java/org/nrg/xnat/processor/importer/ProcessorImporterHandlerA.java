/*
 * web: org.nrg.xnat.processor.importer.ProcessorImporterHandlerA
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.processor.importer;

import lombok.extern.slf4j.Slf4j;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dcm.xnat.SOPHashDicomFileNamer;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.dicomtools.filters.DicomFilterService;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.archive.operations.DicomImportOperation;
import org.nrg.xnat.processors.ArchiveProcessor;
import org.nrg.xnat.processor.services.ArchiveProcessorInstanceService;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
@Slf4j
public abstract class ProcessorImporterHandlerA {
    protected ProcessorImporterHandlerA(final DicomObjectIdentifier<XnatProjectdata> identifier, final DicomFileNamer namer, final List<ArchiveProcessor> processors, final DicomFilterService filterService, final MizerService mizer, final ArchiveProcessorInstanceService processorInstanceService) {
        _identifier = identifier;
        _namer = namer;
        _processors = processors;
        _filterService = filterService;
        _mizer = mizer;
        _processorInstanceService = processorInstanceService;
    }

    public abstract DicomImportOperation getOperation(final Object listenerControl, final UserI user, final FileWriterWrapperI writer, final Map<String, Object> params, final DicomObjectIdentifier<XnatProjectdata> identifier, final DicomFileNamer namer);

    public DicomImportOperation getOperation(final Object listenerControl, final UserI user, final FileWriterWrapperI writer, final Map<String, Object> params) {
        return getOperation(listenerControl, user, writer, params, getIdentifier(), getNamer());
    }

    public List<String> doImport(final Object listenerControl, final UserI user, final FileWriterWrapperI writer, final Map<String, Object> params) throws Exception {
        return doImport(listenerControl, user, writer, params, null, null);
    }

    public List<String> doImport(final Object listenerControl, final UserI user, final FileWriterWrapperI writer, final Map<String, Object> params, final DicomObjectIdentifier<XnatProjectdata> identifier, final DicomFileNamer namer) throws Exception {
        return doImport(getOperation(listenerControl, user, writer, params, identifier, namer));
    }

    public List<String> doImport(final DicomImportOperation operation) throws Exception {
        return operation.call();
    }

//    @Async
//    public Future<List<String>> doImport(final Object listenerControl, final UserI user, final FileWriterWrapperI writer, final Map<String, Object> params) throws Exception {
//        return doImport(listenerControl, user, writer, params, null, null);
//    }
//
//    @Async
//    public Future<List<String>> doImport(final Object listenerControl, final UserI user, final FileWriterWrapperI writer, final Map<String, Object> params, final DicomObjectIdentifier<XnatProjectdata> identifier, final DicomFileNamer namer) throws Exception {
//        return doImport(getOperation(listenerControl, user, writer, params, identifier, namer));
//    }
//
//    @Async
//    public Future<List<String>> doImport(final DicomImportOperation operation) throws Exception {
//        return new AsyncResult<>(operation.call());
//    }

    public DicomObjectIdentifier<XnatProjectdata> getIdentifier() {
        return _identifier;
    }

    public DicomFileNamer getNamer() {
        if (_namer == null) {
            return DEFAULT_NAMER;
        }
        return _namer;
    }

    public List<ArchiveProcessor> getProcessors() {
        return _processors;
    }

    public DicomFilterService getFilterService() {
        return _filterService;
    }

    public MizerService getMizer() {
        return _mizer;
    }

    public ArchiveProcessorInstanceService getProcessorService() {
        return _processorInstanceService;
    }

    public static final String IMPORT_HANDLER_ATTR = "import-handler";

    public static final String SESSION_IMPORTER       = "SI";
    public static final String XAR_IMPORTER           = "XAR";
    public static final String GRADUAL_DICOM_IMPORTER = "processor-DICOM";
    public static final String DICOM_ZIP_IMPORTER     = "DICOM-zip";
    public static final String BLANK_PREARCHIVE_ENTRY = "blank";

    static       String                                         DEFAULT_HANDLER = SESSION_IMPORTER;
    final static Map<String, Class<? extends ProcessorImporterHandlerA>> IMPORTERS       = new HashMap<>();

    private static final String   PROP_OBJECT_IDENTIFIER = "org.nrg.import.handler.impl";
    private static final String   IMPORTER_PROPERTIES    = "importer.properties";
    private static final String   CLASS_NAME             = "className";
    private static final String[] PROP_OBJECT_FIELDS     = new String[]{CLASS_NAME};

    /**
     * This method was added to allow other developers to manually add importers to the list, without adding a
     * configuration file.  However, this would some how need to be done before the import is executed (maybe as a
     * servlet?).
     *
     * @return A map of the currently configured importers.
     */
    @SuppressWarnings("unused")
    public static Map<String, Class<? extends ProcessorImporterHandlerA>> getImporters() {
        return IMPORTERS;
    }

    private static final DicomFileNamer DEFAULT_NAMER = new SOPHashDicomFileNamer();

    private final DicomObjectIdentifier<XnatProjectdata>  _identifier;
    private final DicomFileNamer         _namer;
    private final List<ArchiveProcessor> _processors;
    private final DicomFilterService     _filterService;
    private final MizerService           _mizer;
    private final ArchiveProcessorInstanceService _processorInstanceService;
}
