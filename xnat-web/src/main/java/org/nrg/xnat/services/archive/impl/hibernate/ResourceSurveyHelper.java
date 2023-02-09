package org.nrg.xnat.services.archive.impl.hibernate;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che3.data.Tag;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dicomtools.utilities.DicomUtils;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.services.SerializerService;
import org.nrg.xnat.entities.ResourceSurveyRequest;
import org.nrg.xnat.services.archive.ResourceSurveyReport;
import org.nrg.xnat.services.archive.ResourceSurveyRequestEntityService;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class ResourceSurveyHelper implements Callable<ResourceSurveyReport> {
    private static final Predicate<DcmCatEntry> MISMATCHED_ENTRY = entry -> !StringUtils.equals(entry.getCalculatedFileName(), entry.getFile().getName());

    private final ResourceSurveyRequestEntityService _service;
    private final ResourceSurveyRequest              _request;
    private final SerializerService                  _serializer;
    private final DicomFileNamer                     _dicomFileNamer;
    private final StopTagInputHandler                _stopTagInputHandler;

    public ResourceSurveyHelper(final ResourceSurveyRequestEntityService service, final ResourceSurveyRequest request, final SerializerService serializer, final DicomFileNamer dicomFileNamer, final StopTagInputHandler stopTagInputHandler) {
        log.debug("Creating a new resource survey helper for resource survey request {} for resource {} for user {}", request.getId(), request.getResourceId(), request.getRequester());
        _service             = service;
        _request             = request;
        _serializer          = serializer;
        _dicomFileNamer      = dicomFileNamer;
        _stopTagInputHandler = stopTagInputHandler;
    }

    @Override
    public ResourceSurveyReport call() {
        final ResourceSurveyReport.ResourceSurveyReportBuilder builder = ResourceSurveyReport.builder().resourceSurveyRequestId(_request.getId());
        builder.resourceSurveyRequestId(_request.getId());

        final Path resourceUri = Paths.get(_request.getResourceUri());
        try (final InputStream input = Files.newInputStream(resourceUri)) {
            final DcmCatEntries handler = new DcmCatEntries(_dicomFileNamer, _stopTagInputHandler, resourceUri);
            _serializer.parse(input, handler);

            final List<DcmCatEntry> entries = handler.getEntries();
            builder.totalEntries(entries.size());

            final Map<Boolean, List<DcmCatEntry>> validAndInvalid = entries.stream().collect(Collectors.partitioningBy(DcmCatEntry::isValidDicomFile));
            final List<DcmCatEntry>               badFiles        = validAndInvalid.get(false);
            if (!badFiles.isEmpty()) {
                builder.badFiles(badFiles.stream().map(DcmCatEntry::getFile).collect(Collectors.toList()));
            }

            final Map<Pair<String, String>, List<DcmCatEntry>> groupedByClassAndInstanceUid = validAndInvalid.get(true).stream().collect(Collectors.groupingBy(entry -> Pair.of(entry.getClassUid(), entry.getInstanceUid())));
            builder.uids(groupedByClassAndInstanceUid.keySet().stream().collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toSet()))));

            final Map<Boolean, Map<Pair<String, String>, List<DcmCatEntry>>> splitOnListSize = groupedByClassAndInstanceUid.entrySet().stream()
                                                                                                                           .collect(Collectors.partitioningBy(entry -> entry.getValue().size() > 1,
                                                                                                                                                              Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            builder.mismatchedFiles(splitOnListSize.get(false).values().stream().map(list -> list.get(0)).filter(MISMATCHED_ENTRY).collect(Collectors.toMap(DcmCatEntry::getFile, DcmCatEntry::getCalculatedFileName)));

            final Map<Pair<String, String>, Map<File, String>> duplicates = splitOnListSize.get(true).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().collect(Collectors.toMap(DcmCatEntry::getFile, DcmCatEntry::getCalculatedFileName))));

            if (duplicates.isEmpty()) {
                log.info("Searched catalog at {} for resource ID {} and found {} entries for the catalog with {} UIDs total. There are no UIDs with duplicated files.", resourceUri, _request.getResourceId(), entries.size(), groupedByClassAndInstanceUid.size());
            } else {
                log.info("Searched catalog at {} for resource ID {} and found {} entries for the catalog with {} UIDs total and {} UIDs with duplicated files", resourceUri, _request.getResourceId(), entries.size(), groupedByClassAndInstanceUid.size(), duplicates.size());
                builder.duplicates(ResourceSurveyReport.unflattenDuplicateMaps(duplicates));
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            try {
                log.error("Got an error trying to run survey for resource survey request {} on resource {}, setting to error state.", _request.getId(), _request.getResourceId(), e);
                final ResourceSurveyRequest request = _service.get(_request.getId());
                request.setRsnStatus(ResourceSurveyRequest.Status.ERROR);
                _service.update(request);
            } catch (NotFoundException ignore) {
            }
            throw new RuntimeException(e);
        }

        return builder.build();
    }

    @Value
    @Builder
    private static class DcmCatEntry {
        String  id;
        String  classUid;
        String  instanceUid;
        String  uri;
        File    file;
        String  calculatedFileName;
        boolean isValidDicomFile;

        public static DcmCatEntryBuilder builder(final DicomFileNamer dicomFileNamer, final StopTagInputHandler stopTagInputHandler) {
            return new ExtendedDcmCatEntryBuilder(dicomFileNamer, stopTagInputHandler);
        }

        private static class ExtendedDcmCatEntryBuilder extends DcmCatEntryBuilder {
            private final DicomFileNamer      _dicomFileNamer;
            private final StopTagInputHandler _stopTagInputHandler;

            ExtendedDcmCatEntryBuilder(final DicomFileNamer dicomFileNamer, final StopTagInputHandler stopTagInputHandler) {
                _dicomFileNamer      = dicomFileNamer;
                _stopTagInputHandler = stopTagInputHandler;
            }

            @Override
            public DcmCatEntry build() {
                Validate.notBlank(super.id, "ID cannot be null or empty");
                Validate.notBlank(super.instanceUid, "SOP instance UID cannot be null or empty");
                Validate.notBlank(super.uri, "URI cannot be null or empty");
                Validate.notNull(super.file, "File cannot be null or empty");
                Validate.isTrue(super.file.exists() && super.file.isFile(), "File must exist and be a file");

                final DicomObject dicomObject = getDicomObject();

                if (dicomObject != null) {
                    final String instanceUid = dicomObject.getString(Tag.SOPInstanceUID);
                    Validate.isTrue(StringUtils.equals(super.instanceUid, instanceUid), "The specified SOP instance UID (%s) does not equal the extracted SOP instance UID: %s", super.instanceUid, instanceUid);
                    super.classUid(dicomObject.getString(Tag.SOPClassUID));
                    super.calculatedFileName(getCalculatedFileName(dicomObject));

                    final boolean hasFileName = StringUtils.isNotBlank(super.calculatedFileName);
                    log.debug("The DICOM file {} has SOP class UID {} and SOP instance UID {}, calculated file name is {}", super.file, super.classUid, instanceUid, hasFileName ? super.calculatedFileName : "blank (indicates invalid DICOM file)");
                    super.isValidDicomFile(hasFileName);
                } else {
                    super.isValidDicomFile(false);
                }

                return super.build();
            }

            private String getCalculatedFileName(final DicomObject dicomObject) {
                if (dicomObject != null) {
                    return _dicomFileNamer.makeFileName(dicomObject);
                }
                return null;
            }

            private DicomObject getDicomObject() {
                try {
                    return DicomUtils.read(super.file, _stopTagInputHandler);
                } catch (IOException e) {
                    log.error("An error occurred trying to read the DICOM file {}: {}", super.file.getAbsolutePath(), e.getMessage());
                    return null;
                }
            }
        }
    }

    @Getter
    @Accessors(prefix = "_")
    private static class DcmCatEntries extends DefaultHandler {
        private static final String QNAME_ID       = "ID";
        private static final String QNAME_UID      = "UID";
        private static final String QNAME_URI      = "URI";
        private static final String QNAME_XSI_TYPE = "xsi:type";
        public static final  String CAT_ENTRY      = "cat:entry";
        public static final  String CAT_DCM_ENTRY  = "cat:dcmEntry";

        private final DicomFileNamer      _dicomFileNamer;
        private final StopTagInputHandler _stopTagInputHandler;
        private final Path                _rootPath;
        private final List<DcmCatEntry>   _entries;
        private final List<File>          _badFiles;

        public DcmCatEntries(final DicomFileNamer dicomFileNamer, final StopTagInputHandler stopTagInputHandler, final Path resourceUri) {
            _dicomFileNamer      = dicomFileNamer;
            _stopTagInputHandler = stopTagInputHandler;

            // Make the root path the folder containing the resource URI if the URI indicates a file, otherwise use as is
            _rootPath = resourceUri.toFile().isFile() ? resourceUri.getParent() : resourceUri;
            _entries  = new ArrayList<>();
            _badFiles = new ArrayList<>();
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
            // If we have a cat:entry element with xsi:type set to cat:dcmEntry...
            if (StringUtils.equals(CAT_ENTRY, qName) && StringUtils.equals(CAT_DCM_ENTRY, attributes.getValue(QNAME_XSI_TYPE))) {
                final String path = attributes.getValue(QNAME_URI);
                final File   file = _rootPath.resolve(path).toFile();
                try {
                    _entries.add(DcmCatEntry.builder(_dicomFileNamer, _stopTagInputHandler).id(attributes.getValue(QNAME_ID)).uri(path).instanceUid(attributes.getValue(QNAME_UID)).file(file).build());
                } catch (IllegalArgumentException e) {
                    _badFiles.add(file);
                }
            }
        }
    }
}
