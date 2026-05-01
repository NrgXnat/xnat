/*
 * core: org.nrg.xdat.services.TestGroupFeatureService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xdat.configuration.TestGroupFeatureServiceConfig;
import org.nrg.xdat.entities.GroupFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestGroupFeatureServiceConfig.class)
@Slf4j
public class TestGroupFeatureService {
    private static final String              BASIC_GROUP             = "basicGroup";
    private static final String              TAG                     = "tag";
    private static final String              BASIC_ROLE              = "basicRole";
    private static final String              BASIC_GROUP_X           = "basicGroupX";
    private static final String              TAG_X                   = "tagX";
    private static final String              BASIC_ROLE_X            = "basicRoleX";
    private static final String              SAME_TAG_GROUP          = "sameTagGroup";
    private static final String              SAME_TAG_ROLE           = "sameTagRole";
    private static final String              DIFF_TAG_GROUP          = "diffTagGroup";
    private static final String              TAG_1                   = "tag1";
    private static final String              DIFF_TAG_ROLE           = "diffTagRole";
    private static final String              TAG_2                   = "tag2";
    private static final String              BLOCKED_FEATURE_GROUP   = "blockedFeatureGroup";
    private static final String              BLOCKED_FEATURE_GROUP_2 = "blockedFeatureGroup2";
    private static final String              BLOCKED_FEATURE_GROUP_3 = "blockedFeatureGroup3";
    private static final String              TAG_03                  = "tag03";
    private static final String              BLOCKED_FEATURE_GROUP_4 = "blockedFeatureGroup4";
    private static final String              TAG_04                  = "tag04";

    private             GroupFeatureService _service;

    @Autowired
    public void setService(final GroupFeatureService service) {
        _service = service;
    }

    @Test
    public void testServiceInstance() {
        assertNotNull(_service);
    }

    @Test
    public void testCreation() {
        GroupFeature created = _service.newEntity();
        created.setGroupId(BASIC_GROUP);
        created.setTag(TAG);
        created.setFeature(BASIC_ROLE);
        _service.create(created);

        GroupFeature retrieved = _service.findGroupFeature(BASIC_GROUP, BASIC_ROLE);
        assertNotNull(retrieved);

        assertEquals(created, retrieved);

        _service.delete(created);
        retrieved = _service.retrieve(created.getId());
        assertNull(retrieved);
    }

    @Test
    public void testReCreation() {
        GroupFeature created = _service.newEntity();
        created.setGroupId(BASIC_GROUP_X);
        created.setTag(TAG_X);
        created.setFeature(BASIC_ROLE_X);
        _service.create(created);

        GroupFeature retrieved = _service.findGroupFeature(BASIC_GROUP_X, BASIC_ROLE_X);
        assertNotNull(retrieved);

        assertEquals(created, retrieved);

        _service.delete(created);
        retrieved = _service.retrieve(created.getId());
        assertNull(retrieved);

        created = _service.newEntity();
        created.setGroupId(BASIC_GROUP_X);
        created.setTag(TAG_X);
        created.setFeature(BASIC_ROLE_X);
        _service.create(created);


        retrieved = _service.findGroupFeature(BASIC_GROUP_X, BASIC_ROLE_X);
        assertNotNull(retrieved);

        assertEquals(created, retrieved);

        _service.delete(created);
        retrieved = _service.retrieve(created.getId());
        assertNull(retrieved);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testUniqueConstraintWithSameTag() {
        GroupFeature userAuth1 = _service.newEntity();
        userAuth1.setGroupId(SAME_TAG_GROUP);
        userAuth1.setTag(TAG);
        userAuth1.setFeature(SAME_TAG_ROLE);
        _service.create(userAuth1);
        GroupFeature userAuth2 = _service.newEntity();
        userAuth2.setGroupId(SAME_TAG_GROUP);
        userAuth2.setTag(TAG);
        userAuth2.setFeature(SAME_TAG_ROLE);
        _service.create(userAuth2);

        _service.delete(userAuth1);
        _service.delete(userAuth2);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testUniqueConstraintWithDifferentTags() {
        GroupFeature userAuth1 = _service.newEntity();
        userAuth1.setGroupId(DIFF_TAG_GROUP);
        userAuth1.setTag(TAG_1);
        userAuth1.setFeature(DIFF_TAG_ROLE);
        _service.create(userAuth1);
        GroupFeature userAuth2 = _service.newEntity();
        userAuth2.setGroupId(DIFF_TAG_GROUP);
        userAuth2.setTag(TAG_2);
        userAuth2.setFeature(DIFF_TAG_ROLE);
        _service.create(userAuth2);

        _service.delete(userAuth1);
        _service.delete(userAuth2);
    }

    @Test
    public void testBlockedMod() {
        GroupFeature userAuth1 = _service.newEntity();
        userAuth1.setGroupId(BLOCKED_FEATURE_GROUP);
        userAuth1.setFeature(DIFF_TAG_ROLE);
        _service.create(userAuth1);

        assertFalse(userAuth1.isBlocked());

        GroupFeature retrieved1 = _service.findGroupFeature(BLOCKED_FEATURE_GROUP, DIFF_TAG_ROLE);
        assertFalse(retrieved1.isBlocked());

        retrieved1.setBlocked(true);
        _service.update(retrieved1);


        GroupFeature retrieved2 = _service.findGroupFeature(BLOCKED_FEATURE_GROUP, DIFF_TAG_ROLE);
        assertTrue(retrieved2.isBlocked());

        retrieved2.setBlocked(false);
        _service.update(retrieved2);

        GroupFeature retrieved3 = _service.findGroupFeature(BLOCKED_FEATURE_GROUP, DIFF_TAG_ROLE);
        assertFalse(retrieved3.isBlocked());

        _service.delete(retrieved3);
    }

    @Test
    public void testOnByDefaultMod() {
        GroupFeature userAuth1 = _service.newEntity();
        userAuth1.setGroupId(BLOCKED_FEATURE_GROUP_2);
        userAuth1.setFeature(DIFF_TAG_ROLE);
        _service.create(userAuth1);

        assertFalse(userAuth1.isOnByDefault());

        GroupFeature retrieved1 = _service.findGroupFeature(BLOCKED_FEATURE_GROUP_2, DIFF_TAG_ROLE);
        assertFalse(retrieved1.isOnByDefault());

        retrieved1.setOnByDefault(true);
        _service.update(retrieved1);


        GroupFeature retrieved2 = _service.findGroupFeature(BLOCKED_FEATURE_GROUP_2, DIFF_TAG_ROLE);
        assertTrue(retrieved2.isOnByDefault());

        retrieved2.setOnByDefault(false);
        _service.update(retrieved2);

        GroupFeature retrieved3 = _service.findGroupFeature(BLOCKED_FEATURE_GROUP_2, DIFF_TAG_ROLE);
        assertFalse(retrieved3.isOnByDefault());

        _service.delete(retrieved3);
    }

    @Test
    public void testDeleteByTag() {
        GroupFeature userAuth1 = _service.newEntity();
        userAuth1.setGroupId(BLOCKED_FEATURE_GROUP_3);
        userAuth1.setFeature(DIFF_TAG_ROLE);
        userAuth1.setTag(TAG_03);
        _service.create(userAuth1);

        GroupFeature userAuth2 = _service.newEntity();
        userAuth2.setGroupId(BLOCKED_FEATURE_GROUP_4);
        userAuth2.setFeature(DIFF_TAG_ROLE);
        userAuth2.setTag(TAG_04);
        _service.create(userAuth2);

        List<GroupFeature> features  = _service.findFeaturesForTag(TAG_03);
        List<GroupFeature> features2 = _service.findFeaturesForTag(TAG_04);
        
        assertEquals(1, features.size());
        assertEquals(1, features2.size());

        _service.deleteByTag(TAG_03);

        features  = _service.findFeaturesForTag(TAG_03);
        features2 = _service.findFeaturesForTag(TAG_04);

        assertEquals(0, features.size());
        assertEquals(1, features2.size());


        _service.deleteByTag(TAG_04);

        features2 = _service.findFeaturesForTag(TAG_04);
        assertEquals(0, features2.size());
    }
}
