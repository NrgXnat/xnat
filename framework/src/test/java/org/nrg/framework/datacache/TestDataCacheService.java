/*
 * framework: org.nrg.framework.datacache.TestDataCacheService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.datacache;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.framework.configuration.FrameworkConfig;
import org.nrg.framework.test.OrmTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OrmTestConfiguration.class, FrameworkConfig.class})
public class TestDataCacheService {
    private static final String                  KEY1      = "key1";
    private static final String                  KEY2      = "key2";
    private static final String                  KEY3      = "key3";
    private static final String                  STR1      = "string1";
    private static final String                  STR2      = "string2";
    private static final String                  STR3      = "string3";
    private static final String                  MAP_KEY   = "map";
    private static final HashMap<String, String> MAP_VAL   = new HashMap<>(ImmutableMap.<String, String>builder()
                                                                                       .put(KEY1, STR1)
                                                                                       .put(KEY2, STR2)
                                                                                       .put(KEY3, STR3)
                                                                                       .build());
    private static final String                  UID1      = "1.2.3.4.5.6.7";
    private static final String                  UID2      = "2.3.4.5.6.7.8";
    private static final String                  UID3      = "3.4.5.6.7.8.9";
    private static final String                  PROJECT   = "project";
    private static final String                  USER      = "user";
    private static final String                  CREATED   = "created";
    private static final String                  ACCESSED  = "accessed";
    private static final SimpleDateFormat        FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");

    private final DataCacheService _service;

    @Autowired
    public TestDataCacheService(final DataCacheService service) {
        _service = service;
    }

    @Test
    public void testSimpleItem() {
        _service.put(KEY1, STR1);
        _service.put(KEY2, STR2);
        _service.put(KEY3, STR3);
        String string1 = _service.get(KEY1);
        String string2 = _service.get(KEY2);
        String string3 = _service.get(KEY3);
        assertThat(string1).isNotNull().isNotEmpty().isEqualTo(STR1);
        assertThat(string2).isNotNull().isNotEmpty().isEqualTo(STR2);
        assertThat(string3).isNotNull().isNotEmpty().isEqualTo(STR3);
    }

    @Test
    public void testMapStore() {
        _service.put(MAP_KEY, MAP_VAL);
        HashMap<String, String> received = _service.get(MAP_KEY);
        assertThat(received.get(KEY1)).isNotNull().isNotEmpty().isEqualTo(STR1);
        assertThat(received.get(KEY2)).isNotNull().isNotEmpty().isEqualTo(STR2);
        assertThat(received.get(KEY3)).isNotNull().isNotEmpty().isEqualTo(STR3);
    }

    /**
     * This test was the main motivation for the data cache service (storing study instance UIDs with project
     * associations), so it's just explicitly testing that requirement.
     */
    @Test
    public void testStudyProjectAssignment() {
        // You have to use HashMap or similar instead of just Map since the cached object must be synchronized.
        final String date = FORMATTER.format(new Date());

        final HashMap<String, String>            assoc1 = new HashMap<>(ImmutableMap.of(PROJECT, "projectId1", USER, "userId1", CREATED, date, ACCESSED, date));
        final HashMap<String, String>            assoc2 = new HashMap<>(ImmutableMap.of(PROJECT, "projectId2", USER, "userId2", CREATED, date, ACCESSED, date));
        final HashMap<String, String>            assoc3 = new HashMap<>(ImmutableMap.of(PROJECT, "projectId3", USER, "userId3", CREATED, date, ACCESSED, date));
        HashMap<String, HashMap<String, String>> spa    = new HashMap<>(ImmutableMap.of(UID1, assoc1, UID2, assoc2, UID3, assoc3));

        _service.put("spa", spa);

        HashMap<String, HashMap<String, String>> received = _service.get("spa");
        assertThat(received).isNotNull().isNotEmpty().hasSize(3);

        HashMap<String, String> received1 = received.get(UID1);
        HashMap<String, String> received2 = received.get(UID2);
        HashMap<String, String> received3 = received.get(UID3);
        assertThat(received1).isNotNull()
                             .isNotEmpty()
                             .containsOnly(entry(PROJECT, "projectId1"),
                                           entry(USER, "userId1"),
                                           entry(CREATED, date),
                                           entry(ACCESSED, date));
        assertThat(received2).isNotNull()
                             .isNotEmpty()
                             .containsOnly(entry(PROJECT, "projectId2"),
                                           entry(USER, "userId2"),
                                           entry(CREATED, date),
                                           entry(ACCESSED, date));
        assertThat(received3).isNotNull()
                             .isNotEmpty()
                             .containsOnly(entry(PROJECT, "projectId3"),
                                           entry(USER, "userId3"),
                                           entry(CREATED, date),
                                           entry(ACCESSED, date));
    }
}
