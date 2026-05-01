/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.DICOMSessionBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.DicomObject;
import org.nrg.attr.*;
import org.nrg.dcm.AttrAdapter;
import org.nrg.dcm.AttrDefs;
import org.nrg.dcm.MutableAttrDefs;
import org.nrg.dcm.*;
import org.nrg.dcm.xnat.AbstractConditionalAttrDef.ContainsAssignmentRule;
import org.nrg.dcm.xnat.AbstractConditionalAttrDef.EqualsRule;
import org.nrg.dcm.xnat.services.DicomMappingService;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.services.ContextService;
import org.nrg.io.RelativePathWriterFactory;
import org.nrg.io.ScanCatalogFileWriterFactory;
import org.nrg.session.BeanBuilder;
import org.nrg.session.SessionBuilder;
import org.nrg.sl4fj.PrintWriterLogger;
import org.nrg.ulog.MicroLog;
import org.nrg.util.FileURIOpener;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.*;
import org.nrg.xdat.bean.base.BaseElement;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.nrg.framework.utilities.Reflection;

import static org.nrg.dcm.Attributes.*;
import static org.nrg.xdat.preferences.HandlePetMr.DEFAULT_EXCLUDED_FIELDS;

/**
 * Generates an XNAT session metadata XML from a DICOM study.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@Slf4j
public class DICOMSessionBuilder extends SessionBuilder implements Closeable {
    // Helpers for building sub-beans
    private static final ImmutableMap<String, ? extends BeanBuilder> imageSessionBeanBuilders = ImmutableMap.of("fields/field", value -> {
        final String text = value.getText();
        if (StringUtils.isBlank(text) || StringUtils.equals("null", text)) {
            return Collections.emptyList();
        }
        final XnatExperimentdataFieldBean field = new XnatExperimentdataFieldBean();
        field.setField(text);
        field.setName(value.getAttrs().get("name"));
        return Collections.singletonList(field);
    });

    private static final String                                                 XML_SUFFIX                              = ".xml";
    private static final String                                                 UNKNOWN_MODALITY                        = "OT";  // DICOM: Other
    private static final Map<String, BeanBuilder>                               imageScanBeanBuilders                   = new HashMap<>();
    private static final Comparator<DicomAttributeIndex>                        COMPARATOR                              = new DicomAttributeIndex.Comparator();
    private static final List<Class<? extends XnatImagesessiondataBeanFactory>> DEFAULT_SESSIONDATABEAN_FACTORY_CLASSES = Arrays.asList(XnatPetmrImagesessiondataBeanFactory.class, SOPMapXnatImagesessiondataBeanFactory.class, ModalityMapXnatImagesessiondataBeanFactory.class);
    private static final XnatAttrDef.NamedAttributePredicate                    PROJECT_ATTRIBUTE_PREDICATE             = new XnatAttrDef.NamedAttributePredicate("project");
    private static final XnatAttrDef.Constant                                   EMPTY_PROJECT_ATTRIBUTE                 = new XnatAttrDef.Empty("project");
    private static final XnatImagesessiondataBeanFactory                        DEFAULT_SESSION_BEAN_FACTORY            = new DefaultXnatImagesessiondataBeanFactory();

    private static ContextService contextService;

    private static SortedSet<DicomAttributeIndex> newAttributeIndexSortedSet() {
        return new TreeSet<>(null == COMPARATOR ? new DicomAttributeIndex.Comparator() : COMPARATOR);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<XnatImagesessiondataBeanFactory>                                    sessionBeanFactories      = new ArrayList<>();
    private final List<Class<? extends XnatImagesessiondataBeanFactory>>                   sessionBeanFactoryClasses = new ArrayList<>();
    private final List<XnatImagescandataBeanFactory>                                       scanBeanFactories         = new ArrayList<>();
    private final Map<Class<? extends XnatImagesessiondataBean>, AttrDefs>                 sessionTypeAttrs          = new HashMap<>();
    private final Map<Class<? extends XnatImagesessiondataBean>, Map<String, BeanBuilder>> sessionBeanBuilders       = new HashMap<>();
    private final Map<Class<? extends XnatImagescandataBean>, Map<String, BeanBuilder>>    scanBeanBuilders          = new HashMap<>();
    private final Map<Class<? extends XnatImagescandataBean>, AttrDefs>                    scanTypeAttrs             = new HashMap<>();
    private final List<String>                                                             excludedFields            = new ArrayList<>();
    private final MutableAttrDefs                                                          sessionAttrDefs           = new MutableAttrDefs();
    private final AtomicInteger                                                            nScans                    = new AtomicInteger();

    private final SortedSet<DicomAttributeIndex> ALL_TAGS;
    private final RelativePathWriterFactory      catalogWriterFactory;
    private final DicomMetadataStore             store;
    private final File                           fileSetDir;
    private final String                         studyInstanceUID;
    private final String                         project;

    //TODO Remove this once we are no longer injecting the old implementation
    final static String scanExtensionsPackage = "org.nrg.dcm.xnat.scanBuilder.extensions";
    final static String sessionExtensionsPackage = "org.nrg.dcm.xnat.sessionBuilder.extensions";


    private ContextService getContextService() {
        if (contextService == null) {
            try {
                contextService = XDAT.getContextService();
            } catch (Exception e) {
                log.error("Unable get context service, spring injection will be skipped", e);
            }
        }
        return contextService;
    }

    private SortedSet<DicomAttributeIndex> setBeanFactoriesBuildersAttrsAndTags() {
        scanTypeAttrs.putAll(DICOMScanBuilder.getDefaultScanTypeAttrs());
        scanBeanBuilders.putAll(DICOMScanBuilder.getDefaultScanBeanBuilders());

        final ContextService contextService = getContextService();

        // Scan
        if (contextService != null) {
            try {
                final Map<String, XnatImagescandataBeanFactory> scanFactoryMap = contextService.getBeansOfType(XnatImagescandataBeanFactory.class);
                // We expect that scanDataFactories are going to be exclusive - we don't control the priority/order
                if (scanFactoryMap != null) {
                    scanBeanFactories.addAll(scanFactoryMap.values());
                }
            } catch (Exception e) {
                log.error("Unable to retrieve injected XnatImagescandataBeanFactory beans", e);
            }

            try {
                final Map<String, DICOMImageScanAttributes> scanAttrs = contextService.getBeansOfType(DICOMImageScanAttributes.class);
                if (scanAttrs != null) {
                    for (final DICOMImageScanAttributes attr : scanAttrs.values()) {
                        final Class<? extends XnatImagescandataBean> clazz = attr.getImageScanDataBeanClass();
                        scanTypeAttrs.put(clazz, attr.get());
                        scanBeanBuilders.put(clazz, ImmutableMap.copyOf(imageScanBeanBuilders));
                    }
                }
            } catch (Exception e) {
                log.error("Unable to retrieve additional DICOMImageScanAttributes beans", e);
            }

            //Session
            try {
                // We expect that sessionBeanFactories are going to be exclusive - we don't control the priority/order
                final Map<String, XnatImagesessiondataBeanFactory> sessionFactoryMap = contextService.getBeansOfType(XnatImagesessiondataBeanFactory.class);
                if (sessionFactoryMap != null) {
                    sessionBeanFactories.addAll(sessionFactoryMap.values());
                }
            } catch (Exception e) {
                log.error("Unable to retrieve injected XnatImagesessiondataBeanFactory beans", e);
            }
        }

        sessionTypeAttrs.put(XnatMrsessiondataBean.class, MRSessionAttributes.get());
        sessionBeanBuilders.put(XnatMrsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatPetsessiondataBean.class, PETSessionAttributes.get());
        sessionBeanBuilders.put(XnatPetsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatPetmrsessiondataBean.class, PETMRSessionAttributes.get());
        sessionBeanBuilders.put(XnatPetmrsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatCtsessiondataBean.class, CTSessionAttributes.get());
        sessionBeanBuilders.put(XnatCtsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatXasessiondataBean.class, XASessionAttributes.get());
        sessionBeanBuilders.put(XnatXasessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatXa3dsessiondataBean.class, ImageSessionAttributes.get());
        sessionBeanBuilders.put(XnatXa3dsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatUssessiondataBean.class, USSessionAttributes.get());
        sessionBeanBuilders.put(XnatUssessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatRtsessiondataBean.class, RTSessionAttributes.get());
        sessionBeanBuilders.put(XnatRtsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatCrsessiondataBean.class, CRSessionAttributes.get());
        sessionBeanBuilders.put(XnatCrsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatOptsessiondataBean.class, OPTSessionAttributes.get());
        sessionBeanBuilders.put(XnatOptsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatMgsessiondataBean.class, MGSessionAttributes.get());
        sessionBeanBuilders.put(XnatMgsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatDxsessiondataBean.class, DXSessionAttributes.get());
        sessionBeanBuilders.put(XnatDxsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatNmsessiondataBean.class, NMSessionAttributes.get());
        sessionBeanBuilders.put(XnatNmsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatSrsessiondataBean.class, SRSessionAttributes.get());
        sessionBeanBuilders.put(XnatSrsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatOtherdicomsessiondataBean.class, OtherDICOMSessionAttributes.get());
        sessionBeanBuilders.put(XnatOtherdicomsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatRfsessiondataBean.class, RFSessionAttributes.get());
        sessionBeanBuilders.put(XnatRfsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatSmsessiondataBean.class, SMSessionAttributes.get());
        sessionBeanBuilders.put(XnatSmsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatGmsessiondataBean.class, GMSessionAttributes.get());
        sessionBeanBuilders.put(XnatGmsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatEssessiondataBean.class, ESSessionAttributes.get());
        sessionBeanBuilders.put(XnatEssessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatXcsessiondataBean.class, XCSessionAttributes.get());
        sessionBeanBuilders.put(XnatXcsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatDmssessiondataBean.class, DMSSessionAttributes.get());
        sessionBeanBuilders.put(XnatDmssessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatCfmsessiondataBean.class, CFMSessionAttributes.get());
        sessionBeanBuilders.put(XnatCfmsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatPasessiondataBean.class, PASessionAttributes.get());
        sessionBeanBuilders.put(XnatPasessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatEmgsessiondataBean.class, EMGSessionAttributes.get());
        sessionBeanBuilders.put(XnatEmgsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatEogsessiondataBean.class, EOGSessionAttributes.get());
        sessionBeanBuilders.put(XnatEogsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatRespsessiondataBean.class, RESPSessionAttributes.get());
        sessionBeanBuilders.put(XnatRespsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatPossessiondataBean.class, POSSessionAttributes.get());
        sessionBeanBuilders.put(XnatPossessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatObjsessiondataBean.class, OBJSessionAttributes.get());
        sessionBeanBuilders.put(XnatObjsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatEcgsessiondataBean.class, ECGSessionAttributes.get());
        sessionBeanBuilders.put(XnatEcgsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        sessionTypeAttrs.put(XnatEegsessiondataBean.class, EEGSessionAttributes.get());
        sessionBeanBuilders.put(XnatEegsessiondataBean.class, ImmutableMap.copyOf(imageSessionBeanBuilders));

        try {
            Map<String, DICOMImageSessionAttributes> attrs;
            if (contextService != null && (attrs = contextService.getBeansOfType(DICOMImageSessionAttributes.class)) != null) {
                for (DICOMImageSessionAttributes attr : attrs.values()) {
                    Class<? extends XnatImagesessiondataBean> clazz = attr.getImageSessionDataBeanClass();
                    sessionTypeAttrs.put(clazz, attr.get());
                    sessionBeanBuilders.put(clazz, ImmutableMap.copyOf(imageSessionBeanBuilders));
                }
            }
        } catch (Exception e) {
            log.error("Unable to retrieve additional DICOMImageSessionAttributes beans", e);
        }

        // Add any custom mappings
        try {
            DicomMappingService mappingService;
            if (contextService != null && (mappingService = contextService.getBean(DicomMappingService.class)) != null) {
                addCustomMappings(mappingService, scanTypeAttrs);
                addCustomMappings(mappingService, sessionTypeAttrs);
            }
        } catch (Exception e) {
            log.error("Unable to retrieve DicomMappingService", e);
        }


        final SortedSet<DicomAttributeIndex> tags = newAttributeIndexSortedSet();
        tags.add(SOPClassUID);
        tags.add(SeriesNumber);
        tags.add(StudyInstanceUID);
        tags.add(SeriesInstanceUID);
        tags.add(SeriesDescription);
        tags.add(Modality);
        tags.addAll(CatalogAttributes.get().getNativeAttrs());
        tags.addAll(ImageFileAttributes.get().getNativeAttrs());
        for (final AttrDefs attrDefs : sessionTypeAttrs.values()) {
            tags.addAll(attrDefs.getNativeAttrs());
        }
        tags.addAll(DICOMScanBuilder.getNativeTypeAttrs(scanTypeAttrs));
        return Collections.unmodifiableSortedSet(tags);
    }

    /**
     * Add spring-injected custom mappings for data types
     * @param mappingService the spring mapping service
     * @param typeAttrs the type -> attribute map
     * @param <T> the bean class
     */
    private <T extends Class<? extends BaseElement>> void addCustomMappings(final DicomMappingService mappingService, final Map<T, AttrDefs> typeAttrs) {
        for (T bean : typeAttrs.keySet()) {
            String schemaElement;
            try {
                schemaElement = bean.getConstructor().newInstance().getFullSchemaElementName();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                    NoSuchMethodException | NullPointerException e) {
                log.error("Unable to determine schemaElement for {}", bean, e);
                continue;
            }
            try {
                MutableAttrDefs addlAttrs = mappingService.getAddlAttributesForMapping(project, schemaElement);
                addlAttrs.add(typeAttrs.get(bean)); // add the previous attributes
                typeAttrs.put(bean, addlAttrs);
            } catch (NotFoundException e) {
                // Cannot determine schema element name or no additional attributes set
            }
        }
    }

    /**
     * Creates a session builder that sends output to the given Writer.
     * @param fileSetDir Root directory of the DICOM fileset
     * @param writer Where the XML session document gets written;
     *   if null, the document is written to a file with the same name as the root directory,
     *   plus the .xml suffix
     * @param sessionAttrDefs additional definitions for session-level attributes
     * @param scanAttrDefs additional definitions for scan-level attributes
     * @throws IOException When an error occurs reading or write data.
     */
    public DICOMSessionBuilder(final File fileSetDir, final Writer writer,
                               final Collection<XnatAttrDef> sessionAttrDefs,
                               final Collection<XnatAttrDef> scanAttrDefs,
                               @Nullable final Function<DicomObject, DicomObject> function,
                               @Nullable final DicomMetadataStore store)
            throws IOException, NoUniqueSessionException, SQLException {
        super(fileSetDir, fileSetDir.getPath(), writer, new File(fileSetDir.getPath() + XML_SUFFIX));

        // Parse out project
        this.project = Iterables.tryFind(Iterables.transform(sessionAttrDefs, XnatAttrDef.ATTR_TO_CONSTANT_FN), PROJECT_ATTRIBUTE_PREDICATE).or(EMPTY_PROJECT_ATTRIBUTE).getValue();
        this.ALL_TAGS = setBeanFactoriesBuildersAttrsAndTags();
        this.store = store == null ? getStore(fileSetDir, getTags(sessionAttrDefs, scanAttrDefs), function) : store;
        this.studyInstanceUID = getStudyInstanceUID(this.store);

        this.fileSetDir = fileSetDir.getCanonicalFile();
        this.sessionAttrDefs.addAll(sessionAttrDefs);
        final MutableAttrDefs scanAttrDefs1 = new MutableAttrDefs();
        scanAttrDefs1.addAll(scanAttrDefs);
        this.catalogWriterFactory = new ScanCatalogFileWriterFactory(fileSetDir);
    }

    @SuppressWarnings("unused")
    public DICOMSessionBuilder(final DicomMetadataStore store,
                               final File fileSetDir, final Writer writer,
                               final Collection<XnatAttrDef> sessionAttrDefs,
                               final Collection<XnatAttrDef> scanAttrDefs)
                    throws IOException, NoUniqueSessionException, SQLException {
        this(fileSetDir, writer, sessionAttrDefs, scanAttrDefs, null, store);
    }

    public DICOMSessionBuilder(final File fileSetDir, final Writer writer,
            final Collection<XnatAttrDef> sessionAttrDefs,
            final Collection<XnatAttrDef> scanAttrDefs)
                    throws IOException, NoUniqueSessionException, SQLException {
        this(fileSetDir, writer, sessionAttrDefs, scanAttrDefs, null, null);
    }

    @SuppressWarnings("unused")
    public DICOMSessionBuilder(final File fileSetDir, final XnatAttrDef[]sessionAttrs, final Function<DicomObject,DicomObject> function) throws IOException, NoUniqueSessionException, SQLException {
        this(fileSetDir, null, Arrays.asList(sessionAttrs), Collections.emptyList(), function, null);
    }

    /**
     * Creates a session builder that sends output to the given Writer.
     * @param fileSetDir Root directory of the DICOM fileset
     * @param writer Where the XML session document gets written
     * @param sessionAttrs additional definitions for session-level attributes
     * @throws IOException When an error occurs reading or write data.
     */
    public DICOMSessionBuilder(final File fileSetDir, final Writer writer, final XnatAttrDef...sessionAttrs)
            throws IOException,NoUniqueSessionException,SQLException {
        this(fileSetDir, writer, Arrays.asList(sessionAttrs), Collections.emptyList());
    }

    /**
     * Creates a session builder that writes output to a file with the
     * same name as the file set root, plus a '.xml' suffix.
     * @param fileSetDir Root directory of the DICOM fileset
     * @param sessionAttrs additional definitions for session-level attributes
     * @throws IOException When an error occurs reading or write data.
     */
    public DICOMSessionBuilder(final File fileSetDir, final XnatAttrDef...sessionAttrs)
            throws IOException,NoUniqueSessionException,SQLException {
        this(fileSetDir, null, sessionAttrs);
    }

    private static String getStudyInstanceUID(final DicomMetadataStore store)
            throws IOException,NoUniqueSessionException,SQLException {
        try {
            final Set<String> uids = store.getUniqueValues(StudyInstanceUID);
            if (uids.isEmpty()) {
                throw new NoUniqueSessionException();
            }
            final Iterator<String> i = uids.iterator();
            final String uid = i.next();
            if (i.hasNext()) {
                throw new NoUniqueSessionException(uids.toArray(new String[0]));
            }
            return uid;
        } catch (ConversionFailureException e) {
            throw new RuntimeException(e);    // UIDs should require no conversion
        }
    }

    private DicomMetadataStore createStore(final Collection<DicomAttributeIndex> extraTags) throws IOException,SQLException {
        final SortedSet<DicomAttributeIndex> tags = newAttributeIndexSortedSet();
        tags.addAll(ALL_TAGS);
        tags.addAll(extraTags);
        return EnumeratedMetadataStore.createHSQLDBBacked(tags, FileURIOpener.getInstance());
    }

    private DicomMetadataStore getStore(final File root, final Collection<DicomAttributeIndex> extraTags,
                                        @Nullable Function<DicomObject, DicomObject> function) throws IOException, SQLException {
        DicomMetadataStore s = createStore(extraTags);
        if (function == null) {
            s.add(Collections.singleton(root.toURI()));
        } else {
            s.add(Collections.singleton(root.toURI()), function);
        }
        return s;
    }

    @SafeVarargs
    private static SortedSet<DicomAttributeIndex> getTags(final Collection<XnatAttrDef>...attrDefs) {
        final SortedSet<DicomAttributeIndex> tags = newAttributeIndexSortedSet();
        for (final Collection<XnatAttrDef> defs : attrDefs) {
            for (final XnatAttrDef def : defs) {
                tags.addAll(def.getAttrs());
            }
        }
        return tags;
    }

    /**
     * Creates a session bean object for the named study
     * @param factories           The list of factories available for creating image session beans.
     * @param studyInstanceUID    The study instance UID of the imaging session.
     * @return An image session bean as created by the appropriate factory.
     * @throws SQLException When an error occurs interacting with the database.
     * @throws IOException When an error occurs reading or writing data.
     */
    private XnatImagesessiondataBean makeSessionBean(final Iterable<XnatImagesessiondataBeanFactory> factories, final String studyInstanceUID) throws IOException, SQLException {
        // Try complex and custom rules first
        for (final XnatImagesessiondataBeanFactory factory : factories) {
            final XnatImagesessiondataBean bean = factory.create(store, studyInstanceUID, getParameters());
            if (null != bean) {
                return bean;
            }
        }

        // Nothing worked; try to explain what happened.
        final Map<DicomAttributeIndex,ConversionFailureException> failures = new LinkedHashMap<>();
        final SetMultimap<DicomAttributeIndex,String>             values   = store.getUniqueValuesGiven(ImmutableMap.of(StudyInstanceUID, studyInstanceUID), Arrays.asList(SOPClassUID, Modality), failures);
        for (final Map.Entry<DicomAttributeIndex,ConversionFailureException> fme : failures.entrySet()) {
            log.warn("Unable to convert attribute {}", fme.getKey(), fme.getValue());
        }

        final Set<String> sopClassUids = values.get(SOPClassUID);
        log.warn("Session builder not implemented for SOP class(es) {} or modality(ies) {}, trying default session builder factory", sopClassUids, values.get(Modality));
        return DEFAULT_SESSION_BEAN_FACTORY.create(store, studyInstanceUID);
    }

    /**
     * Append to the list of session bean factory classes.
     * @param classes session bean factories to append
     * @return this
     */
    @SuppressWarnings("unused")
    public DICOMSessionBuilder setSessionBeanFactoryClasses(Iterable<Class<? extends XnatImagesessiondataBeanFactory>> classes) {
        sessionBeanFactoryClasses.clear();
        Iterables.addAll(sessionBeanFactoryClasses, classes);
        return this;
    }
    
    /**
     * Append instances of session bean factory classes to any that have been injected
     *
     * @param log log to which each factory class will report
     * @return session bean factory instances
     */
    private List<XnatImagesessiondataBeanFactory> makeSessionBeanFactories(final Logger log) {
        List<XnatImagesessiondataBeanFactory> factories = new ArrayList<>(sessionBeanFactories);
        // instantiate other factories
        factories.addAll((sessionBeanFactoryClasses.isEmpty() ? DEFAULT_SESSIONDATABEAN_FACTORY_CLASSES : sessionBeanFactoryClasses).stream().map(clazz -> {
            try {
                final XnatImagesessiondataBeanFactory factory = clazz.getConstructor().newInstance();
                factory.setLogger(log);
                return factory;
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                // Every possible exception here is programmer error; don't use checked exceptions.
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList()));
        return factories;
    }

    public List<String> getExcludedFields() {
        if (excludedFields.isEmpty()) {
            excludedFields.addAll(DEFAULT_EXCLUDED_FIELDS);
        }
        return excludedFields;
    }

    @SuppressWarnings("unused")
    public void setExcludedFields(final List<String> fields) {
        excludedFields.clear();
        excludedFields.addAll(fields);
    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    public XnatImagesessiondataBean call() throws IOException,NoUniqueSessionException,SQLException {
        log.info("Building session for {}", studyInstanceUID);
        final PrintWriterLogger sessionLog = new PrintWriterLogger(new File(fileSetDir, "dcmtoxnat.log"));
        final List<XnatImagesessiondataBeanFactory> sessionBeanFactories = makeSessionBeanFactories(sessionLog);
        try {
            final XnatImagesessiondataBean session = makeSessionBean(sessionBeanFactories, studyInstanceUID);
            if (null == session) {
                throw new NoUniqueSessionException("no session type identified");
            }
            final AttrAdapter sessionAttrs = new AttrAdapter(store, Collections.singletonMap(StudyInstanceUID, studyInstanceUID));
            sessionAttrs.add(sessionAttrDefs);

            final AttrDefs sessionAttrDefs = sessionTypeAttrs.containsKey(session.getClass())
                    ? sessionTypeAttrs.get(session.getClass()) : ImageSessionAttributes.get();

            sessionAttrs.add(sessionAttrDefs);

            final Map<ExtAttrDef<DicomAttributeIndex>,Throwable> failures = new HashMap<>();
            final List<ExtAttrValue> sessionValues = getValues(sessionAttrs, failures);
            for (final Map.Entry<ExtAttrDef<DicomAttributeIndex>,Throwable> me: failures.entrySet()) {
                if ("UID".equals(me.getKey().getName())) {
                    final Throwable cause = me.getValue();
                    if (cause instanceof NoUniqueValueException) {
                        throw new NoUniqueSessionException(((NoUniqueValueException)cause).getValues());
                    } else {
                        throw new RuntimeException("Unable to derive UID for unexpected cause", cause);
                    }
                } else {
                    report("session", me.getKey(), me.getValue(), sessionLog);
                }
            }

            final List<String> excludedFields = getExcludedFields();

            final Map<String, BeanBuilder> sessionBeanBuilder = sessionBeanBuilders.containsKey(session.getClass())
                    ? sessionBeanBuilders.get(session.getClass()) : ImmutableMap.copyOf(imageSessionBeanBuilders);

            for (final ExtAttrValue val : setValues(session, sessionValues, sessionBeanBuilder, excludedFields.toArray(new String[0]))) {
                final String name = val.getName();
                // prearchivePath is a special case; we set this by hand. SOURCE is just ignored.
                if ("prearchivePath".equals(name)) {
                    setPrearchivePath(session);
                } else if (!excludedFields.contains(name)){
                    throw new RuntimeException("Attribute " + val.getName() + " unexpectedly skipped");
                }
            }

            // Identify the scans -- one scan per value of Series Instance UID
            final SortedMap<String,Map<String,Series>> seriesToUID = new TreeMap<>(new Utils.MaybeNumericStringComparator());
            final Map<DicomAttributeIndex,ConversionFailureException> failed = new HashMap<>();
            final Set<Map<DicomAttributeIndex,String>> seriesIds = store.getUniqueCombinations(Arrays.asList(SeriesNumber, SeriesInstanceUID, SOPClassUID, Modality), failed);
            if (!failed.isEmpty()) {
                sessionLog.log("Unable to retrieve some series-identifying attributes: " + failed);
            }

            for (final Map<DicomAttributeIndex,String> entry : seriesIds) {
                final String seriesInstanceUID = entry.get(SeriesInstanceUID);

                String tempSeriesNumber = entry.get(SeriesNumber);

                if (StringUtils.startsWith(tempSeriesNumber, "+" )) {
                    tempSeriesNumber = "_" + tempSeriesNumber.substring(1);
                }

                final String seriesNumber = StringUtils.defaultIfBlank(
                        tempSeriesNumber,
                        seriesInstanceUID.replaceAll("[^\\w]", "_")
                );

                Map<String,Series> seriesMap = seriesToUID.computeIfAbsent(seriesNumber, k -> new TreeMap<>());
                final Series series = seriesMap.computeIfAbsent(seriesInstanceUID, k -> new Series(seriesNumber, seriesInstanceUID));

                series.addModality(entry.get(Modality));
                series.addSOPClass(entry.get(SOPClassUID));
            }

            final Map<String,Series> scanToSeries = new LinkedHashMap<>();
            for (final Map.Entry<String, Map<String, Series>> numberUidSeriesEntry : seriesToUID.entrySet()) {  // Number->UID->Series entry
                final String seriesNumber = numberUidSeriesEntry.getKey();
                final Map<String,Series> uids = numberUidSeriesEntry.getValue();
                assert !uids.isEmpty();
                if (uids.size() > 1) {  // each study ID looks like "{SeriesNum}-{Modality}{index}"
                    final Map<String,Integer> modalityCounts = new HashMap<>();
                    for (final Series series : uids.values()) {
                        String modality = SOPModel.getLeadModality(series.getModalities());
                        if (null == modality) {
                            modality = UNKNOWN_MODALITY;
                        }
                        final int index = modalityCounts.containsKey(modality) ? modalityCounts.get(modality) + 1 : 1;
                        modalityCounts.put(modality, index);
                        final String scanID = seriesNumber + "-" + modality + index;
                        scanToSeries.put(scanID, series);
                    }
                } else {
                    // Just one Series Instance UID for this Series Number; use Series Number as Scan ID
                    scanToSeries.put(seriesNumber, uids.values().iterator().next());
                }
            }

            nScans.set(scanToSeries.size());

            for (final Map.Entry<String,Series> nse : scanToSeries.entrySet()) {  // Number->Series entry
                try {
                    final Map<DicomAttributeIndex,String> scanSpec = ImmutableMap.of(SeriesInstanceUID,
                            nse.getValue().getUID());
                    final DICOMScanBuilder scanBuilder = DICOMScanBuilder.fromStore(store, sessionLog,
                            nse.getKey(), nse.getValue(), scanSpec, catalogWriterFactory, useRelativePaths(),
                            scanBeanFactories, scanBeanBuilders, scanTypeAttrs);

                    XnatImagescandataBean scan = scanBuilder.call();

                    //allow injection of other behavior
                    Map<String, Object> params = new HashMap<>();
                    params.put("series",nse.getValue());
                    params.put("scan", scan);
                    params.put("scanSpec", scanSpec);
                    params.put("scanBuilder", scanBuilder);
                    params.put("store", store);
                    Reflection.injectDynamicImplementations(scanExtensionsPackage, params);

                    session.addScans_scan(scan);
                } catch (Throwable t) {
                    log.info("Unable to process scan " + nse.getKey(), t);
                    sessionLog.log("Unable to process scan " + nse.getKey(), t);
                }
            }

            //allow injection of other behavior
            Map<String, Object> params = new HashMap<>();
            params.put("studyInstanceUID",studyInstanceUID);
            params.put("session", session);
            params.put("sessionAttrs", sessionAttrs);
            params.put("store", store);
            Reflection.injectDynamicImplementations(sessionExtensionsPackage, params);

            return session;
        } finally {
            if (sessionLog.hasMessages()) {
                log.info("Warnings occurred in processing " + fileSetDir.getPath() + "; see " + sessionLog);
                sessionLog.close();
            }
        }
    }

    public final String getSessionInfo() {
        return (nScans.get() == 0 ? "(undetermined)" : nScans.get()) + " scans";
    }

    public final void close() throws IOException {
        store.close();
    }

    private static StringBuilder startReport(final Object context, final ExtAttrDef<?> subject) {
        return new StringBuilder(context.toString()).append(" attribute ").append(subject.getName());
    }

    static void report(final Object context, final ExtAttrDef<?> subject, final Throwable t, final MicroLog microLog) {
        try {
            if (t instanceof ConversionFailureException) {
                report(context, subject, (ConversionFailureException)t, microLog);
            } else if (t instanceof NoUniqueValueException) {
                report(context, subject, (NoUniqueValueException)t, microLog);
            } else {
                final StringBuilder sb = startReport(context, subject);
                sb.append(" could not be resolved :").append(t);
                microLog.log(sb.toString());
            }
        } catch (IOException e1) {
            log.error("Unable to write to session log " + microLog, e1);
        }
    }

    static void report(final Object context, final ExtAttrDef<?> subject, final ConversionFailureException e, final MicroLog microLog) throws IOException {
        final StringBuilder sb = startReport(context, subject);
        sb.append(": DICOM attribute ").append(e.getAttr());
        sb.append(" has bad value (").append(e.getValue()).append("); unable to derive attribute(s) ");
        sb.append(Arrays.toString(e.getExtAttrs()));
        microLog.log(sb.toString());
    }

    private static void report(final Object context, final ExtAttrDef<?> subject, final NoUniqueValueException e, final MicroLog log) throws IOException {
        final StringBuilder sb = startReport(context, subject);
        if (0 == e.getValues().length) {
            sb.append(" has no value");
        } else {
            sb.append(" has multiple values: ").append(StringUtils.join(e.getValues(), ", "));
        }
        log.log(sb.toString());
    }

    static File getCommonRoot(final Iterable<File> files) {
        final Iterator<File> fi = files.iterator();
        File root = fi.next().getParentFile();
        while (fi.hasNext()) {
            root = getCommonRoot(root, fi.next());
        }
        return root;
    }

    private static File getCommonRoot(final File f1, final File f2) {
        if (null == f1) return null;
        for (File f2p = f2.getParentFile(); null != f2p; f2p = f2p.getParentFile()) {
            if (f1.equals(f2p)) return f1;
        }
        return getCommonRoot(f1.getParentFile(), f2);
    }

    private static final String PROJECT_PREFIX = "Project:";
    private static final String SUBJECT_PREFIX = "Subject:";
    private static final String SESSION_PREFIX = "Session:";

    private static final String PROJECT_SPEC_LABEL = "Project";
    private static final String SUBJECT_SPEC_LABEL = "Subject";
    private static final String SESSION_SPEC_LABEL = "Session";
    private static final String ASSIGNMENT = "\\:";
    private static final String LABEL_PATTERN = "\\w+";

    /**
     * Runs the session XML builder on the named files.
     * Uses properties to control behavior:
     * output.dir is the pathname of a directory where XML should be saved
     * @param args roots (directory paths) for which XML should be written
     */
    public static void main(final String[] args) throws IOException, NoUniqueSessionException, SQLException {
        final XnatAttrDef defaultProjectAttrDef = new ConditionalAttrDef("project",
                                                                         new ContainsAssignmentRule(PatientComments, PROJECT_SPEC_LABEL, ASSIGNMENT, LABEL_PATTERN, Pattern.CASE_INSENSITIVE),
                                                                         new ContainsAssignmentRule(StudyComments, PROJECT_SPEC_LABEL, ASSIGNMENT, LABEL_PATTERN, Pattern.CASE_INSENSITIVE),
                                                                         new EqualsRule(StudyDescription),
                                                                         new EqualsRule(AccessionNumber));

        final XnatAttrDef defaultSubjectAttrDef = new ConditionalAttrDef("subject_ID",
                                                                         new ContainsAssignmentRule(PatientComments, SUBJECT_SPEC_LABEL, ASSIGNMENT, LABEL_PATTERN, Pattern.CASE_INSENSITIVE),
                                                                         new ContainsAssignmentRule(StudyComments, SUBJECT_SPEC_LABEL, ASSIGNMENT, LABEL_PATTERN, Pattern.CASE_INSENSITIVE),
                                                                         new EqualsRule(PatientName));

        final XnatAttrDef defaultSessionAttrDef = new ConditionalAttrDef("label",
                                                                         new ContainsAssignmentRule(PatientComments, SESSION_SPEC_LABEL, ASSIGNMENT, LABEL_PATTERN, Pattern.CASE_INSENSITIVE),
                                                                         new ContainsAssignmentRule(StudyComments, SESSION_SPEC_LABEL, ASSIGNMENT, LABEL_PATTERN, Pattern.CASE_INSENSITIVE),
                                                                         new EqualsRule(PatientID));

        final String outputRootName = System.getProperty("output.dir");
        final File   outputRoot     = (outputRootName == null) ? null : new File(outputRootName);
        if (outputRoot != null && !outputRoot.isDirectory()) {
            System.err.println("output.dir must be a directory");
            System.exit(-1);
        }

        if (4 < args.length) {
            System.out.println("Usage: SessionBuilder dicom-dir [Project:project] [Subject:subject_id] [Session:session_id]");
            System.exit(-1);
        }

        for (int i = 0; i < args.length;) {
            final File  file       = new File(args[i++]);
            final File  out     = (outputRoot == null) ? new File(file.getPath() + ".xml") : new File(outputRoot, file.getName() + ".xml");

            XnatAttrDef project = defaultProjectAttrDef;
            XnatAttrDef subject = defaultSubjectAttrDef;
            XnatAttrDef session = defaultSessionAttrDef;
            while (i < args.length) {
                String nextArg = args[i++];
                if (nextArg.startsWith(PROJECT_PREFIX)) {
                    project = new XnatAttrDef.Constant("project", nextArg.substring(PROJECT_PREFIX.length()));
                } else if (nextArg.startsWith(SUBJECT_PREFIX)) {
                    subject = new XnatAttrDef.Constant("subject_ID", nextArg.substring(SUBJECT_PREFIX.length()));
                } else if (nextArg.startsWith(SESSION_PREFIX)) {
                    session = new XnatAttrDef.Constant("label", nextArg.substring(SESSION_PREFIX.length()));
                } else {
                    i--;  // not using nextArg here after all
                    break;
                }
            }
            log.debug("starting");
            try (final FileWriter writer = new FileWriter(out);
                 final DICOMSessionBuilder sessionBuilder = new DICOMSessionBuilder(file, writer, LabelAttrDef.wrap(project), LabelAttrDef.wrap(subject), LabelAttrDef.wrap(session))) {
                sessionBuilder.setIsInPrearchive(Boolean.parseBoolean(System.getProperty("is.prearc", "true")));
                sessionBuilder.run();
            }
        }
    }
}
