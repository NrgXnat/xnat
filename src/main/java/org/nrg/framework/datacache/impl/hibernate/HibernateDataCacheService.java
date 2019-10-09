/*
 * framework: org.nrg.framework.datacache.impl.hibernate.HibernateDataCacheService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.datacache.impl.hibernate;

import com.fasterxml.jackson.databind.JsonSerializer;
import org.nrg.framework.datacache.DataCacheItem;
import org.nrg.framework.datacache.DataCacheService;
import org.nrg.framework.datacache.SerializerRegistry;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.framework.services.SerializerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Serializable;

@Service
public class HibernateDataCacheService extends AbstractHibernateEntityService<DataCacheItem, DataCacheItemDAO> implements DataCacheService {
    @Autowired
    public void setSerializerRegistry(final SerializerRegistry serializers) {
        _serializers = serializers;
    }

    @Autowired
    public void setSerializerService(final SerializerService serializerService) {
        _serializerService = serializerService;
    }

    @Override
    @Transactional
    public <T extends Serializable> long put(final String key, final T value) {
        if (_log.isDebugEnabled()) {
            _log.debug("Putting an entry for the key " + key + ": " + value.getClass());
        }
        DataCacheItem item = getDao().getByKey(key);
        if (item != null) {
            if (!item.getType().equals(value.getClass().getName())) {
                // TODO: I don't know if this is the right thing to do here. Need to manage new inserts vs updates.
                throw new NrgServiceRuntimeException(NrgServiceError.AlreadyInitialized, "The " + key + " cache item has already been initialized with a different data type.");
            }
            if (_log.isDebugEnabled()) {
                _log.debug("Updating the value for entry " + key);
            }
            item.setValue(serialize(value));
            getDao().update(item);
        } else {
            item = newEntity(key, serialize(value), value.getClass().getName());
            if (_log.isDebugEnabled()) {
                _log.debug("Creating new entry for key " + key);
            }
        }
        return item.getId();
    }

    @Override
    @Transactional
    public <T extends Serializable> T get(final String key) {
        DataCacheItem item = getDao().getByKey(key);
        if (item == null) {
            return null;
        }
        return deserialize(item);
    }

    @Override
    @Transactional
    public <T extends Serializable> T remove(final String key) {
        DataCacheItem item = getDao().getByKey(key);
        if (item != null) {
            T deserialized = deserialize(item);
            getDao().delete(item);
            return deserialized;
        }
        return null;
    }

    @Override
    @Transactional
    public void clean() {
        // TODO: Implement cache maintenance settings to drive the cache persistence parameters.
    }

    private <T extends Serializable> String serialize(final T value) throws NrgServiceRuntimeException {
        try {
            JsonSerializer<? extends Serializable> serializer = _serializers.getSerializer(value.getClass());
            // If there's no special serializer for this class...
            if (serializer == null) {
                return _serializerService.toJson(value);
            }
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new NrgServiceRuntimeException(e);
        }
        return null;
    }

    private <T extends Serializable> T deserialize(final DataCacheItem item) {
        if (item == null) {
            return null;
        }
        try {
            //noinspection unchecked
            return (T) _serializerService.deserializeJson(item.getValue(), Class.forName(item.getType()));
        } catch (IOException | ClassNotFoundException e) {
            throw new NrgServiceRuntimeException(e);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(HibernateDataCacheService.class);

    private SerializerRegistry _serializers;
    private SerializerService  _serializerService;
}
