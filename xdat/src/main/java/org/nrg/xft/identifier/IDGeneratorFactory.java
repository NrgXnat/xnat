/*
 * core: org.nrg.xft.identifier.IDGeneratorFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.identifier;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Service
@Slf4j
public class IDGeneratorFactory {
    public static final String KEY_SUBJECTS    = "xnat_subjectdata";
    public static final String KEY_EXPERIMENTS = "xnat_experimentdata";
    public static final String KEY_VISITS      = "xnat_pvisitdata";
    public static final int    DEFAULT_DIGITS  = 5;
    public static final String DEFAULT_COLUMN  = "id";

    @Nonnull
    public static IDGeneratorFactory getInstance() {
        return INSTANCE;
    }

    @Nonnull
    public static IDGeneratorI GetIDGenerator() {
        return GetIDGenerator(DEFAULT_GENERATOR_CLASSNAME);
    }

    @Nonnull
    public static IDGeneratorI GetIDGenerator(final String className) {
        final Class<? extends IDGeneratorI> clazz = StringUtils.equals(DEFAULT_GENERATOR_CLASSNAME, className) ? getDefaultGeneratorClass() : getGeneratorClass(className);
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            log.error("Couldn't instantiate an instance of the {} ID generator class.", clazz.getName(), e);
            throw new NrgServiceRuntimeException(e);
        } catch (IllegalAccessException e) {
            log.error("Not allowed to access the {} ID generator class.", clazz.getName(), e);
            throw new NrgServiceRuntimeException(e);
        }
    }

    @Autowired
    public IDGeneratorFactory(final Map<String, IDGeneratorI> generators) {
        INSTANCE = this;
        for (final IDGeneratorI generator : generators.values()) {
            _generators.put(generator.getTable(), generator);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public IDGeneratorI getIDGenerator(final String key) {
        return getIDGenerator(key, DEFAULT_GENERATOR_CLASSNAME, DEFAULT_COLUMN, DEFAULT_DIGITS, null);
    }

    public IDGeneratorI getIDGenerator(final String key, final String className, final String column, final int digits, final String code) {
        final String normalized = StringUtils.lowerCase(key);
        log.debug("Got request for ID generator for table {}", key);
        if (!_generators.containsKey(normalized)) {
            log.debug("Didn't find ID generator for table {}, creating new instance", key);
            final IDGeneratorI generator = GetIDGenerator(className);
            generator.setTable(normalized);
            generator.setColumn(column);
            generator.setDigits(digits);
            generator.setCode(code);
            _generators.put(normalized, generator);
        }
        return _generators.get(normalized);
    }

    @Nullable
    public IDGeneratorI setIDGenerator(final String key, final IDGeneratorI generator) {
        return _generators.put(StringUtils.lowerCase(key), generator);
    }

    @Nonnull
    private static Class<? extends IDGeneratorI> getGeneratorClass(final String className) {
        try {
            return Class.forName(className).asSubclass(IDGeneratorI.class);
        } catch (ClassNotFoundException e) {
            throw new NrgServiceRuntimeException("Didn't find the ID generator class " + className, e);
        }
    }

    @Nonnull
    private static Class<? extends IDGeneratorI> getDefaultGeneratorClass() {
        if (DEFAULT_GENERATOR_CLASS == null) {
            DEFAULT_GENERATOR_CLASS = getGeneratorClass(DEFAULT_GENERATOR_CLASSNAME);
        }
        return DEFAULT_GENERATOR_CLASS;
    }

    private static final String       DEFAULT_GENERATOR_CLASSNAME = "org.nrg.xnat.turbine.utils.IDGenerator";
    private static final List<String> DEFAULT_GENERATORS          = Arrays.asList(KEY_SUBJECTS, KEY_EXPERIMENTS, KEY_VISITS);

    private static IDGeneratorFactory            INSTANCE;
    private static Class<? extends IDGeneratorI> DEFAULT_GENERATOR_CLASS;

    private final Map<String, IDGeneratorI> _generators = new HashMap<>();
}
