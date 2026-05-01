/*
 * PrearcImporter: org.nrg.dcm.Restructurer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Move;
import org.dcm4che2.data.Tag;
import org.nrg.AbstractRestructurer;
import org.nrg.attr.ConversionFailureException;
import org.nrg.attr.Utils;
import org.nrg.dicomtools.filters.SeriesImportFilter;
import org.nrg.dicomtools.utilities.DicomUtils;
import org.nrg.framework.status.LoggerStatusReporter;
import org.nrg.util.FileURIOpener;
import org.nrg.xnat.Files;
import org.nrg.xnat.Labels;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts an arbitrary directory structure of DICOM files
 * into a structure we like.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@Slf4j
public final class Restructurer extends AbstractRestructurer {
    private final static String                   MULTIPLE_FILES         = "(selected files)";
    private final static String                   DEFAULT_SESSION_NAME   = "session";
    private final static Set<DicomAttributeIndex> DEFAULT_KEYS           = Stream.of(Attributes.StudyInstanceUID, Attributes.Modality, Attributes.SeriesNumber).collect(Collectors.toSet());
    private final static Set<DicomAttributeIndex> PATIENT_SELECTION_KEYS = Collections.singleton(Attributes.PatientID);
    private final static Set<DicomAttributeIndex> STUDY_LABEL_KEYS       = Collections.singleton(new FixedDicomAttributeIndex(Tag.StudyID));
    private final static Set<DicomAttributeIndex> SERIES_SELECTION_KEYS  = Stream.of(Attributes.SeriesInstanceUID, Attributes.SeriesDescription, Attributes.TransferSyntaxUID).collect(Collectors.toSet());
    private final static Set<DicomAttributeIndex> ALL_KEYS               = Stream.of(DEFAULT_KEYS, PATIENT_SELECTION_KEYS, STUDY_LABEL_KEYS, SERIES_SELECTION_KEYS).flatMap(Collection::stream).collect(Collectors.toSet());
    private final static Set<Integer>             ALL_KEYS_TAGS          = ALL_KEYS.stream().map(Restructurer::getAttributeTag).collect(Collectors.toSet());

    private static int getAttributeTag(final DicomAttributeIndex attribute) {
        return attribute.getPath(null)[0];
    }

    private static final boolean decompressIsSupported = Decompress.isSupported();

    private final Collection<File>         incoming;
    private final File                     destination;
    private final List<SeriesImportFilter> filters;
    private final Project                  project;
    private final Set<File>                studies = new LinkedHashSet<>();

    @SuppressWarnings("unused")
    public Restructurer(final Collection<File> incoming, final File destination) throws IOException {
        this(incoming, destination, null);
    }

    public Restructurer(final Collection<File> incoming, final File destination, final List<SeriesImportFilter> filters) throws IOException {
        super(log);

        //noinspection ResultOfMethodCallIgnored
        destination.mkdirs();
        if (!destination.isDirectory()) {
            throw new IOException(destination + " is not a directory");
        }

        this.incoming = ImmutableSet.copyOf(incoming);
        this.destination = destination;
        this.filters = ObjectUtils.getIfNull(filters, Collections::emptyList);
        this.project = new Project();
        project.setBaseDir(destination);
    }

    @SuppressWarnings("unused")
    public Restructurer(final File[] incoming, final File destination) throws IOException {
        this(Arrays.asList(incoming), destination, null);
    }

    public Restructurer(final File incoming, final File destination) throws IOException {
        this(Collections.singleton(incoming), destination, null);
    }

    public Restructurer(final File[] incoming, final File destination, final List<SeriesImportFilter> filters) throws IOException {
        this(Arrays.asList(incoming), destination, filters);
    }

    @SuppressWarnings("unused")
    public Restructurer(final File incoming, final File destination, final List<SeriesImportFilter> filters) throws IOException {
        this(Collections.singleton(incoming), destination, filters);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    public void run() {
        if (incoming.isEmpty()) {
            return;
        }

        final Object masterObj;
        if (1 == incoming.size()) {
            masterObj = incoming.iterator().next();
        } else {
            masterObj = MULTIPLE_FILES;
        }
        publishStatus(masterObj, "looking for DICOM files");

        destination.mkdirs();

        final Set<Integer> referencedTags = filters.stream().map(SeriesImportFilter::getFilterTags).flatMap(Collection::stream).collect(Collectors.toSet());
        final Set<Integer> additionalTags = referencedTags.isEmpty() ? Collections.emptySet() : referencedTags.stream().filter(tag -> !ALL_KEYS_TAGS.contains(tag)).collect(Collectors.toSet());
        log.debug("Found {} DICOM tags referenced in total from the series import filters, with {} tags not already contained in the default set of keys.", referencedTags.size(), additionalTags.size());

        final Set<DicomAttributeIndex> referencedAttributes = additionalTags.isEmpty() ? ALL_KEYS : Stream.concat(ALL_KEYS.stream(), additionalTags.stream().map(FixedDicomAttributeIndex::new)).collect(Collectors.toSet());

        // Find all DICOM files in the incoming set and sort them by session
        // (this is almost equivalent to DICOM study, but see comments for SessionDirectoryRecordFactory)
        final DicomMetadataStore dicomMetadataStore;
        try {
            dicomMetadataStore = EnumeratedMetadataStore.createHSQLDBBacked(referencedAttributes, FileURIOpener.getInstance());
            dicomMetadataStore.add(incoming.stream().map(input -> {
                if (input == null) {
                    return null;
                }
                try {
                    return DicomUtils.getQualifiedUri(input.getCanonicalPath());
                } catch (URISyntaxException e) {
                    return input.toURI();
                } catch (IOException e) {
                    log.warn("An error occurred trying to get the URI of the file " + input.getAbsolutePath(), e);
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet()));
            publishStatus(masterObj, "found " + dicomMetadataStore.getSize() + " DICOM files");
        } catch (IOException |
                SQLException e) {
            publishFailure(masterObj, e.getMessage());
            return;
        }

        try {
            final Set<String> studyUIDs;
            try {
                studyUIDs = dicomMetadataStore.getUniqueValues(Attributes.StudyInstanceUID);
            } catch (ConversionFailureException | IOException | SQLException e) {
                publishFailure(masterObj, e.getMessage());
                return;
            }

            boolean warnedAboutDecompression = false;
            STUDIES:
            for (final String studyUID : studyUIDs) {
                final Map<DicomAttributeIndex, String> studySpec = Collections.singletonMap(Attributes.StudyInstanceUID, studyUID);

                final Set<DicomAttributeIndex> keys = new LinkedHashSet<>(PATIENT_SELECTION_KEYS);
                keys.addAll(STUDY_LABEL_KEYS);
                keys.add(Attributes.Modality);

                final SetMultimap<DicomAttributeIndex, String> values;
                try {
                    final Map<DicomAttributeIndex, ConversionFailureException> failures = new HashMap<>();
                    values = dicomMetadataStore.getUniqueValuesGiven(studySpec, keys, failures);
                    if (!failures.isEmpty()) {
                        publishFailure(masterObj, "Skipping study " + studyUID + ": " + failures.values());
                        continue;
                    }
                } catch (IOException e) {
                    publishFailure(masterObj, "An error occurred reading a file or data stream. Skipping study " + studyUID + ": " + e.getMessage());
                    continue;
                } catch (SQLException e) {
                    publishFailure(masterObj, "An error occurred interacting with the metadata store. Skipping study " + studyUID + ": " + e.getMessage());
                    continue;
                }

                final String label = buildLabel(values);
                publishStatus(masterObj, "found DICOM study " + label + " (" + studyUID + ")");

                final File studyDir = Utils.getUnique(destination, label);
                studies.add(studyDir);
                final File scansDir = new File(studyDir, SCANS_DIR);
                scansDir.mkdirs();
                if (!studyDir.isDirectory()) {
                    publishFailure(studyDir, "unable to create image directory " + scansDir.getPath());
                    continue;
                }

                final Set<String> seriesUIDs;
                try {
                    final Map<DicomAttributeIndex, ConversionFailureException> failures   = new HashMap<>();
                    final SetMultimap<DicomAttributeIndex, String>             seriesUIDm = dicomMetadataStore.getUniqueValuesGiven(studySpec, SERIES_SELECTION_KEYS, failures);
                    if (!failures.isEmpty()) {
                        publishFailure(studyDir, "unable to determine series selection keys for study " + label);
                        continue;
                    }

                    seriesUIDs = seriesUIDm.get(Attributes.SeriesInstanceUID);


                    final Set<Map<String, String>> seriesAttributes = dicomMetadataStore.getUniqueCombinationsGivenValues(studySpec, referencedAttributes, failures)
                                                                                        .stream()
                                                                                        .map(DicomUtils.MAP_BY_ATTRIBUTE_TO_STRING_FUNCTION)
                                                                                        .collect(Collectors.toSet());

                    for (final Map<String, String> series : seriesAttributes) {
                        if (filters.stream().anyMatch(filter -> !filter.shouldIncludeDicomObject(series))) {
                            final String seriesInstanceUID = series.get("SeriesInstanceUID");
                            seriesUIDs.remove(seriesInstanceUID);
                            log.info("The series with instance UID {} matched one or more of the series import filters and is being removed from the restructuring.", seriesInstanceUID);
                        }
                    }

                    if (null == seriesUIDs) {
                        publishFailure(studyDir, "unable to determine Series Instance UIDs for study " + label);
                        continue;
                    }
                } catch (IOException e) {
                    publishFailure(masterObj, "An error occurred reading a file or data stream. Skipping study " + studyUID + ": " + e.getMessage());
                    continue;
                } catch (SQLException e) {
                    publishFailure(masterObj, "An error occurred interacting with the metadata store. Skipping study " + studyUID + ": " + e.getMessage());
                    continue;
                }

                boolean needsDecompression = false;
                for (final String seriesUID : seriesUIDs) {
                    final Map<DicomAttributeIndex, String> seriesSpec = new HashMap<>(studySpec);
                    seriesSpec.put(Attributes.SeriesInstanceUID, seriesUID);

                    final Set<URI> files;
                    final File     scanDir;
                    try {
                        final Map<DicomAttributeIndex, ConversionFailureException> failures = new HashMap<>();
                        files = dicomMetadataStore.getResourcesForValues(seriesSpec, failures);
                        if (failures.isEmpty()) {
                            final SetMultimap<DicomAttributeIndex, String> metadata      = dicomMetadataStore.getUniqueValuesGiven(seriesSpec, SERIES_SELECTION_KEYS, failures);
                            final Set<String>                              seriesNumbers = metadata.get(Attributes.SeriesNumber).stream().filter(Objects::nonNull).collect(Collectors.toSet());

                            final String seriesNumber;
                            if (!seriesNumbers.isEmpty()) {
                                if (seriesNumbers.size() > 1) {
                                    log.warn("Multiple series numbers associated with seriesUID {}, using first", seriesUID);
                                }
                                seriesNumber = seriesNumbers.iterator().next();
                            } else {
                                seriesNumber = null;
                            }

                            final String scanId = determineScanSubdir(seriesNumber, seriesUID);
                            scanDir = new File(scansDir, scanId);
                            for (final String tsuid : metadata.get(Attributes.TransferSyntaxUID)) {
                                if (Decompress.needsDecompress(tsuid)) {
                                    needsDecompression = true;
                                    break;
                                }
                            }
                        } else {
                            publishFailure(masterObj, "Skipping series " + seriesUID + " of session " + label + ": " + failures.values());
                            continue STUDIES;
                        }
                    } catch (IOException e) {
                        publishFailure(masterObj, "An error occurred reading a file or data stream. Skipping series " + seriesUID + " of session " + label + ": " + e.getMessage());
                        continue STUDIES;
                    } catch (SQLException e) {
                        publishFailure(masterObj, "An error occurred interacting with the metadata store. Skipping series " + seriesUID + " of session " + label + ": " + e.getMessage());
                        continue STUDIES;
                    }

                    final File scanDicomDataDir = new File(scanDir, "DICOM");

                    for (final URI infileUri : files) {
                        File infile = new File(infileUri);
                        if (needsDecompression) {
                            if (decompressIsSupported) {
                                try {
                                    infile = Decompress.dicomObject2File(Decompress.decompress_image(infile), infile);
                                } catch (Throwable e) {
                                    publishFailure(masterObj, "Decompression error :" + e + ". Storing in original format.");
                                }
                            } else if (!warnedAboutDecompression) {
                                publishWarning(masterObj, "Files in " + label + " need decompression but support is not enabled."
                                                          + " Storing in original format.");
                                warnedAboutDecompression = true;
                            }
                        }

                        // Construct a unique name for the file in the DICOM data directory for this scan.
                        final File outfile  = Utils.getUnique(scanDicomDataDir, infile.getName());
                        final Move moveTask = new Move();
                        moveTask.setProject(project);
                        moveTask.setFile(infile);
                        moveTask.setTofile(outfile);
                        try {
                            moveTask.execute();
                        } catch (BuildException e) {
                            publishWarning(studyDir, e.getMessage());
                        }
                        if (!outfile.exists()) {
                            publishFailure(studyDir, "unable to move " + infile + " to " + outfile);
                        }
                    }
                    try {
                        dicomMetadataStore.remove(files.stream().filter(uri -> !new File(uri).exists()).collect(Collectors.toSet()));
                    } catch (SQLException e) {
                        log.warn("unable to remove some moved files from metadata store", e);
                    }
                }
            }

            // Remove any remaining empty directory trees.
            pruneDirectoryTree(incoming);

            publishSuccess(masterObj, "done finding DICOM");
        } finally {
            try {
                dicomMetadataStore.close();
            } catch (IOException e) {
                log.error("metadata database close failed", e);
            }
        }

    }

    @Nonnull
    public static String determineScanSubdir(@Nullable String seriesNum, @Nonnull String seriesUID) {
        String scanId;
        if (Files.isValidFilename(seriesNum)) {
            scanId = seriesNum;
        } else {
            scanId = Labels.toLabelChars(seriesUID);
        }
        return scanId == null ? "NA" : scanId;
    }

    private String buildLabel(final SetMultimap<DicomAttributeIndex, String> attributes) {
        for (final DicomAttributeIndex key : Restructurer.PATIENT_SELECTION_KEYS) {
            if (attributes.containsKey(key)) {
                final Set<String>      values = attributes.get(key);
                final Optional<String> found  = values.stream().filter(StringUtils::isNotBlank).findFirst();
                if (found.isPresent()) {
                    return found.get().replaceAll("[\\W]", "_");
                }
            }
        }
        return Restructurer.DEFAULT_SESSION_NAME;
    }


    /*
     * (non-Javadoc)
     * @see org.nrg.Restructurer#getSessions()
     */
    public Collection<File> getSessions() {
        return studies;
    }


    /**
     * Restructure the specified data.
     *
     * @param args First argument is the pathname of the destination directory. Successive arguments are path names to be restructured.
     *
     * @throws IOException When an error occurs reading or writing data.
     */
    public static void main(final String[] args) throws IOException {
        final File destination = new File(args[0]);
        for (final String arg : args) {
            final Restructurer restructurer = new Restructurer(new File(arg), destination);
            restructurer.addStatusListener(new LoggerStatusReporter(Restructurer.class));
            restructurer.run();
            System.out.println("Sessions found in " + arg + ": " + restructurer.studies);
        }
    }
}
