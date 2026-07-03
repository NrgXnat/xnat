/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.CatalogBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.xnat;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.nrg.attr.ExtAttrDef;
import org.nrg.attr.ExtAttrValue;
import org.nrg.dcm.AttrAdapter;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.DicomMetadataStore;
import org.nrg.dcm.SOPModel;
import org.nrg.session.SessionBuilder;
import org.nrg.ulog.MicroLog;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.CatDcmcatalogBean;
import org.nrg.xdat.bean.CatDcmentryBean;
import org.nrg.xdat.bean.XnatResourcecatalogBean;
import org.nrg.xdat.preferences.SiteConfigPreferences;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.Callable;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@Slf4j
public class CatalogBuilder implements Callable<Map<File, AbstractMap.SimpleEntry<XnatResourcecatalogBean, CatDcmcatalogBean>>> {
    public static final String RESOURCE_LABEL_DICOM       = "DICOM";
    public static final String RESOURCE_LABEL_SECONDARY   = "secondary";
    public static final String RESOURCE_FORMAT            = "DICOM";
    public static final String RESOURCE_CONTENT           = "RAW";
    public static final String RESOURCE_CONTENT_SECONDARY = "secondary";
    public static final String SCANS_DIR                  = "SCANS";

    private static final Map<?, String> EMPTY_CONSTRAINTS                     = new HashMap<>();
    private static final String         XNAT_GENERATED_SCAN_PARENT_PATH_REGEX = Paths.get(".*", SCANS_DIR, "[^" + File.separator + "]+").toString();
    private static final String         XNAT_GENERATED_SCAN_PATH_REGEX        = Paths.get(XNAT_GENERATED_SCAN_PARENT_PATH_REGEX, RESOURCE_LABEL_DICOM).toString();
    private static final Object         MUTEX                                 = new Object();

    private final Map<Object, String> constraints = new LinkedHashMap<>();

    private final String              id;
    private final MicroLog            microLog;
    private final DicomMetadataStore  store;
    private final File                root;
    private final boolean             shouldLoadFiles;
    private final boolean             separateSecondaryDicomOnArchive;

    private Integer nFrames = null;

    public CatalogBuilder(final String id, final MicroLog microLog,
                          final DicomMetadataStore store, final File root,
                          final boolean shouldLoadFiles) {
        this(id, microLog, store, root, EMPTY_CONSTRAINTS, shouldLoadFiles);
    }

    public CatalogBuilder(final String id, final MicroLog log,
                          final DicomMetadataStore store, final File root,
                          final Map<?, String> constraints) {
        this(id, log, store, root, constraints, false);
    }

    public CatalogBuilder(final String id, final MicroLog microLog,
                          final DicomMetadataStore store, final File root,
                          final Map<?, String> constraints,
                          final boolean shouldLoadFiles) {
        this.id       = id;
        this.microLog = microLog;
        this.store    = store;
        this.root     = root;
        if (null != constraints) {
            this.constraints.putAll(constraints);
        }
        this.shouldLoadFiles = shouldLoadFiles;
        // The null check below documents that this class is expected to work without an initialized application
        // context (e.g. unit tests); make the lookup itself equally tolerant instead of relying on whichever test
        // happened to populate XDAT's static caches first.
        SiteConfigPreferences preferences;
        try {
            preferences = XDAT.getSiteConfigPreferences();
        } catch (RuntimeException e) {
            preferences = null;
        }
        this.separateSecondaryDicomOnArchive = preferences == null || preferences.getSeparateSecondaryDicomOnArchive();
    }

    public int getFrameCount() {
        return Optional.ofNullable(nFrames).orElseThrow(() -> new IllegalStateException("frame count not ready"));
    }

    public Map<File, AbstractMap.SimpleEntry<XnatResourcecatalogBean, CatDcmcatalogBean>> call() throws IOException, SQLException {
        if (shouldLoadFiles) {
            final Map<String, String> addCols = Collections.singletonMap(SOPModel.XNAT_SCAN_COLUMN, id);
            store.add(Collections.singleton(root.toURI()), addCols);
            constraints.putAll(addCols);
        }

        final ListMultimap<URI, ExtAttrValue> fileValues    = getSortedFileValues(store, constraints);
        final List<ExtAttrValue>              catalogValues = getCatalogValues(constraints);

        // Build the catalog beans and resource records.
        final Map<File, AbstractMap.SimpleEntry<XnatResourcecatalogBean, CatDcmcatalogBean>> catalogFileMap = new HashMap<>();
        final CatDcmcatalogBean                                                              catalog        = new CatDcmcatalogBean();
        // Add an entry for each file to the catalog, and count total number of frames (z dimension)
        final Map<URI, String> secondaryFiles = new HashMap<>();
        nFrames = 0;
        File primaryCatalogLocation   = null;
        File secondaryCatalogLocation = null;
        synchronized (MUTEX) {
            for (final URI uri : fileValues.keySet()) {
                final File file = new File(uri);
                final List<ExtAttrValue> values = fileValues.get(uri);
                final ExtAttrValue sopAttr = values.removeFirst();
                assert ImageFileAttributes.SOPClassUID.equals(sopAttr.getName()) : "expected SOP Class UID, found " + sopAttr;
                final String fileSOPClass = sopAttr.getText();
                if (!separateSecondaryDicomOnArchive || SOPModel.isPrimaryImagingSOP(fileSOPClass)) {
                    ImmutablePair<Path, String> primaryLocation = fixLocation(file, primaryCatalogLocation, true);
                    primaryCatalogLocation = primaryLocation.getKey().toFile();
                    int                   frames = 1;        // one frame per file unless otherwise specified
                    final CatDcmentryBean entry  = new CatDcmentryBean();
                    for (final ExtAttrValue val : SessionBuilder.setValues(entry, values, ImageFileAttributes.SOPClassUID, "URI", "numFrames")) {
                        if (ImageFileAttributes.SOPClassUID.equals(val.getName())) {
                            // ignore; we're done with this attribute
                            log.debug("Done processing {}", val.getName());
                        } else if ("URI".equals(val.getName())) {
                            String relativePath = primaryLocation.getValue();
                            if (relativePath != null) {
                                entry.setUri(relativePath);
                            }
                        } else if ("numFrames".equals(val.getName())) {
                            try {
                                frames = Integer.parseInt(val.getText());
                            } catch (NumberFormatException e) {
                                log.warn("invalid frame count value {} in {}", val.getText(), uri);
                            }
                        } else {
                            log.warn("unexpected file attribute {}", val);
                        }
                    }
                    catalog.addEntries_entry(entry);
                    nFrames += frames;
                } else {
                    ImmutablePair<Path, String> secondaryLocation = fixLocation(file, secondaryCatalogLocation, false);
                    secondaryCatalogLocation = secondaryLocation.getKey().toFile();
                    secondaryFiles.put(uri, secondaryLocation.getValue());
                }
            }
        }
        if (nFrames > 0) {
            // There are some primary imaging data in this scan, so fill out
            // the primary catalog and build a resource for it.
            for (final ExtAttrValue val : SessionBuilder.setValues(catalog, catalogValues, "dimensions")) {
                if (!"dimensions".equals(val.getName())) {
                    log.warn("unexpected attribute {}", val);
                }
                // "dimensions" is set in both the series and the catalog.
                // It's attributes-only and contains only "x" and "y";
                // "z" must be computed from individual DICOM objects.
                catalog.setDimensions_x(val.getAttrs().get("x"));
                catalog.setDimensions_y(val.getAttrs().get("y"));
            }
            catalog.setDimensions_z(nFrames);

            final XnatResourcecatalogBean resource = new XnatResourcecatalogBean();
            resource.setLabel(RESOURCE_LABEL_DICOM);
            resource.setFormat(RESOURCE_FORMAT);
            resource.setContent(RESOURCE_CONTENT);

            catalogFileMap.put(primaryCatalogLocation, new AbstractMap.SimpleEntry<>(resource, catalog));
        }

        // We might have some secondary files (i.e., those with different SOP class)
        // in the series as well
        if (!secondaryFiles.isEmpty()) {
            final CatDcmcatalogBean secondary = new CatDcmcatalogBean();
            for (final URI uri : secondaryFiles.keySet()) {
                final CatDcmentryBean entry = new CatDcmentryBean();
                for (final ExtAttrValue val : SessionBuilder.setValues(entry, fileValues.get(uri), "URI", "instanceNumber", "numFrames")) {
                    if ("URI".equals(val.getName())) {
                        String relativePath = secondaryFiles.get(uri);
                        if (relativePath != null) {
                            entry.setUri(relativePath);
                        }
                    }
                    // ignore instanceNumber, numFrames, as they don't really apply to secondary files
                }
                secondary.addEntries_entry(entry);
            }
            final XnatResourcecatalogBean resource = new XnatResourcecatalogBean();
            resource.setLabel(RESOURCE_LABEL_SECONDARY);
            resource.setFormat(RESOURCE_FORMAT);
            resource.setContent(RESOURCE_CONTENT_SECONDARY);
            catalogFileMap.put(secondaryCatalogLocation, new AbstractMap.SimpleEntry<>(resource, secondary));
        }

        return catalogFileMap;
    }

    private List<ExtAttrValue> getCatalogValues(final Map<?, String> constraints) throws IOException {
        final AttrAdapter catalogAttrs = new AttrAdapter(store, constraints);
        catalogAttrs.add(CatalogAttributes.get());
        final Map<ExtAttrDef<DicomAttributeIndex>, Throwable> catalogFailures = new HashMap<>();
        final List<ExtAttrValue>                              catalogValues   = SessionBuilder.getValues(catalogAttrs, catalogFailures);
        for (final Map.Entry<ExtAttrDef<DicomAttributeIndex>, Throwable> me : catalogFailures.entrySet()) {
            DICOMSessionBuilder.report(id, me.getKey(), me.getValue(), microLog);
        }
        return catalogValues;
    }

    private ImmutablePair<Path, String> fixLocation(File file, @Nullable File catalogLocation, boolean isPrimaryImagingSOP) {
        if (catalogLocation != null) {
            // We've already determined the location, just move files
            return moveFile(catalogLocation.toPath(), file);
        }

        if (root.getAbsolutePath().matches(XNAT_GENERATED_SCAN_PARENT_PATH_REGEX)) {
            // If root is the parent of DICOM and/or secondary subdirectories, return whichever corresponds to file
            Path relative = root.toPath().relativize(file.toPath());
            for (String label : Arrays.asList(RESOURCE_LABEL_DICOM, RESOURCE_LABEL_SECONDARY)) {
                if (relative.startsWith(label)) {
                    return getDestinationAndRelativePath(root.toPath().resolve(label), file);
                }
            }
        }

        if (!root.getAbsolutePath().matches(XNAT_GENERATED_SCAN_PATH_REGEX)) {
            // This isn't an XNAT-generated DICOM path, let's not modify it
            return getDestinationAndRelativePath(root.toPath(), file);
        }

        return moveFile(getPath(isPrimaryImagingSOP), file);
    }

    private Path getPath(boolean isPrimaryImagingSOP) {
        Path location = root.toPath();

        // Set subdir to scanID
        int  count      = location.getNameCount();
        int  scanDirIdx = count - 2;
        Path scanDir    = location.getName(scanDirIdx);
        if (!scanDir.toString().equals(id)) {
            location = location.getRoot().resolve(location.subpath(0, scanDirIdx))
                               .resolve(id).resolve(location.subpath(scanDirIdx + 1, count));
        }

        // Set resource dir to secondary
        if (!isPrimaryImagingSOP && !location.endsWith(RESOURCE_LABEL_SECONDARY)) {
            location = location.getParent().resolve(RESOURCE_LABEL_SECONDARY);
        }
        return location;
    }

    private ImmutablePair<Path, String> moveFile(Path destination, File file) {
        // If the destination hasn't changed, use root
        if (destination.toAbsolutePath().equals(root.toPath())) {
            return getDestinationAndRelativePath(root.toPath(), file);
        }
        if (file.toPath().startsWith(destination)) {
            return getDestinationAndRelativePath(destination, file);
        }

        // Move files if we've changed the location
        try {
            if (Files.notExists(destination)) {
                Files.createDirectories(destination);
            }
            Files.move(file.toPath(), destination.resolve(file.getName()));
            return ImmutablePair.of(destination, file.getName());
        } catch (IOException e) {
            log.error("Unable to move {} to {}", file, destination, e);
            return getDestinationAndRelativePath(root.toPath(), file);
        }
    }

    private ImmutablePair<Path, String> getDestinationAndRelativePath(Path destination, File file) {
        try {
            return ImmutablePair.of(destination, destination.relativize(file.toPath()).toString());
        } catch (IllegalArgumentException e) {
            log.error("session directory {} not root for image file {}", destination, file, e);
            return ImmutablePair.of(destination, null);
        }
    }

    private static ListMultimap<URI, ExtAttrValue> getSortedFileValues(final DicomMetadataStore store, final Map<?, String> constraints)
            throws IOException, SQLException {
        final AttrAdapter fileAttrs = new AttrAdapter(store, constraints);
        fileAttrs.add(ImageFileAttributes.get());
        final ListMultimap<URI, ExtAttrValue> unsorted = fileAttrs.getValuesForResources();
        if (unsorted.isEmpty()) {
            throw new FileNotFoundException("scan contains no image files");
        }
        final ListMultimap<URI, ExtAttrValue> sorted = Multimaps.newListMultimap(new TreeMap<>(new ImageURIComparator(unsorted)), ArrayList::new);
        sorted.putAll(unsorted);
        return ArrayListMultimap.create(sorted);
    }
}
