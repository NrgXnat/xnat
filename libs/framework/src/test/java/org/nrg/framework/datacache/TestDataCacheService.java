/*
 * framework: org.nrg.framework.datacache.TestDataCacheService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.datacache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.configuration.FrameworkConfig;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.test.OrmTestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {OrmTestConfiguration.class, FrameworkConfig.class})
public class TestDataCacheService {
    public static final String KEY1 = "key1";
    public static final String KEY2 = "key2";
    public static final String KEY3 = "key3";
    public static final String STR1 = "string1";
    public static final String STR2 = "string2";
    public static final String STR3 = "string3";
    public static final String MAP_KEY = "map";
    public static final HashMap<String, String> MAP_VAL = new HashMap<String, String>() {{
        put(KEY1, STR1);
        put(KEY2, STR2);
        put(KEY3, STR3);
    }};
    public static final String UID1 = "1.2.3.4.5.6.7";
    public static final String UID2 = "2.3.4.5.6.7.8";
    public static final String UID3 = "3.4.5.6.7.8.9";

    @Test
    public void testSimpleItem() throws NrgServiceException {
        _service.put(KEY1, STR1);
        _service.put(KEY2, STR2);
        _service.put(KEY3, STR3);
        String string1 = _service.get(KEY1);
        String string2 = _service.get(KEY2);
        String string3 = _service.get(KEY3);
        assertEquals(STR1, string1);
        assertEquals(STR2, string2);
        assertEquals(STR3, string3);
    }

    @Test
    public void testMapStore() throws NrgServiceException {
        _service.put(MAP_KEY, MAP_VAL);
        HashMap<String, String> received = _service.get(MAP_KEY);
        assertEquals(STR1, received.get(KEY1));
        assertEquals(STR2, received.get(KEY2));
        assertEquals(STR3, received.get(KEY3));
    }

    /**
     * This test was the main motivation for the data cache service (storing study instance UIDs with project
     * associations), so it's just explicitly testing that requirement.
     */
    @Test
    public void testStudyProjectAssignment() throws NrgServiceException {
        // You have to use HashMap or similar instead of just Map since the cached object must be synchronized.
        final String date = FORMATTER.format(new Date());

        final HashMap<String, String> assoc1 = new HashMap<String, String>() {{
            put("project", "projectId1");
            put("user", "userId1");
            put("created", date);
            put("accessed", date);
        }};
        final HashMap<String, String> assoc2 = new HashMap<String, String>() {{
            put("project", "projectId2");
            put("user", "userId2");
            put("created", date);
            put("accessed", date);
        }};
        final HashMap<String, String> assoc3 = new HashMap<String, String>() {{
            put("project", "projectId3");
            put("user", "userId3");
            put("created", date);
            put("accessed", date);
        }};

        HashMap<String, HashMap<String, String>> spa = new HashMap<String, HashMap<String, String>>();
        spa.put(UID1, assoc1);
        spa.put(UID2, assoc2);
        spa.put(UID3, assoc3);

        _service.put("spa", spa);

        HashMap<String, HashMap<String, String>> received = _service.get("spa");
        assertNotNull(received);
        assertEquals(3, received.size());

        HashMap<String, String> received1 = received.get(UID1);
        HashMap<String, String> received2 = received.get(UID2);
        HashMap<String, String> received3 = received.get(UID3);
        
        assertNotNull(received1);
        assertNotNull(received2);
        assertNotNull(received3);
        
        assertEquals("projectId1", received1.get("project"));
        assertEquals("userId1", received1.get("user"));
        assertEquals(date, received1.get("created"));
        assertEquals(date, received1.get("accessed"));
        
        assertEquals("projectId2", received2.get("project"));
        assertEquals("userId2", received2.get("user"));
        assertEquals(date, received2.get("created"));
        assertEquals(date, received2.get("accessed"));
        
        assertEquals("projectId3", received3.get("project"));
        assertEquals("userId3", received3.get("user"));
        assertEquals(date, received3.get("created"));
        assertEquals(date, received3.get("accessed"));
    }


    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");

    @Inject
    private DataCacheService _service;
}
