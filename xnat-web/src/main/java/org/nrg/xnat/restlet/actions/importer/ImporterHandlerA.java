/*
 * web: org.nrg.xnat.restlet.actions.importer.ImporterHandlerA
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions.importer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.utilities.Reflection;
import org.nrg.status.StatusProducer;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.archive.DicomZipImporter;
import org.nrg.xnat.archive.GradualDicomImporter;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xdat.turbine.utils.PropertiesHelper;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public abstract class ImporterHandlerA  extends StatusProducer implements Callable<List<String>>{


    public ImporterHandlerA(final Object listenerControl, final UserI u,  final FileWriterWrapperI fw, final Map<String,Object> params) {
        super((listenerControl==null)?u:listenerControl);
    }

    public abstract List<String> call() throws ClientException, ServerException;

    private static final Logger logger = Logger.getLogger(ImporterHandlerA.class);

    public static final String IMPORT_HANDLER_ATTR = "import-handler";

    public static final String SESSION_IMPORTER="SI";
    public static final String XAR_IMPORTER="XAR";
    public static final String GRADUAL_DICOM_IMPORTER="gradual-DICOM";
    public static final String DICOM_ZIP_IMPORTER="DICOM-zip";
    public static final String BLANK_PREARCHIVE_ENTRY="blank";

    static String DEFAULT_HANDLER=SESSION_IMPORTER;
    final static Map<String,Class<? extends ImporterHandlerA>> IMPORTERS= new HashMap<>();

    private static final String PROP_OBJECT_IDENTIFIER = "org.nrg.import.handler.impl";
    private static final String IMPORTER_PROPERTIES = "importer.properties";
    private static final String CLASS_NAME = "className";
    private static final String[] PROP_OBJECT_FIELDS = new String[]{CLASS_NAME};
    static{
    	//First, find importers by property file (if it exists)
        //EXAMPLE PROPERTIES FILE 
        //org.nrg.import.handler=NIFTI
        //org.nrg.import.handler.impl.NIFTI.className=org.nrg.import.handler.CustomNiftiImporter:w
        try {
            IMPORTERS.putAll((new PropertiesHelper<ImporterHandlerA>()).buildClassesFromProps(IMPORTER_PROPERTIES, PROP_OBJECT_IDENTIFIER, PROP_OBJECT_FIELDS, CLASS_NAME));

        } catch (Exception e) {
            logger.error("",e);
        }
        //Second, find importers by annotation
        final ImporterHandlerPackages packages = XDAT.getContextService().getBean("importerHandlerPackages",ImporterHandlerPackages.class);
        for (final String pkg : packages) {
			try {
				final List<Class<?>> classesForPackage = Reflection.getClassesForPackage(pkg);
				for (final Class<?> clazz : classesForPackage) {
                    if (ImporterHandlerA.class.isAssignableFrom(clazz)) {
                        if (!clazz.isAnnotationPresent(ImporterHandler.class)) {
                       		continue;
                       	}
                       	ImporterHandler anno = clazz.getAnnotation(ImporterHandler.class);
                        if (anno!=null && !IMPORTERS.containsKey(anno.handler())) {
                        	if (logger.isDebugEnabled()) {
                        		logger.debug("Found ImporterHandler: " + clazz.getName());
                        	}
                           	IMPORTERS.put(anno.handler(),(Class<? extends ImporterHandlerA>)clazz);
                        }
                    }
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
        }
    }

    public static ImporterHandlerA buildImporter(String format,final Object uID, final UserI u, final FileWriterWrapperI fi, Map<String,Object> params) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException, ImporterNotFoundException {
        if(StringUtils.isEmpty(format)){
            format=DEFAULT_HANDLER;
        }

        Class<? extends ImporterHandlerA> importerImpl=IMPORTERS.get(format);
        if(importerImpl==null){

            throw new ImporterNotFoundException("Unknown importer implementation specified: " + format,new IllegalArgumentException());
        }

        final Constructor con=importerImpl.getConstructor(Object.class, UserI.class, FileWriterWrapperI.class, Map.class);
        final ImporterHandlerA handler = (ImporterHandlerA)con.newInstance(uID, u, fi, params);

        /* Abuse Spring to inject some additional parameters. Please fix this. */
        if (GradualDicomImporter.class.equals(importerImpl)) {
            final ContextService context = XDAT.getContextService();
            final GradualDicomImporter gdi = (GradualDicomImporter)handler;
            gdi.setIdentifier(context.getBean("dicomObjectIdentifier", DicomObjectIdentifier.class));
            try {
                final DicomFileNamer namer = context.getBean("dicomFileNamer", DicomFileNamer.class);
                if (null != namer) {
                    gdi.setNamer(namer);
                }
            } catch (NoSuchBeanDefinitionException e) {
                logger.debug("no DicomFileNamer specified", e);
            }
        } else if (DicomZipImporter.class.equals(importerImpl)) {
            final ContextService context = XDAT.getContextService();
            final DicomZipImporter dzi = (DicomZipImporter)handler;
            dzi.setIdentifier(context.getBean("dicomObjectIdentifier", DicomObjectIdentifier.class));
            try {
                final DicomFileNamer namer = context.getBean("dicomFileNamer", DicomFileNamer.class);
                if (null != namer) {
                    dzi.setNamer(namer);
                }
            } catch (NoSuchBeanDefinitionException e) {
                logger.debug("no DicomFileNamer specified", e);
            }
        }

        /* Abuse Spring to inject some additional parameters. Please fix this. */
        if (GradualDicomImporter.class.equals(importerImpl)) {
            final ContextService context = XDAT.getContextService();
            final GradualDicomImporter gdi = (GradualDicomImporter)handler;
            gdi.setIdentifier(context.getBean("dicomObjectIdentifier", DicomObjectIdentifier.class));
            try {
                final DicomFileNamer namer = context.getBean("dicomFileNamer", DicomFileNamer.class);
                if (null != namer) {
                    gdi.setNamer(namer);
                }
            } catch (NoSuchBeanDefinitionException e) {
                logger.debug("no DicomFileNamer specified", e);
            }
        } else if (DicomZipImporter.class.equals(importerImpl)) {
            final ContextService context = XDAT.getContextService();
            final DicomZipImporter dzi = (DicomZipImporter)handler;
            dzi.setIdentifier(context.getBean("dicomObjectIdentifier", DicomObjectIdentifier.class));
            try {
                final DicomFileNamer namer = context.getBean("dicomFileNamer", DicomFileNamer.class);
                if (null != namer) {
                    dzi.setNamer(namer);
                }
            } catch (NoSuchBeanDefinitionException e) {
                logger.debug("no DicomFileNamer specified", e);
            }
        }

        return handler;

    }

    /**
     * This method was added to allow other developers to manually add importers to the list, without adding a configuration file.  However, this would some how need to be done before the import is executed (maybe as a servlet?).
     * @return
     */
    public static Map<String,Class<? extends ImporterHandlerA>> getImporters(){
        return IMPORTERS;
    }
}
