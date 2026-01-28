/*
 * web: org.nrg.xnat.turbine.utils.SerializableSessionDataTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.utils;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Unit tests for SerializableSessionData to verify serialization compatibility
 * with Redis session caching and API compatibility with Turbine's SessionData.
 *
 * @author Tim Olsen
 */
public class SerializableSessionDataTest {

    private SerializableSessionData sessionData;

    @Before
    public void setUp() {
        sessionData = new SerializableSessionData();
    }

    @Test
    public void testBasicPutAndGet() {
        sessionData.put("key1", "value1");
        assertEquals("value1", sessionData.get("key1"));

        sessionData.put("key2", 123);
        assertEquals(123, sessionData.get("key2"));

        sessionData.put("key3", true);
        assertEquals(true, sessionData.get("key3"));
    }

    @Test
    public void testPutReturnsOldValue() {
        assertNull(sessionData.put("key", "value1"));
        assertEquals("value1", sessionData.put("key", "value2"));
        assertEquals("value2", sessionData.get("key"));
    }

    @Test
    public void testNullValueHandling() {
        // Put null value
        sessionData.put("nullKey", null);

        // Should contain the key
        assertTrue(sessionData.containsKey("nullKey"));

        // Should return null
        assertNull(sessionData.get("nullKey"));

        // Size should be 1
        assertEquals(1, sessionData.size());
    }

    @Test
    public void testNullValueReplace() {
        sessionData.put("key", "value");
        assertEquals("value", sessionData.get("key"));

        // Replace with null
        assertEquals("value", sessionData.put("key", null));
        assertNull(sessionData.get("key"));
        assertTrue(sessionData.containsKey("key"));

        // Replace null with value
        assertNull(sessionData.put("key", "newValue"));
        assertEquals("newValue", sessionData.get("key"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutNullKeyThrowsException() {
        sessionData.put(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNullKeyThrowsException() {
        sessionData.get(null);
    }

    @Test
    public void testContainsKey() {
        assertFalse(sessionData.containsKey("key"));

        sessionData.put("key", "value");
        assertTrue(sessionData.containsKey("key"));

        sessionData.put("nullKey", null);
        assertTrue(sessionData.containsKey("nullKey"));

        assertFalse(sessionData.containsKey("nonexistent"));
        assertFalse(sessionData.containsKey(null));
    }

    @Test
    public void testRemove() {
        sessionData.put("key", "value");
        assertTrue(sessionData.containsKey("key"));

        assertEquals("value", sessionData.remove("key"));
        assertFalse(sessionData.containsKey("key"));
        assertNull(sessionData.get("key"));
    }

    @Test
    public void testRemoveNullValue() {
        sessionData.put("key", null);
        assertTrue(sessionData.containsKey("key"));

        assertNull(sessionData.remove("key"));
        assertFalse(sessionData.containsKey("key"));
    }

    @Test
    public void testRemoveNonexistentKey() {
        assertNull(sessionData.remove("nonexistent"));
        assertNull(sessionData.remove(null));
    }

    @Test
    public void testClear() {
        sessionData.put("key1", "value1");
        sessionData.put("key2", "value2");
        sessionData.put("key3", null);

        assertEquals(3, sessionData.size());

        sessionData.clear();

        assertEquals(0, sessionData.size());
        assertTrue(sessionData.isEmpty());
        assertFalse(sessionData.containsKey("key1"));
        assertFalse(sessionData.containsKey("key2"));
        assertFalse(sessionData.containsKey("key3"));
    }

    @Test
    public void testIsEmpty() {
        assertTrue(sessionData.isEmpty());

        sessionData.put("key", "value");
        assertFalse(sessionData.isEmpty());

        sessionData.clear();
        assertTrue(sessionData.isEmpty());
    }

    @Test
    public void testSize() {
        assertEquals(0, sessionData.size());

        sessionData.put("key1", "value1");
        assertEquals(1, sessionData.size());

        sessionData.put("key2", "value2");
        assertEquals(2, sessionData.size());

        sessionData.put("key3", null);
        assertEquals(3, sessionData.size());

        sessionData.remove("key1");
        assertEquals(2, sessionData.size());

        sessionData.clear();
        assertEquals(0, sessionData.size());
    }

    @Test
    public void testIterator() {
        sessionData.put("key1", "value1");
        sessionData.put("key2", "value2");
        sessionData.put("key3", null);

        Iterator<String> iterator = sessionData.iterator();
        Set<String> keys = new HashSet<>();
        iterator.forEachRemaining(keys::add);

        assertEquals(3, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
        assertTrue(keys.contains("key3"));
    }

    @Test
    public void testIteratorEmptyData() {
        Iterator<String> iterator = sessionData.iterator();
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testApplicationToolInterface() {
        // Test init and refresh methods (should not throw exceptions)
        sessionData.init(null);
        sessionData.refresh();

        // Operations should still work after init/refresh
        sessionData.put("key", "value");
        assertEquals("value", sessionData.get("key"));
    }

    @Test
    public void testSerializability() throws Exception {
        // Populate with various data types
        sessionData.put("string", "value");
        sessionData.put("integer", 42);
        sessionData.put("long", 123456789L);
        sessionData.put("boolean", true);
        sessionData.put("null", null);

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(sessionData);
        oos.close();

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SerializableSessionData deserialized = (SerializableSessionData) ois.readObject();
        ois.close();

        // Verify all data survived serialization
        assertEquals("value", deserialized.get("string"));
        assertEquals(42, deserialized.get("integer"));
        assertEquals(123456789L, deserialized.get("long"));
        assertEquals(true, deserialized.get("boolean"));
        assertNull(deserialized.get("null"));
        assertTrue(deserialized.containsKey("null"));
        assertEquals(5, deserialized.size());
    }

    @Test
    public void testSerializationWithComplexObjects() throws Exception {
        // Test with serializable complex objects
        sessionData.put("array", new String[]{"a", "b", "c"});
        sessionData.put("object", new SerializableTestObject("test", 123));

        // Serialize and deserialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(sessionData);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SerializableSessionData deserialized = (SerializableSessionData) ois.readObject();
        ois.close();

        // Verify
        String[] array = (String[]) deserialized.get("array");
        assertArrayEquals(new String[]{"a", "b", "c"}, array);

        SerializableTestObject obj = (SerializableTestObject) deserialized.get("object");
        assertEquals("test", obj.name);
        assertEquals(123, obj.value);
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        final int threadCount = 10;
        final int operationsPerThread = 100;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(threadCount);
        final AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready

                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "thread" + threadId + "_key" + j;
                        sessionData.put(key, "value" + j);

                        if (sessionData.containsKey(key)) {
                            Object value = sessionData.get(key);
                            if (value != null && value.equals("value" + j)) {
                                successCount.incrementAndGet();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown(); // Start all threads
        doneLatch.await(); // Wait for all threads to finish

        // All operations should succeed
        assertEquals(threadCount * operationsPerThread, successCount.get());

        // Verify final size
        assertEquals(threadCount * operationsPerThread, sessionData.size());
    }

    @Test
    public void testToString() {
        String str = sessionData.toString();
        assertTrue(str.contains("SerializableSessionData"));
        assertTrue(str.contains("entries=0"));

        sessionData.put("key1", "value1");
        sessionData.put("key2", "value2");

        str = sessionData.toString();
        assertTrue(str.contains("entries=2"));
    }

    /**
     * Helper class for testing serialization of complex objects.
     */
    private static class SerializableTestObject implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final int value;

        public SerializableTestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}
