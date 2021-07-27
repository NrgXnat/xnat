/*
 * web: org.nrg.xnat.archive.XNATSessionBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import com.google.common.collect.ImmutableMap;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.nrg.dcm.xnat.DICOMSessionBuilder;
import org.nrg.dcm.xnat.XnatAttrDef;
import org.nrg.dcm.xnat.XnatImagesessiondataBeanFactory;
import org.nrg.ecat.xnat.PETSessionBuilder;
import org.nrg.framework.services.ContextService;
import org.nrg.resources.SupplementalResourceBuilderUtils;
import org.nrg.session.SessionBuilder;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.utils.PropertiesHelper;
import org.nrg.xft.XFT;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.nrg.xnat.helpers.prearchive.PrearcUtils.*;

@Slf4j
public class XNATSessionBuilder implements Callable<Boolean> {
    //config params for loading injecting a different executor for pooling the session builders.
    private static final String                                                 EXECUTOR_FILE_NAME           = "session-builder.properties";
    private static final String                                                 EXECUTOR_IDENTIFIER          = "org.nrg.SessionBuilder.executor.impl";
    private static final String                                                 SEQUENCE                     = "sequence";
    private static final String                                                 CLASS_NAME                   = "className";
    private static final String[]                                               PROP_OBJECT_FIELDS           = new String[]{CLASS_NAME, SEQUENCE};
    private static final String                                                 PROP_OBJECT_IDENTIFIER       = "org.nrg.SessionBuilder.impl";
    private static final String                                                 SESSION_BUILDER_PROPERTIES   = "session-builder.properties";
    private static final String                                                 PROJECT_PARAM                = "project";
    private static final String                                                 DICOM                        = "DICOM";
    private static final BuilderConfig                                          DICOM_BUILDER                = new BuilderConfig(DICOM, DICOMSessionBuilder.class, 0);
    private static final String                                                 ECAT                         = "ECAT";
    private static final BuilderConfig                                          ECAT_BUILDER                 = new BuilderConfig(ECAT, PETSessionBuilder.class, 1);
    private static final Class<?>[]                                             PARAMETER_TYPES              = new Class[]{File.class, Writer.class};
    private static final List<BuilderConfig>                                    BUILDER_CLASSES              = initializeBuilderClasses();
    private static final List<Class<? extends XnatImagesessiondataBeanFactory>> SESSION_DATA_FACTORY_CLASSES = new ArrayList<>();

    private static ContextService  _contextService  = null;
    private static ExecutorService _executorService = null;

    private final File                dir;
    private final File                xml;
    private final boolean             isInPrearchive;
    private final Map<String, String> params;

    /**
     * @param dir            The directory containing the session to build.
     * @param xml            The XML output location.
     * @param isInPrearchive Indicates whether the session is in the prearchive.
     * @param params         Parameters passed into the session builder for this particular context.
     */
    public XNATSessionBuilder(final File dir, final File xml, final boolean isInPrearchive, final Map<String, String> params) {
        if (null == dir || null == xml) {
            throw new NullPointerException();
        }
        this.dir = dir;
        this.xml = xml;
        this.isInPrearchive = isInPrearchive;
        this.params = ImmutableMap.copyOf(params);
    }

    /**
     * @param dir            The directory containing the session to build.
     * @param xml            The XML output location.
     * @param project        The project with which the session is associated.
     * @param isInPrearchive Indicates whether the session is in the prearchive.
     */
    public XNATSessionBuilder(final File dir, final File xml, final String project, final boolean isInPrearchive) {
        this(dir, xml, isInPrearchive, Collections.singletonMap(PROJECT_PARAM, project));
    }

    public boolean execute() {
        final ExecutorService executor = getExecutor();
        try {
            return executor.submit(this).get();
        } catch (InterruptedException e) {
            log.error("session build interrupted", e);
        } catch (ExecutionException e) {
            log.error("session build failed", e);
        }
        return false;
    }

    /**
     * Deprecated: you should use spring beans instead
     * Add session data bean factory classes to the chain used to map DICOM SOP classes to XNAT session types
     *
     * @param classes session bean factory classes
     *
     * @return this
     */
    @SuppressWarnings("unused")
    @Deprecated
    public XNATSessionBuilder setSessionDataFactoryClasses(final Iterable<Class<? extends XnatImagesessiondataBeanFactory>> classes) {
        SESSION_DATA_FACTORY_CLASSES.clear();
        classes.forEach(SESSION_DATA_FACTORY_CLASSES::add);
        return this;
    }

    /**
     * Iterate over the available Builders to try to generate an xml for the files in this directory.
     * <p/>
     * The iteration will stop once it successfully builds an xml (or runs out of builder configs).
     *
     * @throws IOException When something goes wrong writing the session XML.
     */
    @SuppressWarnings("unchecked")
    public Boolean call() throws IOException {
        xml.getParentFile().mkdirs();

        try (final FileWriter fileWriter = new FileWriter(xml)) {
            if (null == _contextService && SESSION_DATA_FACTORY_CLASSES.isEmpty()) {
                _contextService = XDAT.getContextService();
                try {
                    //Legacy support for a bean of a list of classes
                    SESSION_DATA_FACTORY_CLASSES.addAll(_contextService.getBean("sessionDataFactoryClasses", Collection.class));
                } catch (Exception ignored) {
                    // Ignore
                }
            }

            for (final BuilderConfig bc : BUILDER_CLASSES) {
                switch (bc.getCode()) {
                    case DICOM:
                        buildDicomSession(fileWriter);
                        break;

                    case ECAT:
                        buildPetSession(fileWriter);
                        break;

                    default:
                        buildCustomSession(fileWriter, bc);
                }

                if (xml.exists() && xml.length() > 0) {
                    break;
                }
            }
        }

        // Insert any supplemental resources
        SupplementalResourceBuilderUtils.addSupplementalResourcesToXml(Collections.singleton(xml),
                                                                       Collections.singleton(dir));

        return Boolean.TRUE;
    }

    private void buildCustomSession(final FileWriter fileWriter, final BuilderConfig builderConfig) {
        //this is currently unused... and probably should be re-written.  It was a first pass.
        try {
            final Constructor<? extends SessionBuilder> constructor = builderConfig.sessionBuilderClass.getConstructor(PARAMETER_TYPES);
            try {
                final SessionBuilder sessionBuilder = constructor.newInstance(dir, fileWriter);
                sessionBuilder.setIsInPrearchive(isInPrearchive);
                sessionBuilder.run();
            } catch (IllegalArgumentException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
                log.error("An error occurred trying to build the non-DICOM non-ECAT session", e);
            }
        } catch (SecurityException | NoSuchMethodException e) {
            log.error("An error occurred trying to build the specified session builder class", e);
        }
    }

    private void buildPetSession(final FileWriter fw) {
        //hard coded implementation for ECAT
        final PETSessionBuilder petSessionBuilder = new PETSessionBuilder(dir, fw, params.get(PROJECT_PARAM));
        log.debug("assigning session params for ECAT session builder from {}", params);

        petSessionBuilder.setSessionLabel(params.get(PARAM_LABEL));
        petSessionBuilder.setSubject(params.get(PARAM_SUBJECT_ID));
        petSessionBuilder.setTimezone(Optional.ofNullable(params.get(PARAM_TIMEZONE)).orElseGet(() -> TimeZone.getDefault().toString()));
        petSessionBuilder.setIsInPrearchive(isInPrearchive);
        petSessionBuilder.run();
    }

    private void buildDicomSession(final FileWriter fileWriter) throws IOException {
        // Hard-coded implementation for DICOM.
        // Turn the parameters into an array of XnatAttrDef.Constant attribute definitions
        final boolean createPetMrAsPet = PrearcUtils.HandlePetMr.get(params.get(SEPARATE_PET_MR)) == PrearcUtils.HandlePetMr.Pet;
        final XnatAttrDef[] attrDefs = params.entrySet().stream().map(entry -> new XnatAttrDef.Constant(entry.getKey(), createPetMrAsPet && entry.getKey().equals("label") && entry.getValue().toLowerCase().contains(PrearcUtils.HandlePetMr.PetMr.value())
                                                                                                                        ? new StringBuilder(new StringBuilder(entry.getValue()).reverse().toString().replaceFirst("(?i)rmtep", "TEP")).reverse().toString()
                                                                                                                        : entry.getValue())).toArray(XnatAttrDef[]::new);
        try (final DICOMSessionBuilder dicomSessionBuilder = new DICOMSessionBuilder(dir, fileWriter, attrDefs)) {
            @SuppressWarnings("unchecked") final List<String> excludedFields = XDAT.getContextService().getBean("excludedDicomImportFields", List.class);
            if (excludedFields != null) {
                dicomSessionBuilder.setExcludedFields(excludedFields);
            }
            dicomSessionBuilder.setIsInPrearchive(isInPrearchive);
            if (!SESSION_DATA_FACTORY_CLASSES.isEmpty()) {
                // spring bean sessionDataFactories will take precedence over these in attempting to match
                // classes added to this list will override the defaults in DICOMSessionBuilder
                dicomSessionBuilder.setSessionBeanFactoryClasses(SESSION_DATA_FACTORY_CLASSES);
            }
            if (!params.isEmpty()) {
                dicomSessionBuilder.setParameters(params);
            }
            dicomSessionBuilder.run();
        } catch (IOException e) {
            log.warn("unable to process session directory {}", dir, e);
            throw e;
        } catch (SQLException e) {
            log.error("unable to process session directory {}", dir, e);
        } catch (Throwable e) {
            log.error("An unexpected error occurred trying to process session directory {}", dir, e);
        }
    }

    private static ExecutorService getExecutor() {
        if (_executorService == null) {
            _executorService = XDAT.getContextService().getBeanSafely(ExecutorService.class);
        }
        if (_executorService == null) {
            _executorService = initializeExecutorFromProperties();
        }
        return _executorService;
    }

    private static ExecutorService initializeExecutorFromProperties() {
        final PropertiesHelper.ImplLoader<ExecutorService> loader = new PropertiesHelper.ImplLoader<>(EXECUTOR_FILE_NAME, EXECUTOR_IDENTIFIER);
        try {
            return loader.buildNoArgs(Executors.newFixedThreadPool(PropertiesHelper.GetIntegerProperty(EXECUTOR_FILE_NAME, EXECUTOR_IDENTIFIER + ".size", 2)));
        } catch (IllegalArgumentException | SecurityException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException | ConfigurationException e) {
            log.error("An error occurred trying to build the executor based on the file name {} and identifier {}", EXECUTOR_FILE_NAME, EXECUTOR_IDENTIFIER, e);
            return Executors.newCachedThreadPool();
        }
    }

    private static int getSequence(final String sequence) {
        return StringUtils.isNotBlank(sequence) ? Integer.parseInt(sequence) : 3;
    }

    private static BuilderConfig getBuilderConfig(final Map.Entry<String, Map<String, Object>> entry) {
        final String className = (String) entry.getValue().get(CLASS_NAME);
        final String sequence  = (String) entry.getValue().get(SEQUENCE);
        try {
            return new BuilderConfig(entry.getKey(), Class.forName(className).asSubclass(SessionBuilder.class), getSequence(sequence));
        } catch (NumberFormatException e) {
            log.error("An error occurred trying to convert the value {} to an integer. Please check your builder configuration.", sequence, e);
        } catch (ClassNotFoundException e) {
            log.error("Couldn't locate the class {}. Please check your builder configuration and classpath.", className, e);
        }
        return null;
    }

    private static List<BuilderConfig> initializeBuilderClasses() {
        //EXAMPLE PROPERTIES FILE
        //org.nrg.SessionBuilder.impl=NIFTI
        //org.nrg.SessionBuilder.impl.NIFTI.className=org.nrg.builders.CustomNiftiBuilder
        //org.nrg.SessionBuilder.impl.NIFTI.sequence=3
        final File properties = new File(XFT.GetConfDir(), SESSION_BUILDER_PROPERTIES);
        final List<BuilderConfig> configurations = PropertiesHelper.RetrievePropertyObjects(properties, PROP_OBJECT_IDENTIFIER, PROP_OBJECT_FIELDS).entrySet()
                                                                   .stream()
                                                                   .filter(entry -> StringUtils.isNotBlank((String) entry.getValue().get(CLASS_NAME)))
                                                                   .map(XNATSessionBuilder::getBuilderConfig)
                                                                   .filter(Objects::nonNull).collect(Collectors.toList());
        if (configurations.stream().noneMatch(config -> config.getCode().equals(DICOM))) {
            configurations.add(DICOM_BUILDER);
        }
        if (configurations.stream().noneMatch(config -> config.getCode().equals(ECAT))) {
            configurations.add(ECAT_BUILDER);
        }
        return configurations;
    }

    @Value
    private static class BuilderConfig implements Comparable<BuilderConfig> {
        String                          code;
        Class<? extends SessionBuilder> sessionBuilderClass;
        Integer                         order;

        BuilderConfig(final String code, final Class<? extends SessionBuilder> builderClass, final Integer order) {
            if (code == null) {
                throw new NullPointerException();
            }
            if (builderClass == null) {
                throw new NullPointerException();
            }

            this.code = code;
            this.sessionBuilderClass = builderClass;
            this.order = (order == null) ? 0 : order;
        }

        @Override
        public int compareTo(@Nonnull final BuilderConfig config) {
            return getOrder().compareTo(config.getOrder());
        }
    }
}
