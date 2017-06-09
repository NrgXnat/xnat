/*
 * prefs: org.nrg.prefs.transformers.AbstractPreferenceTransformer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import org.nrg.framework.generics.AbstractParameterizedWorker;
import org.nrg.framework.services.SerializerService;
import org.nrg.prefs.entities.PreferenceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides basic functions for preference transformers to know their parameterized data type.
 *
 * @param <T> The type that this transformer can create from a serialized instance.
 */
public abstract class AbstractPreferenceTransformer<T> extends AbstractParameterizedWorker<T> implements PreferenceTransformer<T> {
    protected AbstractPreferenceTransformer(final SerializerService serializer) {
        _serializer = serializer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract T transform(final String serialized);

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getValueType() {
        return getParameterizedType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handles(final Class<?> valueType) {
        return getParameterizedType().equals(valueType) || valueType.isArray() && getParameterizedType().equals(valueType.getComponentType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handles(final PreferenceInfo info) {
        return info.getItemType() == null ? handles(info.getValueType()) : handles(info.getItemType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public T[] arrayOf(final String serialized) {
        final List<T> list = listOf(serialized);
        return list.toArray((T[]) Array.newInstance(getParameterizedType(), list.size()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> listOf(final String serialized) {
        try {
            final JsonNode node = _serializer.deserializeJson(serialized);
            final List<T> list = new ArrayList<>();
            if (!node.isArray()) {
                list.add(transform(serialized));
            } else {
                for (final JsonNode jsonNode : node) {
                    list.add(transform(jsonNode.asText()));
                }
            }
            return list;
        } catch (IOException e) {
            _log.warn("An error occurred attempting to deserialize the preference value: " + serialized, e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, T> mapOf(final String serialized) {
        throw new UnsupportedOperationException();
    }

    private static final Logger _log = LoggerFactory.getLogger(AbstractPreferenceTransformer.class);

    private final SerializerService _serializer;
}
