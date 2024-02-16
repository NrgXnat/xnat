/*
 * web: org.nrg.xnat.restlet.actions.importer.ImporterHandlerA
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions.importer;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dcm.xnat.SOPHashDicomFileNamer;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.turbine.utils.PropertiesHelper;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.event.archive.ArchiveStatusProducer;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class ImporterHandlerA extends ArchiveStatusProducer implements Callable<List<String>> {
    public ImporterHandlerA(final Object listenerControl, final UserI u) {
        super(listenerControl, u);
    }

    public abstract List<String> call() throws ClientException, ServerException;

    public DicomObjectIdentifier<XnatProjectdata> getIdentifier() {
        return _identifier;
    }

    public ImporterHandlerA setIdentifier(final DicomObjectIdentifier<XnatProjectdata> identifier) {
        _identifier = identifier;
        return this;
    }

    public DicomFileNamer getNamer() {
        return Optional.ofNullable(_namer)
                .orElseGet(() -> _namer = XDAT.getContextService().getBeanSafely("dicomFileNamer", DicomFileNamer.class));
    }

    public ImporterHandlerA setNamer(final DicomFileNamer namer) {
        _namer = namer;
        return this;
    }

    private static final Logger logger = Logger.getLogger(ImporterHandlerA.class);

    public static final String IMPORT_HANDLER_ATTR = "import-handler";
    public static final String IGNORE_UNPARSABLE_PARAM = "Ignore-Unparsable";
    public static final String SESSION_IMPORTER       = "SI";
    public static final String XAR_IMPORTER           = "XAR";
    public static final String GRADUAL_DICOM_IMPORTER = "gradual-DICOM";
    public static final String DICOM_INBOX_IMPORTER   = "inbox";
    public static final String DICOM_ZIP_IMPORTER     = "DICOM-zip";
    private final static Map<String, Class<? extends ImporterHandlerA>> IMPORTERS = new HashMap<>();

    private static final String   PROP_OBJECT_IDENTIFIER = "org.nrg.import.handler.impl";
    private static final String   IMPORTER_PROPERTIES    = "importer.properties";
    private static final String   CLASS_NAME             = "className";
    private static final String[] PROP_OBJECT_FIELDS     = new String[]{CLASS_NAME};

    static {
        //First, find importers by property file (if it exists)
        //EXAMPLE PROPERTIES FILE 
        //org.nrg.import.handler=NIFTI
        //org.nrg.import.handler.impl.NIFTI.className=org.nrg.import.handler.CustomNiftiImporter:w
        try {
            IMPORTERS.putAll((new PropertiesHelper<ImporterHandlerA>()).buildClassesFromProps(IMPORTER_PROPERTIES, PROP_OBJECT_IDENTIFIER, PROP_OBJECT_FIELDS, CLASS_NAME));

        } catch (Exception e) {
            logger.error("", e);
        }
        //Second, find importers by annotation
        XDAT.getContextService().getBeansOfType(ImporterHandlerPackages.class).values().stream().flatMap(Collection::stream).forEach(pkg -> {
            try {
                final List<Class<?>> classesForPackage = Reflection.getClassesForPackage(pkg);
                for (final Class<?> clazz : classesForPackage) {
                    if (ImporterHandlerA.class.isAssignableFrom(clazz)) {
                        if (!clazz.isAnnotationPresent(ImporterHandler.class)) {
                            continue;
                        }
                        final ImporterHandler annotation = clazz.getAnnotation(ImporterHandler.class);
                        if (annotation != null && !IMPORTERS.containsKey(annotation.handler())) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Found ImporterHandler: " + clazz.getName());
                            }
                            IMPORTERS.put(annotation.handler(), (Class<? extends ImporterHandlerA>) clazz);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static ImporterHandlerA buildImporter(String format, final Object uID, final UserI u, final FileWriterWrapperI fi, Map<String, Object> params) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException, ImporterNotFoundException {
        if (StringUtils.isEmpty(format)) {
            String defaultUploaderImporter = siteConfigPreferences.getUiDefaultCompressedUploaderImporter();
            if (DICOM_ZIP_IMPORTER.equals(defaultUploaderImporter)) {
                format = DICOM_ZIP_IMPORTER;
            } else {
                format = SESSION_IMPORTER;
            }
        }

        Class<? extends ImporterHandlerA> importerImpl = IMPORTERS.get(format);
        if (importerImpl == null) {

            throw new ImporterNotFoundException("Unknown importer implementation specified: " + format, new IllegalArgumentException());
        }

        final Constructor      constructor = importerImpl.getConstructor(Object.class, UserI.class, FileWriterWrapperI.class, Map.class);
        final ImporterHandlerA handler     = (ImporterHandlerA) constructor.newInstance(uID, u, fi, params);
        final ContextService   context     = XDAT.getContextService();

        /* Abuse Spring to inject some additional parameters. Please fix this. */
        final DicomObjectIdentifier identifier = context.getBean("dicomObjectIdentifier", DicomObjectIdentifier.class);
        handler.setIdentifier(identifier);
        return handler;

    }

    /**
     * This method was added to allow other developers to manually add importers to the list, without adding a
     * configuration file.  However, this would some how need to be done before the import is executed (maybe as a
     * servlet?).
     *
     * @return A map of the currently configured importers.
     */
    @SuppressWarnings("unused")
    public static Map<String, Class<? extends ImporterHandlerA>> getImporters() {
        return IMPORTERS;
    }

    private DicomObjectIdentifier _identifier;
    private DicomFileNamer        _namer;
    private static final SiteConfigPreferences siteConfigPreferences = XDAT.getSiteConfigPreferences();
}
