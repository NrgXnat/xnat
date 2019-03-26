package org.nrg.xnat.archive.operations;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.nrg.action.ClientException;
import org.nrg.dcm.Decompress;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.dicomtools.filters.DicomFilterService;
import org.nrg.dicomtools.filters.SeriesImportFilter;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.framework.status.StatusProducer;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.Files;
import org.nrg.xnat.processors.ArchiveProcessor;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.processor.services.ArchiveProcessorInstanceService;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.services.cache.UserProjectCache;
import org.restlet.data.Status;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Slf4j
public abstract class AbstractDicomImportOperation extends StatusProducer implements DicomImportOperation {
    public AbstractDicomImportOperation(final Object control, final UserI user, final Map<String, Object> parameters, final FileWriterWrapperI fileWriter, final DicomObjectIdentifier<XnatProjectdata> identifier, final DicomFileNamer namer, final MizerService mizer, final DicomFilterService filterService, final List<ArchiveProcessor> processors, final ArchiveProcessorInstanceService processorInstanceService) {
        super(control);
        _user = user;
        _parameters = parameters;
        _cache = XDAT.getContextService().getBean(UserProjectCache.class);
        _fileWriter = fileWriter;
        _namer = namer;
        _identifier = identifier;
        _mizer = mizer;
        _filterService = filterService;
        _processors = processors;
        _processorsMap = null;
        _processorInstanceService = processorInstanceService;
    }

    public abstract List<String> call() throws Exception;

    protected ArchiveProcessorInstanceService getProcessorInstanceService() {
        return _processorInstanceService;
    }

    protected DicomFilterService getFilterService() {
        return _filterService;
    }

    protected MizerService getMizer() {
        return _mizer;
    }

    protected UserProjectCache getCache() {
        return _cache;
    }

    protected FileWriterWrapperI getFileWriter() {
        return _fileWriter;
    }

    protected UserI getUser() {
        return _user;
    }

    protected Map<String, Object> getParameters() {
        return _parameters;
    }

    protected List<ArchiveProcessor> getProcessors() {
        return _processors;
    }

    protected Map<Class<? extends ArchiveProcessor>, ArchiveProcessor> getProcessorsMap() {
        if(_processorsMap==null){
            _processorsMap = new HashMap<>();
            for(ArchiveProcessor processor:_processors){
                if(processor!=null){
                    _processorsMap.put(processor.getClass(),processor);
                }
            }
        }
        return _processorsMap;
    }

    protected DicomObjectIdentifier<XnatProjectdata> getIdentifier() {
        return _identifier;
    }

    protected DicomFileNamer getNamer() {
        return _namer;
    }

    protected XnatProjectdata getProject(final String alias, final Callable<XnatProjectdata> lookupProject) {
        if (null != alias) {
            log.debug("looking for project matching alias {} from query parameters", alias);
            final XnatProjectdata project = _cache.get(_user, alias);
            if (project != null) {
                log.info("Storage request specified project or alias {}, found accessible project {}", alias, project.getId());
                return project;
            }
            log.info("storage request specified project {}, which does not exist or user does not have create perms", alias);
        } else {
            log.trace("no project alias found in query parameters");
        }

        // No alias, or we couldn't match it to a project. Run the identifier to see if that can get a project name/alias.
        // (Don't cache alias->identifier-derived-project because we didn't use the alias to derive the project.)
        try {
            return null == lookupProject ? null : lookupProject.call();
        } catch (Throwable t) {
            log.error("error in project lookup", t);
            return null;
        }
    }

    protected boolean shouldIncludeDicomObject(final SeriesImportFilter filter, final DicomObject dicom) {
        // If we don't have a filter or the filter is turned off, then we include the DICOM object by default (no filtering)
        if (filter == null || !filter.isEnabled()) {
            return true;
        }
        final boolean shouldInclude = filter.shouldIncludeDicomObject(dicom);
        if (log.isDebugEnabled()) {
            final String association = StringUtils.isBlank(filter.getProjectId()) ? "site" : "project " + filter.getProjectId();
            log.debug("The series import filter for " + association + " indicated a DICOM object from series \"" + dicom.get(Tag.SeriesDescription).getString(dicom.getSpecificCharacterSet(), true) + "\" " + (shouldInclude ? "should" : "shouldn't") + " be included.");
        }
        return shouldInclude;
    }

    protected PrearchiveCode shouldAutoArchive(final XnatProjectdata project, final DicomObject o) {
        if (null == project) {
            return null;
        }
        Boolean fromDicomObject = getIdentifier().requestsAutoarchive(o);
        if (fromDicomObject != null) {
            return fromDicomObject ? PrearchiveCode.AutoArchive : PrearchiveCode.Manual;
        }
        ArcProject arcProject = project.getArcSpecification();
        if (arcProject == null) {
            log.warn("Tried to get the arc project from project {}, but got null in return. Returning null for the prearchive code, but it's probably not good that the arc project wasn't found.", project.getId());
            return null;
        }
        return PrearchiveCode.code(arcProject.getPrearchiveCode());
    }

    protected static boolean initializeCanDecompress() {
        try {
            return Decompress.isSupported();
        } catch (NoClassDefFoundError error) {
            return false;
        }
    }

    protected static <K, V> String getString(final Map<K, V> m, final K k, final V defaultValue) {
        final V v = m.get(k);
        if (null == v) {
            return null == defaultValue ? null : defaultValue.toString();
        } else {
            return v.toString();
        }
    }

    protected static DicomObject read(final InputStream in, final String name) throws ClientException {
        try (final BufferedInputStream bis = new BufferedInputStream(in);
             final DicomInputStream dis = new DicomInputStream(bis)) {
            final DicomObject o = dis.readDicomObject();
            if (Strings.isNullOrEmpty(o.getString(Tag.SOPClassUID))) {
                throw new ClientException("object " + name + " contains no SOP Class UID");
            }
            if (Strings.isNullOrEmpty(o.getString(Tag.SOPInstanceUID))) {
                throw new ClientException("object " + name + " contains no SOP Instance UID");
            }
            return o;
        } catch (IOException e) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "unable to parse or close DICOM object", e);
        }
    }

    protected static void write(final DicomObject fmi, final DicomObject dataset, final InputStream remainder, final File file, final String source, final SessionData sessionData)
            throws ClientException, IOException {
        IOException                ioexception = null;
        final FileOutputStream     fos         = new FileOutputStream(file);
        final BufferedOutputStream bos         = new BufferedOutputStream(fos);
        try {
            final DicomOutputStream dos = new DicomOutputStream(bos);
            try {
                final String tsuid = fmi.getString(Tag.TransferSyntaxUID, DEFAULT_TRANSFER_SYNTAX);
                try {
                    if (Decompress.needsDecompress(tsuid) && canDecompress) {
                        try {
                            // Read the rest of the object into memory so the pixel data can be decompressed.
                            final DicomInputStream dis = new DicomInputStream(remainder, tsuid);
                            try {
                                dis.readDicomObject(dataset, -1);
                            } catch (IOException e) {
                                ioexception = e;
                                throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,
                                        "error parsing DICOM object", e);
                            }
                            final ByteArrayInputStream bis    = new ByteArrayInputStream(Decompress.dicomObject2Bytes(dataset, tsuid));
                            final DicomObject          dicomImageData      = Decompress.decompress_image(bis, tsuid);
                            final String               dtsdui = Decompress.getTsuid(dicomImageData);
                            try {
                                fmi.putString(Tag.TransferSyntaxUID, VR.UI, dtsdui);
                                dos.writeFileMetaInformation(fmi);
                                dos.writeDataset(dicomImageData.dataset(), dtsdui);
                            } catch (Throwable t) {
                                if (t instanceof IOException) {
                                    ioexception = (IOException) t;
                                } else {
                                    log.error("Unable to write decompressed dataset", t);
                                }
                                try {
                                    dos.close();
                                } catch (IOException e) {
                                    throw ioexception = null == ioexception ? e : ioexception;
                                }
                            }
                        } catch (ClientException e) {
                            throw e;
                        } catch (Throwable t) {
                            log.error("Decompression failed; storing in original format " + tsuid, t);
                            dos.writeFileMetaInformation(fmi);
                            dos.writeDataset(dataset, tsuid);
                            if (null != remainder) {
                                final long copied = ByteStreams.copy(remainder, bos);
                                log.trace("copied {} additional bytes to {}", copied, file);
                            }
                        }
                    } else {
                        dos.writeFileMetaInformation(fmi);
                        dos.writeDataset(dataset, tsuid);
                        if (null != remainder) {
                            final long copied = ByteStreams.copy(remainder, bos);
                            log.trace("copied {} additional bytes to {}", copied, file);
                        }
                    }
                } catch (NoClassDefFoundError t) {
                    log.error("Unable to check compression status; storing in original format " + tsuid, t);
                    dos.writeFileMetaInformation(fmi);
                    dos.writeDataset(dataset, tsuid);
                    if (null != remainder) {
                        final long copied = ByteStreams.copy(remainder, bos);
                        log.trace("copied {} additional bytes to {}", copied, file);
                    }
                }
            } catch (IOException e) {
                throw ioexception = null == ioexception ? e : ioexception;
            } finally {
                try {
                    dos.close();
                    LoggerFactory.getLogger("org.nrg.xnat.received").info("{}:{}", source, file);
                } catch (IOException e) {
                    throw null == ioexception ? e : ioexception;
                }
            }
        } catch (IOException e) {
            throw ioexception = e;
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                throw null == ioexception ? e : ioexception;
            }
        }
    }

    protected File getSafeFile(File sessionDir, String scan, String name, DicomObject o, boolean forceRename) {
        String fileName = getNamer().makeFileName(o);
        while (fileName.charAt(0) == '.') {
            fileName = fileName.substring(1);
        }
        final File safeFile = Files.getImageFile(sessionDir, scan, fileName);
        if (forceRename) {
            return safeFile;
        }
        final String valname = Files.toFileNameChars(name);
        if (!Files.isValidFilename(valname)) {
            return safeFile;
        }
        final File reqFile = Files.getImageFile(sessionDir, scan, valname);
        if (reqFile.exists()) {
            try (final FileInputStream fin = new FileInputStream(reqFile)) {
                final DicomObject o1 = read(fin, name);
                if (Objects.equal(o.get(Tag.SOPInstanceUID), o1.get(Tag.SOPInstanceUID)) &&
                        Objects.equal(o.get(Tag.SOPClassUID), o1.get(Tag.SOPClassUID))) {
                    return reqFile;  // object are equivalent; ok to overwrite
                } else {
                    return safeFile;
                }
            } catch (Throwable t) {
                return safeFile;
            }
        } else {
            return reqFile;
        }
    }

    protected static final String  DEFAULT_TRANSFER_SYNTAX = TransferSyntax.ImplicitVRLittleEndian.uid();
    protected static final String  RENAME_PARAM            = "rename";
    protected static final boolean canDecompress           = initializeCanDecompress();

    private final UserProjectCache                       _cache;
    private final FileWriterWrapperI                     _fileWriter;
    private final UserI                                  _user;
    private final Map<String, Object>                    _parameters;
    private final DicomObjectIdentifier<XnatProjectdata> _identifier;
    private final DicomFileNamer                         _namer;
    private final MizerService                           _mizer;
    private final List<ArchiveProcessor>                 _processors;
    private final DicomFilterService                     _filterService;
    private final ArchiveProcessorInstanceService        _processorInstanceService;

    private Map<Class<? extends ArchiveProcessor>, ArchiveProcessor> _processorsMap;
}
