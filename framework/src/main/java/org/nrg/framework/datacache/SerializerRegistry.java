/*
 * framework: org.nrg.framework.datacache.SerializerRegistry
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.datacache;

import com.fasterxml.jackson.databind.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

public class SerializerRegistry extends HashMap<Class<?>, String> {
    public SerializerRegistry() {

    }
    public SerializerRegistry(Map<Class<?>, String> seed) {
        putAll(seed);
    }

    @SuppressWarnings("unchecked")
    public <T> JsonSerializer<? extends T> getSerializer(Class<? extends T> clazz) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (containsKey(clazz)) {
            if (_cache.containsKey(clazz)) {
                return (JsonSerializer<? extends T>) _cache.get(clazz);
            }
            Class<?> serializer = Class.forName(get(clazz));
            if (JsonSerializer.class.isAssignableFrom(serializer)) {
                JsonSerializer<?> instance = serializer.asSubclass(JsonSerializer.class).newInstance();
                _cache.put(clazz, instance);
                return (JsonSerializer<? extends T>) instance;
            } else {
                throw new InstantiationException("The class configured to serialize " + clazz.getName() + " is " + serializer.getName() + ", which is not a valid implementation or extension of the " + JsonSerializer.class.getName() + " class");
            }
        }
        return null;
    }

    private Map<Class<?>, JsonSerializer<?>> _cache = new HashMap<>();
}
