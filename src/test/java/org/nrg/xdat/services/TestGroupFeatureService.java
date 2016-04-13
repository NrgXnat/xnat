package org.nrg.xdat.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xdat.configuration.TestGroupFeatureServiceConfig;
import org.nrg.xdat.entities.GroupFeature;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestGroupFeatureServiceConfig.class)
public class TestGroupFeatureService {
    @Test
    public void testServiceInstance() {
        assertNotNull(_service);
    }

    @Test
    public void testCreation() throws NrgServiceException {
    	GroupFeature created = _service.newEntity();
    	created.setGroupId("basicgroup");
    	created.setTag("tag");
    	created.setFeature("basicrole");
    	_service.create(created);
    	
    	GroupFeature retrieved = _service.findGroupFeature("basicgroup", "basicrole");
    	assertNotNull(retrieved);

        assertEquals(created, retrieved);
        
        _service.delete(created);
        retrieved = _service.retrieve(created.getId());
        assertTrue(retrieved == null);
    }
    @Test
    public void testReCreation() throws NrgServiceException {
    	GroupFeature created = _service.newEntity();
    	created.setGroupId("basicgroupx");
    	created.setTag("tagx");
    	created.setFeature("basicrolex");
    	_service.create(created);
    	
    	GroupFeature retrieved = _service.findGroupFeature("basicgroupx", "basicrolex");
    	assertNotNull(retrieved);

        assertEquals(created, retrieved);
        
        _service.delete(created);
        retrieved = _service.retrieve(created.getId());
        assertTrue(retrieved == null);
        
        created = _service.newEntity();
    	created.setGroupId("basicgroupx");
    	created.setTag("tagx");
    	created.setFeature("basicrolex");
    	_service.create(created);
    	

    	retrieved = _service.findGroupFeature("basicgroupx", "basicrolex");
    	assertNotNull(retrieved);

        assertEquals(created, retrieved);
        
        _service.delete(created);
        retrieved = _service.retrieve(created.getId());
        assertTrue(retrieved == null);
    }
    
    @Test(expected=ConstraintViolationException.class)
    public void testUniqueConstraintWithSameTag() throws NrgServiceException {
    	GroupFeature userAuth1 = _service.newEntity();
    	userAuth1.setGroupId("sametaggroup");
    	userAuth1.setTag("tag");
    	userAuth1.setFeature("sametagrole");
        _service.create(userAuth1);
        GroupFeature userAuth2 = _service.newEntity();
        userAuth2.setGroupId("sametaggroup");
        userAuth2.setTag("tag");
        userAuth2.setFeature("sametagrole");
        _service.create(userAuth2);

        _service.delete(userAuth1);
        _service.delete(userAuth2);
    }

    @Test(expected=ConstraintViolationException.class)
    public void testUniqueConstraintWithDifferentTags() throws NrgServiceException {
    	GroupFeature userAuth1 = _service.newEntity();
    	userAuth1.setGroupId("difftaggroup");
    	userAuth1.setTag("tag1");
    	userAuth1.setFeature("difftagrole");
        _service.create(userAuth1);
        GroupFeature userAuth2 = _service.newEntity();
        userAuth2.setGroupId("difftaggroup");
        userAuth2.setTag("tag2");
        userAuth2.setFeature("difftagrole");
        _service.create(userAuth2);
        
        _service.delete(userAuth1);
        _service.delete(userAuth2);
    }

    @Test
    public void testBlockedMod() throws NrgServiceException {
    	GroupFeature userAuth1 = _service.newEntity();
    	userAuth1.setGroupId("blockedfeaturegroup");
    	userAuth1.setFeature("difftagrole");
        _service.create(userAuth1);
        
        assertFalse(userAuth1.isBlocked());
        
        GroupFeature retrieved1 = _service.findGroupFeature("blockedfeaturegroup", "difftagrole");
        assertFalse(retrieved1.isBlocked());
        
        retrieved1.setBlocked(true);
        _service.update(retrieved1);

        
        GroupFeature retrieved2 = _service.findGroupFeature("blockedfeaturegroup", "difftagrole");
        assertTrue(retrieved2.isBlocked());

        retrieved2.setBlocked(false);
        _service.update(retrieved2);

        GroupFeature retrieved3 = _service.findGroupFeature("blockedfeaturegroup", "difftagrole");
        assertFalse(retrieved3.isBlocked());

        _service.delete(retrieved3);
    }

    @Test
    public void testOnByDefaultMod() throws NrgServiceException {
    	GroupFeature userAuth1 = _service.newEntity();
    	userAuth1.setGroupId("blockedfeaturegroup2");
    	userAuth1.setFeature("difftagrole");
        _service.create(userAuth1);
        
        assertFalse(userAuth1.isOnByDefault());
        
        GroupFeature retrieved1 = _service.findGroupFeature("blockedfeaturegroup2", "difftagrole");
        assertFalse(retrieved1.isOnByDefault());
        
        retrieved1.setOnByDefault(true);
        _service.update(retrieved1);

        
        GroupFeature retrieved2 = _service.findGroupFeature("blockedfeaturegroup2", "difftagrole");
        assertTrue(retrieved2.isOnByDefault());

        retrieved2.setOnByDefault(false);
        _service.update(retrieved2);

        GroupFeature retrieved3 = _service.findGroupFeature("blockedfeaturegroup2", "difftagrole");
        assertFalse(retrieved3.isOnByDefault());

        _service.delete(retrieved3);
    }

    @Test
    public void testDeleteByTag() throws NrgServiceException {
    	GroupFeature userAuth1 = _service.newEntity();
    	userAuth1.setGroupId("blockedfeaturegroup3");
    	userAuth1.setFeature("difftagrole");
    	userAuth1.setTag("tag03");
        _service.create(userAuth1);
        
    	GroupFeature userAuth2 = _service.newEntity();
    	userAuth1.setGroupId("blockedfeaturegroup4");
    	userAuth1.setFeature("difftagrole");
    	userAuth1.setTag("tag04");
        _service.create(userAuth1);
        
        List<GroupFeature> features=_service.findFeaturesForTag("tag03");
        List<GroupFeature> features2=_service.findFeaturesForTag("tag04");
        
        assertEquals(1,features.size());
        assertEquals(1,features2.size());
        
        _service.deleteByTag("tag03");
        
        features=_service.findFeaturesForTag("tag03");
        features2=_service.findFeaturesForTag("tag04");
        
        assertEquals(0,features.size());
        assertEquals(1,features2.size());
        

        _service.deleteByTag("tag04");
        
        features2=_service.findFeaturesForTag("tag04");
        assertEquals(0,features2.size() );
    }

    @Inject
    private GroupFeatureService _service;
}
