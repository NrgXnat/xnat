/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.DICOMScanBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.attr.ConversionFailureException;
import org.nrg.attr.ExtAttrDef;
import org.nrg.attr.ExtAttrValue;
import org.nrg.dcm.AttrAdapter;
import org.nrg.dcm.AttrDefs;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.DicomMetadataStore;
import org.nrg.dcm.EnumeratedMetadataStore;
import org.nrg.dcm.SOPModel;
import org.nrg.dcm.xnat.exceptions.UnableToBuildScanException;
import org.nrg.dcm.xnat.utils.DicomMappingUtils;
import org.nrg.io.RelativePathWriter;
import org.nrg.io.RelativePathWriterFactory;
import org.nrg.io.ScanCatalogFileWriterFactory;
import org.nrg.session.BeanBuilder;
import org.nrg.session.SessionBuilder;
import org.nrg.ulog.FileMicroLogFactory;
import org.nrg.ulog.MicroLog;
import org.nrg.util.FileURIOpener;
import org.nrg.xdat.bean.CatDcmcatalogBean;
import org.nrg.xdat.bean.XnatAnnscandataBean;
import org.nrg.xdat.bean.XnatCfmscandataBean;
import org.nrg.xdat.bean.XnatCrscandataBean;
import org.nrg.xdat.bean.XnatCtscandataBean;
import org.nrg.xdat.bean.XnatDmsscandataBean;
import org.nrg.xdat.bean.XnatDx3dcraniofacialscandataBean;
import org.nrg.xdat.bean.XnatDxscandataBean;
import org.nrg.xdat.bean.XnatEcgscandataBean;
import org.nrg.xdat.bean.XnatEegscandataBean;
import org.nrg.xdat.bean.XnatEmgscandataBean;
import org.nrg.xdat.bean.XnatEogscandataBean;
import org.nrg.xdat.bean.XnatEpsscandataBean;
import org.nrg.xdat.bean.XnatEsscandataBean;
import org.nrg.xdat.bean.XnatEsvscandataBean;
import org.nrg.xdat.bean.XnatGmscandataBean;
import org.nrg.xdat.bean.XnatGmvscandataBean;
import org.nrg.xdat.bean.XnatHdscandataBean;
import org.nrg.xdat.bean.XnatImagescandataBean;
import org.nrg.xdat.bean.XnatIoscandataBean;
import org.nrg.xdat.bean.XnatMgscandataBean;
import org.nrg.xdat.bean.XnatMrscandataBean;
import org.nrg.xdat.bean.XnatMtlscandataBean;
import org.nrg.xdat.bean.XnatNmscandataBean;
import org.nrg.xdat.bean.XnatObjscandataBean;
import org.nrg.xdat.bean.XnatOpscandataBean;
import org.nrg.xdat.bean.XnatOptscandataBean;
import org.nrg.xdat.bean.XnatOtherdicomscandataBean;
import org.nrg.xdat.bean.XnatPascandataBean;
import org.nrg.xdat.bean.XnatPetscandataBean;
import org.nrg.xdat.bean.XnatPosscandataBean;
import org.nrg.xdat.bean.XnatResourcecatalogBean;
import org.nrg.xdat.bean.XnatRespscandataBean;
import org.nrg.xdat.bean.XnatRfscandataBean;
import org.nrg.xdat.bean.XnatRtimagescandataBean;
import org.nrg.xdat.bean.XnatScscandataBean;
import org.nrg.xdat.bean.XnatSegscandataBean;
import org.nrg.xdat.bean.XnatSmscandataBean;
import org.nrg.xdat.bean.XnatSrscandataBean;
import org.nrg.xdat.bean.XnatUsscandataBean;
import org.nrg.xdat.bean.XnatXa3dscandataBean;
import org.nrg.xdat.bean.XnatXascandataBean;
import org.nrg.xdat.bean.XnatXcscandataBean;
import org.nrg.xdat.bean.XnatXcvscandataBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.nrg.dcm.NamedAttributes.Modality;
import static org.nrg.dcm.NamedAttributes.SOPClassUID;
import static org.nrg.dcm.NamedAttributes.SeriesInstanceUID;
import static org.nrg.dcm.NamedAttributes.SeriesNumber;
import static org.nrg.dcm.xnat.CatalogBuilder.RESOURCE_LABEL_DICOM;

/**
 * Builds an XNAT imageScanData object from a DICOM series
 */
@Slf4j
public class DICOMScanBuilder implements Callable<XnatImagescandataBean> {
    private final        List<XnatImagescandataBeanFactory>                                    scanBeanFactories     = new ArrayList<>();
    private final        Map<Class<? extends XnatImagescandataBean>, Map<String, BeanBuilder>> scanBeanBuilders;
    private final        Map<Class<? extends XnatImagescandataBean>, AttrDefs>                 scanTypeAttrs;
    private static final List<DicomAttributeIndex>                                             ATTRIBUTES            = Arrays.asList(SeriesInstanceUID, SeriesNumber, SOPClassUID, Modality);
    private final static Map<String, BeanBuilder>                                              imageScanBeanBuilders = new HashMap<>();

    private final static ImmutableMap<Class<? extends XnatImagescandataBean>, Map<String, BeanBuilder>> defaultScanBeanBuilders =
            new ImmutableMap.Builder<Class<? extends XnatImagescandataBean>, Map<String, BeanBuilder>>()
                    .put(XnatCrscandataBean.class, imageScanBeanBuilders)
                    .put(XnatCtscandataBean.class, new ImmutableMap.Builder<String, BeanBuilder>()
                            .putAll(imageScanBeanBuilders)
                            .putAll(CTScanAttributes.getBeanBuilders())
                            .build())
                    .put(XnatDxscandataBean.class, imageScanBeanBuilders)
                    .put(XnatDx3dcraniofacialscandataBean.class, imageScanBeanBuilders)
                    .put(XnatEcgscandataBean.class, imageScanBeanBuilders)
                    .put(XnatEpsscandataBean.class, imageScanBeanBuilders)
                    .put(XnatEsscandataBean.class, imageScanBeanBuilders)
                    .put(XnatEsvscandataBean.class, imageScanBeanBuilders)
                    .put(XnatGmscandataBean.class, imageScanBeanBuilders)
                    .put(XnatGmvscandataBean.class, imageScanBeanBuilders)
                    .put(XnatHdscandataBean.class, imageScanBeanBuilders)
                    .put(XnatIoscandataBean.class, imageScanBeanBuilders)
                    .put(XnatMgscandataBean.class, imageScanBeanBuilders)
                    .put(XnatMrscandataBean.class, new ImmutableMap.Builder<String, BeanBuilder>()
                            .putAll(imageScanBeanBuilders)
                            .putAll(MRScanAttributes.getBeanBuilders())
                            .build())
                    .put(XnatNmscandataBean.class, imageScanBeanBuilders)
                    .put(XnatOpscandataBean.class, imageScanBeanBuilders)
                    .put(XnatOptscandataBean.class, imageScanBeanBuilders)
                    .put(XnatPetscandataBean.class, imageScanBeanBuilders)
                    .put(XnatRfscandataBean.class, imageScanBeanBuilders)
                    .put(XnatRtimagescandataBean.class, imageScanBeanBuilders)
                    .put(XnatScscandataBean.class, imageScanBeanBuilders)
                    .put(XnatSegscandataBean.class, imageScanBeanBuilders)
                    .put(XnatSrscandataBean.class, imageScanBeanBuilders)
                    .put(XnatSmscandataBean.class, imageScanBeanBuilders)
                    .put(XnatUsscandataBean.class, imageScanBeanBuilders)
                    .put(XnatXascandataBean.class,  new ImmutableMap.Builder<String, BeanBuilder>()
                            .putAll(imageScanBeanBuilders)
                            .putAll(XAScanAttributes.getBeanBuilders())
                            .build())
                    .put(XnatXcscandataBean.class, imageScanBeanBuilders)
                    .put(XnatXcvscandataBean.class, imageScanBeanBuilders)
                    .put(XnatDmsscandataBean.class, imageScanBeanBuilders)
                    .put(XnatCfmscandataBean.class, imageScanBeanBuilders)
                    .put(XnatPascandataBean.class, imageScanBeanBuilders)
                    .put(XnatEmgscandataBean.class, imageScanBeanBuilders)
                    .put(XnatEogscandataBean.class, imageScanBeanBuilders)
                    .put(XnatRespscandataBean.class, imageScanBeanBuilders)
                    .put(XnatPosscandataBean.class, imageScanBeanBuilders)
                    .put(XnatObjscandataBean.class, imageScanBeanBuilders)
                    .put(XnatMtlscandataBean.class, imageScanBeanBuilders)
                    .put(XnatAnnscandataBean.class, imageScanBeanBuilders)
                    .put(XnatOtherdicomscandataBean.class, imageScanBeanBuilders)
                    .build();

    private final static ImmutableMap<Class<? extends XnatImagescandataBean>, AttrDefs> defaultScanTypeAttrs =
            new ImmutableMap.Builder<Class<? extends XnatImagescandataBean>, AttrDefs>()
                    .put(XnatCrscandataBean.class, ImageScanAttributes.get())
                    .put(XnatCtscandataBean.class, CTScanAttributes.get())
                    .put(XnatDx3dcraniofacialscandataBean.class, ImageScanAttributes.get())
                    .put(XnatDxscandataBean.class, ImageScanAttributes.get())
                    .put(XnatEcgscandataBean.class, ECGScanAttributes.get())
                    .put(XnatEegscandataBean.class, EEGScanAttributes.get())
                    .put(XnatEpsscandataBean.class, ImageScanAttributes.get())
                    .put(XnatEsscandataBean.class, ESScanAttributes.get())
                    .put(XnatEsvscandataBean.class, ImageScanAttributes.get())
                    .put(XnatGmscandataBean.class, ImageScanAttributes.get())
                    .put(XnatGmvscandataBean.class, ImageScanAttributes.get())
                    .put(XnatHdscandataBean.class, ImageScanAttributes.get())
                    .put(XnatIoscandataBean.class, ImageScanAttributes.get())
                    .put(XnatMgscandataBean.class, ImageScanAttributes.get())
                    .put(XnatMrscandataBean.class, MRScanAttributes.get())
                    .put(XnatNmscandataBean.class, ImageScanAttributes.get())
                    .put(XnatOpscandataBean.class, ImageScanAttributes.get())
                    .put(XnatOptscandataBean.class, OPTScanAttributes.get())
                    .put(XnatPetscandataBean.class, PETScanAttributes.get())
                    .put(XnatRfscandataBean.class, ImageScanAttributes.get())
                    .put(XnatRtimagescandataBean.class, ImageScanAttributes.get())
                    .put(XnatScscandataBean.class, ImageScanAttributes.get())
                    .put(XnatSegscandataBean.class, ImageScanAttributes.get())
                    .put(XnatSrscandataBean.class, ImageScanAttributes.get())
                    .put(XnatSmscandataBean.class, ImageScanAttributes.get())
                    .put(XnatUsscandataBean.class, ImageScanAttributes.get())
                    .put(XnatXascandataBean.class, XAScanAttributes.get())
                    .put(XnatXcscandataBean.class, XCScanAttributes.get())
                    .put(XnatXcvscandataBean.class, ImageScanAttributes.get())
                    .put(XnatDmsscandataBean.class, DMSScanAttributes.get())
                    .put(XnatCfmscandataBean.class, CFMScanAttributes.get())
                    .put(XnatPascandataBean.class, PAScanAttributes.get())
                    .put(XnatEmgscandataBean.class, EMGScanAttributes.get())
                    .put(XnatEogscandataBean.class, EOGScanAttributes.get())
                    .put(XnatRespscandataBean.class, RESPScanAttributes.get())
                    .put(XnatPosscandataBean.class, POSScanAttributes.get())
                    .put(XnatObjscandataBean.class, OBJScanAttributes.get())
                    .put(XnatMtlscandataBean.class, MTLScanAttributes.get())
                    .put(XnatAnnscandataBean.class, ANNScanAttributes.get())
                    .put(XnatOtherdicomscandataBean.class, ImageScanAttributes.get())
                    .put(XnatXa3dscandataBean.class, ImageScanAttributes.get())
                    .build();

    private static final XnatImagescandataBeanFactory DEFAULT_SCAN_BEAN_FACTORY = new DefaultXnatImagescandataBeanFactory();

    static ImmutableMap<Class<? extends XnatImagescandataBean>, Map<String, BeanBuilder>> getDefaultScanBeanBuilders() {
        return defaultScanBeanBuilders;
    }

    static ImmutableMap<Class<? extends XnatImagescandataBean>, AttrDefs> getDefaultScanTypeAttrs() {
        return defaultScanTypeAttrs;
    }

    static ImmutableSet<DicomAttributeIndex> getNativeTypeAttrs(Map<Class<? extends XnatImagescandataBean>, AttrDefs> allScanTypeAttrs) {
        return ImmutableSet.copyOf(allScanTypeAttrs.values().stream().map(AttrDefs::getNativeAttrs).flatMap(Collection::stream).collect(Collectors.toSet()));
    }

    private final DicomMetadataStore                store;
    private final MicroLog                          uLog;
    private final String                            id;
    private final Series                            series;
    private final Iterable<XnatResourcecatalogBean> resources;
    private final Callable<Integer>                 nFrames;

    /**
     * NOTE: nFrames will always be evaluated AFTER an Iterator on catalogResource
     * has been fully consumed. Therefore, it's safe (and expected) to determine
     * the number of frames in the same code that builds the catalogs.
     *
     * @param store            DicomMetadataStore that holds metadata for the scan
     * @param sessionLog       MicroLog destination for warning and error reports
     * @param scanID           XNAT scan label
     * @param series           DICOM series record
     * @param catalogResources XnatResourcecatalogBeans representing the scan catalog(s)
     * @param nFrames          Callable that evaluates to the number of frames in the scan
     */
    public DICOMScanBuilder(final DicomMetadataStore store,
                            final MicroLog sessionLog,
                            final String scanID,
                            final Series series,
                            final Iterable<XnatResourcecatalogBean> catalogResources,
                            final Callable<Integer> nFrames,
                            final List<XnatImagescandataBeanFactory> scanBeanFactories,
                            final Map<Class<? extends XnatImagescandataBean>, Map<String, BeanBuilder>> scanBeanBuilders,
                            final Map<Class<? extends XnatImagescandataBean>, AttrDefs> scanTypeAttrs) {
        this.store = store;
        this.uLog = sessionLog;
        this.id = StringUtils.startsWith(scanID, "+") ?  "_" + scanID.substring(1): scanID;
        this.series = series;
        this.resources = catalogResources;
        this.nFrames = nFrames;
        if (scanBeanFactories != null) {
            this.scanBeanFactories.addAll(scanBeanFactories);
        }
        this.scanBeanBuilders = scanBeanBuilders != null ? scanBeanBuilders : defaultScanBeanBuilders;
        this.scanTypeAttrs = scanTypeAttrs != null ? scanTypeAttrs : defaultScanTypeAttrs;
    }

    @Nullable
    private static XnatImagescandataBean createScanBean(List<XnatImagescandataBeanFactory> scanBeanFactories,
                                                        final DicomMetadataStore store,
                                                        final Series series) {
        for (final XnatImagescandataBeanFactory factory : scanBeanFactories) {
            final XnatImagescandataBean bean = factory.create(series, store);
            if (null != bean) {
                return bean;
            }
        }
        // Nothing worked; try to explain what happened.
        log.warn("Scan builder not implemented for SOP class(es) {} or modality(ies) {}, trying default scan builder factory", series.getSOPClasses(), series.getModalities());
        return DEFAULT_SCAN_BEAN_FACTORY.create(series, store);
    }

    public XnatImagescandataBean call() throws IOException, UnableToBuildScanException {
        final List<XnatImagescandataBeanFactory> scanBeanFactories = makeScanBeanFactories();
        final XnatImagescandataBean              scan              = createScanBean(scanBeanFactories, store, series);
        if (scan == null) {
            throw new UnableToBuildScanException("No suitable scan bean factories");
        }

        final AttrDefs scanAttrDefs = scanTypeAttrs.containsKey(scan.getClass())
                ? scanTypeAttrs.get(scan.getClass()) : ImageScanAttributes.get();

        final AttrAdapter scanAttrs = new AttrAdapter(store, scanAttrDefs);
        final Map<DicomAttributeIndex, String> scanSpec = Collections.singletonMap(SeriesInstanceUID, series.getUID());

        final Map<ExtAttrDef<DicomAttributeIndex>, Throwable> scanFailures = new HashMap<>();
        final List<ExtAttrValue>                              scanValues   = SessionBuilder.getValues(scanAttrs, scanSpec, scanFailures);
        for (final Map.Entry<ExtAttrDef<DicomAttributeIndex>, Throwable> me : scanFailures.entrySet()) {
            DICOMSessionBuilder.report("scan " + id, me.getKey(), me.getValue(), uLog);
        }

        final Map<String, BeanBuilder> scanBeanBuilder = scanBeanBuilders.containsKey(scan.getClass())
                ? scanBeanBuilders.get(scan.getClass()) : ImmutableMap.copyOf(imageScanBeanBuilders);

        for (final ExtAttrValue val : SessionBuilder.setValues(scan, scanValues, scanBeanBuilder, "ID", "parameters/addParam")) {
            switch (val.getName()) {
                case "ID":
                    scan.setId(id);
                    break;
                case "parameters/addParam":
                    final String paramName = val.getAttrs().get("name");
                    try {
                        // This is nasty.  Is there a better way?
                        DicomMappingUtils.findAndInvokeAddParamMethod(scan, paramName, val.getText());
                    } catch (Exception e) {
                        uLog.log(id, e);
                        log.error("{} does not include a parameters/addParam field, unable to set {} for scan {}", scan.getClass(), paramName, id, e);
                    }
                    break;
                default:
                    log.error("scan field {} unexpectedly skipped", val.getName());
            }
        }

        for (final XnatResourcecatalogBean resource : resources) {
            scan.addFile(resource);
        }
        try {
            scan.setFrames(nFrames.call());
        } catch (Throwable t) {
            uLog.log("unable to determine frame count for scan " + id, t);
        }

        return scan;
    }

    private static File getScanDir(final DicomMetadataStore store, final Map<DicomAttributeIndex, String> scanSpec) throws IOException, SQLException {
        final AttrAdapter fileAttrs = new AttrAdapter(store, scanSpec);
        fileAttrs.add(ImageFileAttributes.get());
        final Multimap<URI, ExtAttrValue> unsorted = fileAttrs.getValuesForResources();
        if (unsorted.isEmpty()) {
            throw new FileNotFoundException("scan contains no image files");
        }
        return DICOMSessionBuilder.getCommonRoot(unsorted.keySet().stream().map(File::new).collect(Collectors.toList()));
    }

    private static class ResourceCatalogCollector implements Callable<Iterable<XnatResourcecatalogBean>>, Iterable<XnatResourcecatalogBean> {
        private final DicomMetadataStore               store;
        private final MicroLog                         uLog;
        private final Map<DicomAttributeIndex, String> constraints;
        private final String                           scanID;
        private final RelativePathWriterFactory        catalogWriterFactory;
        private final       boolean       useRelativeCatalogPath;
        private       final AtomicInteger nFrames = new AtomicInteger(-1);

        ResourceCatalogCollector(final DicomMetadataStore store,
                                 final MicroLog uLog,
                                 final Map<DicomAttributeIndex, String> constraints,
                                 final String id,
                                 final RelativePathWriterFactory catalogWriterFactory,
                                 final boolean useRelativeCatalogPath) {
            this.store = store;
            this.uLog = uLog;
            this.constraints = constraints;
            this.scanID = id;
            this.catalogWriterFactory = catalogWriterFactory;
            this.useRelativeCatalogPath = useRelativeCatalogPath;
        }

        public Callable<Integer> getFrameCountCallable() {
            return () -> {
                if (nFrames.get() == -1) {
                    throw new IllegalStateException("frame count not ready");
                }
                return nFrames.get();
            };
        }

        public Iterable<XnatResourcecatalogBean> call() {
            try {
                final File scanDir = getScanDir(store, constraints);
                if (null == scanDir) {
                    uLog.log("No common root available for files in scan " + scanID);
                }
                final CatalogBuilder builder = new CatalogBuilder(scanID, uLog, store, scanDir, constraints);
                final Collection<XnatResourcecatalogBean> resources = new ArrayList<>();

                for (final Map.Entry<File, AbstractMap.SimpleEntry<XnatResourcecatalogBean, CatDcmcatalogBean>> me : builder.call().entrySet()) {
                    final File catalogPath = me.getKey();
                    final AbstractMap.SimpleEntry<XnatResourcecatalogBean, CatDcmcatalogBean> e = me.getValue();
                    final XnatResourcecatalogBean resource = e.getKey();
                    final CatDcmcatalogBean       catalog  = e.getValue();
                    final String label = RESOURCE_LABEL_DICOM.equals(resource.getLabel()) ? scanID : scanID + "_" + resource.getLabel();
                    try (final RelativePathWriter writer = catalogWriterFactory.getWriter(catalogPath, label)) {
                        catalog.toXML(writer, true);
                        resource.setUri(useRelativeCatalogPath ? writer.getRelativePath() : writer.getFullPath());
                        resources.add(resource);
                    }
                }
                nFrames.set(builder.getFrameCount());
                if (scanDir != null && !org.nrg.xft.utils.FileUtils.HasFiles(scanDir)) {
                    // We may have moved all the files into scanId or secondary dirs, so if the original dir is empty, remove it
                    FileUtils.deleteDirectory(scanDir);
                    File parent = scanDir.getParentFile();
                    if (!org.nrg.xft.utils.FileUtils.HasFiles(parent)) {
                        FileUtils.deleteDirectory(parent);
                    }

                }
                return resources;
            } catch (Throwable t) {
                final Logger logger = LoggerFactory.getLogger(DICOMScanBuilder.class);
                logger.error("Unable to build catalog for scan {}", scanID, t);
                try {
                    uLog.log("Unable to build catalog for scan " + scanID, t);
                } catch (IOException e) {
                    logger.error("Unable to write to session log", e);
                }
                return Collections.emptyList();
            }
        }

        public @Nonnull
        Iterator<XnatResourcecatalogBean> iterator() {
            return call().iterator();
        }
    }

    private static Series buildSeries(final DicomMetadataStore store) throws IOException, SQLException {
        final Map<DicomAttributeIndex, ConversionFailureException> failures = new HashMap<>();
        final Set<Map<DicomAttributeIndex, String>>                ids      = store.getUniqueCombinations(ATTRIBUTES, failures);
        if (ids.isEmpty()) {
            throw new RuntimeException("no files found");    // TODO: better exception
        }

        final Multimap<DicomAttributeIndex, String> indexMaps = HashMultimap.create();
        ids.stream().map(Multimaps::forMap).forEach(indexMaps::putAll);

        final String type              = SOPModel.getScanType(indexMaps.get(SOPClassUID));
        String       seriesInstanceUID = null, seriesNumber = null;
        for (final Map<DicomAttributeIndex, String> m : ids) {
            if (m.containsKey(SeriesInstanceUID)) {
                seriesInstanceUID = m.get(SeriesInstanceUID);
            }
            if (m.containsKey(SeriesNumber)) {
                seriesNumber = m.get(SeriesNumber);
            }
            if (null == type || type.equals(SOPModel.getScanType(m.get(SOPClassUID)))) {
                break;
            }
        }

        final Series series = new Series(seriesNumber, seriesInstanceUID);
        for (final String modality : indexMaps.get(Modality)) {
            series.addModality(modality);
        }
        for (final String sopClass : indexMaps.get(SOPClassUID)) {
            series.addSOPClass(sopClass);
        }

        return series;
    }

    public static DICOMScanBuilder fromStore(final DicomMetadataStore store,
                                             final MicroLog sessionLog,
                                             final String scanID,
                                             final Series series,
                                             final Map<DicomAttributeIndex, String> constraints,
                                             final RelativePathWriterFactory catalogWriterFactory,
                                             final boolean useRelativePath,
                                             final List<XnatImagescandataBeanFactory> scanBeanFactoryClasses,
                                             final Map<Class<? extends XnatImagescandataBean>, Map<String, BeanBuilder>> scanBeanBuilders,
                                             final Map<Class<? extends XnatImagescandataBean>, AttrDefs> scanTypeAttrs) {
        final ResourceCatalogCollector collector = new ResourceCatalogCollector(store, sessionLog, constraints, scanID,
                                                                                catalogWriterFactory, useRelativePath);
        return new DICOMScanBuilder(store, sessionLog, scanID, series, collector,
                                    collector.getFrameCountCallable(), scanBeanFactoryClasses, scanBeanBuilders, scanTypeAttrs);
    }

    public static DICOMScanBuilder fromScanDirectory(final DicomMetadataStore store,
                                                     final MicroLog sessionLog,
                                                     final String scanID,
                                                     final RelativePathWriterFactory catalogWriterFactory,
                                                     final boolean useRelativePath,
                                                     final List<XnatImagescandataBeanFactory> scanBeanFactoryClasses,
                                                     final Map<Class<? extends XnatImagescandataBean>, Map<String, BeanBuilder>> scanBeanBuilders,
                                                     final Map<Class<? extends XnatImagescandataBean>, AttrDefs> scanTypeAttrs)
            throws IOException, SQLException {
        final Map<DicomAttributeIndex, String> constraints = Collections.emptyMap();
        final ResourceCatalogCollector collector = new ResourceCatalogCollector(store, sessionLog, constraints, scanID,
                                                                                catalogWriterFactory, useRelativePath);

        final Series series = buildSeries(store);

        return new DICOMScanBuilder(store, sessionLog, scanID, series, collector,
                                    collector.getFrameCountCallable(), scanBeanFactoryClasses, scanBeanBuilders, scanTypeAttrs);
    }

    public static XnatImagescandataBean buildScanFromDirectory(final File sessionDir,
                                                               final File scanDir,
                                                               final String scanID,
                                                               final boolean useRelativePath)
            throws IOException, SQLException, UnableToBuildScanException {
        final FileMicroLogFactory       logFactory           = new FileMicroLogFactory(sessionDir);
        final MicroLog                  log                  = logFactory.getLog("scan-" + scanDir.getName());
        final RelativePathWriterFactory catalogWriterFactory = new ScanCatalogFileWriterFactory(sessionDir);

        final Set<DicomAttributeIndex> attrs = new LinkedHashSet<>(getNativeTypeAttrs(defaultScanTypeAttrs));
        attrs.addAll(ImageFileAttributes.get().getNativeAttrs());
        attrs.addAll(CatalogAttributes.get().getNativeAttrs());
        attrs.addAll(Arrays.asList(SeriesInstanceUID, SeriesNumber, SOPClassUID, Modality));
        try (final DicomMetadataStore store = EnumeratedMetadataStore.createHSQLDBBacked(attrs, FileURIOpener.getInstance())) {
            store.add(Collections.singletonList(scanDir.toURI()));
            return fromScanDirectory(store, log, scanID, catalogWriterFactory, useRelativePath, null, defaultScanBeanBuilders, defaultScanTypeAttrs).call();
        }
    }

    /**
     * Build the default scan bean factories and add them to any spring-injected ones
     *
     * @return session bean factory instances
     */
    private List<XnatImagescandataBeanFactory> makeScanBeanFactories() {
        final List<XnatImagescandataBeanFactory> factories = new ArrayList<>(scanBeanFactories);
        // instantiate default factory and add to list
        try {
            factories.add(DefaultXnatImagescandataBeanFactory.class.getConstructor().newInstance());
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            // Every possible exception here is programmer error; don't use checked exceptions.
            throw new RuntimeException(e);
        }
        for (XnatImagescandataBeanFactory factory : factories) {
            factory.setLogger(log);
        }
        return factories;
    }
}
