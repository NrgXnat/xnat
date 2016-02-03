/*
 * org.nrg.xnat.archive.XNATSessionBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/17/13 2:15 PM
 */
package org.nrg.xnat.archive;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.configuration.ConfigurationException;
import org.nrg.dcm.xnat.DICOMSessionBuilder;
import org.nrg.dcm.xnat.XnatAttrDef;
import org.nrg.dcm.xnat.XnatImagesessiondataBeanFactory;
import org.nrg.ecat.xnat.PETSessionBuilder;
import org.nrg.framework.services.ContextService;
import org.nrg.session.SessionBuilder;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.utils.PropertiesHelper;
import org.nrg.xft.XFT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


public class XNATSessionBuilder implements Callable<Boolean> {
    private static final Logger logger = LoggerFactory.getLogger(XNATSessionBuilder.class);

    //config params for loading injecting a different executor for pooling the session builders.
    private static final String _executorFileName = "session-builder.properties";
    private static final String _executorIdentifier = "org.nrg.SessionBuilder.executor.impl";

    //config params for session builder specification
    private static final String SEQUENCE = "sequence";
    private static final String CLASS_NAME = "className";
    private static final String[] PROP_OBJECT_FIELDS = new String[]{CLASS_NAME, SEQUENCE};
    private static final String PROP_OBJECT_IDENTIFIER = "org.nrg.SessionBuilder.impl";
    private static final String SESSION_BUILDER_PROPERTIES = "session-builder.properties";

    private static final String PROJECT_PARAM = "project";


    private static final String DICOM = "DICOM";
    private static final BuilderConfig DICOM_BUILDER = new BuilderConfig(DICOM, DICOMSessionBuilder.class, 0);

    private static final String ECAT = "ECAT";
    private static final BuilderConfig ECAT_BUILDER = new BuilderConfig(ECAT, PETSessionBuilder.class, 1);

    private static List<BuilderConfig> builderClasses;

    private static final Class<?>[] PARAMETER_TYPES = new Class[]{File.class, Writer.class};

    private static final List<Class<? extends XnatImagesessiondataBeanFactory>> sessionDataFactoryClasses = Lists.newArrayList();
    private static ContextService contextService = null;

    private final File dir, xml;
    private final boolean isInPrearchive;
    private final Map<String, String> params;

    static {
        builderClasses = new ArrayList<>();

        //EXAMPLE PROPERTIES FILE
        //org.nrg.SessionBuilder.impl=NIFTI
        //org.nrg.SessionBuilder.impl.NIFTI.className=org.nrg.builders.CustomNiftiBuilder
        //org.nrg.SessionBuilder.impl.NIFTI.sequence=3
        try {
            final File props = new File(XFT.GetConfDir(), SESSION_BUILDER_PROPERTIES);
            final Map<String, Map<String, Object>> confBuilders = PropertiesHelper.RetrievePropertyObjects(props, PROP_OBJECT_IDENTIFIER, PROP_OBJECT_FIELDS);
            for (final String key : confBuilders.keySet()) {
                final String className = (String) confBuilders.get(key).get(CLASS_NAME);
                final String seqS = (String) confBuilders.get(key).get(SEQUENCE);

                if (className != null) {
                    try {
                        final Class<?> c = Class.forName(className);
                        Integer seq = 3;//default
                        if (seqS != null) {
                            seq = Integer.valueOf(seqS);
                        }
                        builderClasses.add(new BuilderConfig(key, c, seq));
                    } catch (NumberFormatException e) {
                        LoggerFactory.getLogger(XNATSessionBuilder.class).error("An error occurred trying to convert the value " + seqS + " to an integer. Please check your builder configuration.", e);
                    } catch (ClassNotFoundException e) {
                        LoggerFactory.getLogger(XNATSessionBuilder.class).error("Couldn't locate the class " + className + ". Please check your builder configuration and classpath.", e);
                    }
                }
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(XNATSessionBuilder.class).error("", e);
        }

        if (!CollectionUtils.exists(builderClasses, new Predicate() {
            public boolean evaluate(Object bc) {
                return ((BuilderConfig) bc).getCode().equals(DICOM);
            }
        })) {
            builderClasses.add(DICOM_BUILDER);
        }

        if (!CollectionUtils.exists(builderClasses, new Predicate() {
            public boolean evaluate(Object bc) {
                return ((BuilderConfig) bc).getCode().equals(ECAT);
            }
        })) {
            builderClasses.add(ECAT_BUILDER);
        }
    }

    /**
     * @param dir               The directory containing the session to build.
     * @param xml               The XML output location.
     * @param isInPrearchive    Indicates whether the session is in the prearchive.
     * @param params            Parameters passed into the session builder for this particular context.
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
     * @param dir               The directory containing the session to build.
     * @param xml               The XML output location.
     * @param project           The project with which the session is associated.
     * @param isInPrearchive    Indicates whether the session is in the prearchive.
     */
    public XNATSessionBuilder(final File dir, final File xml, final String project, final boolean isInPrearchive) {
        this(dir, xml, isInPrearchive, Collections.singletonMap(PROJECT_PARAM, project));
    }

    public Boolean execute() {
        final ExecutorService executor = getExecutor();
        try {
            return executor.submit(this).get();
        } catch (InterruptedException e) {
            logger.error("session build interrupted", e);
            return false;
        } catch (ExecutionException e) {
            logger.error("session build failed", e);
            return false;
        }
    }

    /**
     * Add session data bean factory classes to the chain used to map DICOM SOP classes to XNAT session types
     *
     * @param classes session bean factory classes
     *
     * @return this
     */
    @SuppressWarnings("unused")
    public XNATSessionBuilder setSessionDataFactoryClasses(final Iterable<Class<? extends XnatImagesessiondataBeanFactory>> classes) {
        sessionDataFactoryClasses.clear();
        Iterables.addAll(sessionDataFactoryClasses, classes);
        return this;
    }

    /**
     * Iterate over the available Builders to try to generate an xml for the files in this directory.
     * <p/>
     * The iteration will stop once it successfully builds an xml (or runs out of builder configs).
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public Boolean call() throws IOException {
        xml.getParentFile().mkdirs();
        final FileWriter fw = new FileWriter(xml);

        if (null == contextService && sessionDataFactoryClasses.isEmpty()) {
            contextService = XDAT.getContextService();
            sessionDataFactoryClasses.addAll(contextService.getBean("sessionDataFactoryClasses", Collection.class));
        }

        for (final BuilderConfig bc : builderClasses) {
            switch (bc.getCode()) {
                case DICOM:
                    buildDicomSession(fw);
                    break;

                case ECAT:
                    buildPetSession(fw);
                    break;

                default:
                    buildCustomSession(fw, bc);
            }

            if (xml.exists() && xml.length() > 0) {
                break;
            }
        }

        return Boolean.TRUE;
    }

    @SuppressWarnings("unchecked")
    private void buildCustomSession(final FileWriter fw, final BuilderConfig bc) {
        //this is currently unused... and probably should be re-written.  It was a first pass.
        try {
            Constructor con = bc.c.getConstructor(PARAMETER_TYPES);
            try {
                SessionBuilder sessionBuilder = (SessionBuilder) con.newInstance(dir.getPath(), fw);
                sessionBuilder.setIsInPrearchive(isInPrearchive);
                sessionBuilder.run();
            } catch (IllegalArgumentException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
                logger.error("An error occurred trying to build the non-DICOM non-ECAT session", e);
            }
        } catch (SecurityException | NoSuchMethodException e) {
            logger.error("An error occurred trying to build the specified session builder class", e);
        }
    }

    private void buildPetSession(final FileWriter fw) {
        //hard coded implementation for ECAT
        final PETSessionBuilder petSessionBuilder = new PETSessionBuilder(dir, fw, params.get(PROJECT_PARAM));
        logger.debug("assigning session params for ECAT session builder from {}", params);

        petSessionBuilder.setSessionLabel(params.get("label"));
        petSessionBuilder.setSubject(params.get("subject_ID"));
        petSessionBuilder.setTimezone(params.containsKey("TIMEZONE") ? params.get("TIMEZONE") : TimeZone.getDefault().toString());
        petSessionBuilder.setIsInPrearchive(isInPrearchive);
        petSessionBuilder.run();
    }

    private void buildDicomSession(final FileWriter fw) throws IOException {
        // Hard-coded implementation for DICOM.
        // Turn the parameters into an array of XnatAttrDef.Constant attribute definitions
        final boolean createPetMrAsPet = params.containsKey("separatePetMr") && params.get("separatePetMr").equals("pet");
        final ArrayList<XnatAttrDef> xnatAttrDefs = Lists.newArrayList(Iterables.transform(params.entrySet(),
                new Function<Map.Entry<String, String>, XnatAttrDef>() {
                    public XnatAttrDef apply(final Map.Entry<String, String> me) {
                        final String key = me.getKey();
                        final String value;
                        if (createPetMrAsPet && key.equals("label") && me.getValue().toUpperCase().contains("PETMR")) {
                            value = new StringBuilder(new StringBuilder(me.getValue()).reverse().toString().replaceFirst("(?i)rmtep", "TEP")).reverse().toString();
                        } else {
                            value = me.getValue();
                        }
                        return new XnatAttrDef.Constant(key, value);
                    }
                }));
        final XnatAttrDef attrDefs[] = xnatAttrDefs.toArray(new XnatAttrDef[xnatAttrDefs.size()]);

        try (final DICOMSessionBuilder dicomSessionBuilder = new DICOMSessionBuilder(dir, fw, attrDefs)) {
            @SuppressWarnings("unchecked")
            final List<String> excludedFields = XDAT.getContextService().getBean("excludedDicomImportFields", List.class);
            if (excludedFields != null) {
                dicomSessionBuilder.setExcludedFields(excludedFields);
            }
            dicomSessionBuilder.setIsInPrearchive(isInPrearchive);
            if (!sessionDataFactoryClasses.isEmpty()) {
                dicomSessionBuilder.setSessionBeanFactories(sessionDataFactoryClasses);
            }
            if (!params.isEmpty()) {
                dicomSessionBuilder.setParameters(params);
            }
            dicomSessionBuilder.run();
        } catch (IOException e) {
            logger.warn("unable to process session directory " + dir, e);
            throw e;
        } catch (SQLException e) {
            logger.error("unable to process session directory " + dir, e);
        } catch (Throwable e) {
            logger.error("", e);
        }
    }

    private static ExecutorService _executorService = null;

    public static ExecutorService getExecutor() {
        if (_executorService == null) {
            PropertiesHelper.ImplLoader<ExecutorService> loader = new PropertiesHelper.ImplLoader<>(_executorFileName, _executorIdentifier);
            try {
                _executorService = loader.buildNoArgs(Executors.newFixedThreadPool(PropertiesHelper.GetIntegerProperty(_executorFileName, _executorIdentifier + ".size", 2)));
            } catch (IllegalArgumentException | SecurityException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException | ConfigurationException e) {
                logger.error("An error occurred trying to build the executor based on the file name " + _executorFileName + " and identifier " + _executorIdentifier, e);
            }
        }

        return _executorService;
    }

    private static class BuilderConfig implements Comparable {
        protected String code;
        protected Class c;
        protected Integer order;

        public BuilderConfig(final String code, final Class c, final Integer order) {
            if (code == null) throw new NullPointerException();
            if (c == null) throw new NullPointerException();

            this.code = code;
            this.c = c;
            this.order = (order == null) ? 0 : order;
        }

        @Override
        public int compareTo(Object object) {
            if (object == null) {
                throw new NullPointerException();
            }
            if (!BuilderConfig.class.isAssignableFrom(object.getClass())) {
                throw new ClassCastException("Can't cast from " + object.getClass().getName() + " to " + BuilderConfig.class.getName());
            }
            return getOrder().compareTo(((BuilderConfig) object).getOrder());
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Class getC() {
            return c;
        }

        public void setC(Class c) {
            this.c = c;
        }

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }
    }
}
