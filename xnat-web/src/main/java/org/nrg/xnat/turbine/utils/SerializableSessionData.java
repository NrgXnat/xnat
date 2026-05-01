/*
 * web: org.nrg.xnat.turbine.utils.SerializableSessionData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.turbine.services.pull.ApplicationTool;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serializable replacement for org.apache.turbine.services.pull.util.SessionData
 * that supports Redis session caching.
 *
 * This class provides a session-scoped storage mechanism for temporary data that
 * persists across requests within a user's session. Unlike the standard Turbine
 * SessionData, this implementation is fully serializable for distributed session
 * management via Redis/Redisson.
 *
 * <p>Usage in Velocity templates:</p>
 * <pre>
 * $sessionData.put("key", $value)
 * $sessionData.get("key")
 * $sessionData.containsKey("key")
 * $sessionData.clear()
 * </pre>
 *
 * <p><strong>Important:</strong> All objects stored in this SessionData must be
 * Serializable for Redis persistence to work correctly.</p>
 *
 * @author Tim Olsen
 */
@Slf4j
public class SerializableSessionData implements ApplicationTool, Serializable {
    private static final long serialVersionUID = 5628391945760419834L;

    /**
     * Sentinel object to represent null values in the map.
     * ConcurrentHashMap does not support null keys or values, so we use
     * this sentinel to distinguish between "key not present" and "key present with null value".
     */
    private static final Object NULL_SENTINEL = new Serializable() {
        private static final long serialVersionUID = 1L;

        @Override
        public String toString() {
            return "NULL_SENTINEL";
        }
    };

    /**
     * Thread-safe, serializable backing store for session data.
     */
    private final ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();


    /**
     * Initialize the tool. Called by Turbine Pull Service when the tool is created.
     *
     * @param data Initialization data (typically unused for session data tools)
     */
    @Override
    public void init(Object data) {
        log.debug("SerializableSessionData init() called");
        // No initialization needed for session data
    }

    /**
     * Refresh the tool. Called by Turbine Pull Service when the tool needs to refresh its state.
     */
    @Override
    public void refresh() {
        log.debug("SerializableSessionData refresh() called");
        // No refresh needed for session data
    }

    /**
     * Retrieves a value from the session data by key.
     *
     * @param key The key to look up (must not be null)
     * @return The value associated with the key, or null if not found
     * @throws IllegalArgumentException if key is null
     */
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        Object value = data.get(key);
        return (value == NULL_SENTINEL) ? null : value;
    }

    /**
     * Stores a key-value pair in the session data.
     *
     * <p><strong>Warning:</strong> The value must be Serializable for Redis persistence
     * to work correctly. Non-serializable values will cause serialization errors when
     * the session is persisted to Redis.</p>
     *
     * @param key The key (must not be null)
     * @param value The value to store (may be null; must be Serializable for Redis)
     * @return The previous value associated with the key, or null if there was none
     * @throws IllegalArgumentException if key is null
     */
    public Object put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Warn if value is not serializable (may cause Redis issues)
        if (value != null && !(value instanceof Serializable)) {
            log.warn("Non-serializable value of type {} stored for key '{}'. This may cause Redis session persistence to fail.",value.getClass().getName(),key);
        }

        Object toStore = (value == null) ? NULL_SENTINEL : value;
        Object previous = data.put(key, toStore);
        return (previous == NULL_SENTINEL) ? null : previous;
    }

    /**
     * Checks if the session data contains the specified key.
     *
     * @param key The key to check
     * @return true if the key exists in the session data, false otherwise
     */
    public boolean containsKey(String key) {
        return key != null && data.containsKey(key);
    }

    /**
     * Removes all key-value pairs from the session data.
     */
    public void clear() {
        log.debug("Clearing all session data ({}} entries)", data.size());
        data.clear();
    }

    /**
     * Returns an iterator over the keys in the session data.
     *
     * @return Iterator over the keys
     */
    public Iterator<String> iterator() {
        return data.keySet().iterator();
    }

    /**
     * Removes the specified key and its associated value from the session data.
     *
     * @param key The key to remove
     * @return The previous value associated with the key, or null if there was none
     */
    public Object remove(String key) {
        if (key == null) {
            return null;
        }
        Object previous = data.remove(key);
        return (previous == NULL_SENTINEL) ? null : previous;
    }

    /**
     * Checks if the session data is empty.
     *
     * @return true if the session data contains no key-value pairs, false otherwise
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Returns the number of key-value pairs in the session data.
     *
     * @return The number of entries
     */
    public int size() {
        return data.size();
    }

    /**
     * Returns a string representation of the session data for debugging.
     *
     * @return String representation showing the number of entries
     */
    @Override
    public String toString() {
        return "SerializableSessionData[entries=" + data.size() + "]";
    }
}
