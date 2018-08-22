/*
 * web: org.nrg.xnat.restlet.actions.importer.ImporterMap
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.processor.importer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.ConfigurationException;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.turbine.utils.PropertiesHelper;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.processor.importer.ProcessorImporterHandlerA;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.nrg.xnat.processor.importer.ProcessorImporterHandlerA.DEFAULT_HANDLER;

@Component
@Slf4j
public class ProcessorImporterMap extends HashMap<String, Class<? extends ProcessorImporterHandlerA>> {
    public ProcessorImporterMap(final Set<String> packages, final List<ProcessorImporterHandlerA> importers) throws ConfigurationException, IOException, ClassNotFoundException {
        putAll((new PropertiesHelper<ProcessorImporterHandlerA>()).buildClassesFromProps(IMPORTER_PROPERTIES, PROP_OBJECT_IDENTIFIER, PROP_OBJECT_FIELDS, CLASS_NAME));

        for (final String pkg : packages) {
            final List<Class<?>> classesForPackage = Reflection.getClassesForPackage(pkg);
            for (final Class<?> clazz : classesForPackage) {
                if (ProcessorImporterHandlerA.class.isAssignableFrom(clazz)) {
                    if (!clazz.isAnnotationPresent(ImporterHandler.class)) {
                        continue;
                    }
                    final Class<? extends ProcessorImporterHandlerA> importHandlerClass = clazz.asSubclass(ProcessorImporterHandlerA.class);
                    final ImporterHandler                   annotation         = importHandlerClass.getAnnotation(ImporterHandler.class);
                    if (annotation != null && !containsKey(annotation.handler())) {
                        log.debug("Found ImporterHandler: " + importHandlerClass.getName());
                        put(annotation.handler(), importHandlerClass);
                    }
                }
            }
        }

        for (final ProcessorImporterHandlerA importer : importers) {
            final Class<? extends ProcessorImporterHandlerA> importHandlerClass = importer.getClass();
            final ImporterHandler                   annotation         = importHandlerClass.getAnnotation(ImporterHandler.class);
            if (annotation != null && !containsKey(annotation.handler())) {
                log.debug("Found ImporterHandler: " + importHandlerClass.getName());
                _importers.put(annotation.handler(), importer);
            }
        }
    }

    public ProcessorImporterHandlerA getImporter(final String handler) {
        return _importers.get(handler);
    }

    public ProcessorImporterHandlerA getImporter(final Object listener, final UserI user, final FileWriterWrapperI writer, final Map<String, Object> parameters) {
        return getImporter(DEFAULT_HANDLER, listener, user, writer, parameters);
    }

    public ProcessorImporterHandlerA getImporter(final String format, final Object listener, final UserI user, final FileWriterWrapperI writer, final Map<String, Object> parameters) {
        final Class<? extends ProcessorImporterHandlerA> clazz = get(format);
        //return _importers;
        return null;
    }

    private static final String   PROP_OBJECT_IDENTIFIER = "org.nrg.import.handler.impl";
    private static final String   IMPORTER_PROPERTIES    = "importer.properties";
    private static final String   CLASS_NAME             = "className";
    private static final String[] PROP_OBJECT_FIELDS     = new String[]{CLASS_NAME};

    private final Map<String, ProcessorImporterHandlerA> _importers = new HashMap<>();
}