/*
 * core: org.nrg.xdat.services.TestFeatureDefinitionService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xdat.configuration.TestFeatureDefinitionServiceConfig;
import org.nrg.xdat.entities.FeatureDefinition;
import org.nrg.xdat.entities.GroupFeature;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestFeatureDefinitionServiceConfig.class)
public class TestFeatureDefinitionService {
    @Test
    public void testServiceInstance() {
        assertNotNull(_service);
    }

    @Test
    public void testCreation() throws NrgServiceException {
    	FeatureDefinition created = _service.newEntity();
    	created.setKey("key1");
    	created.setName("name1");
    	_service.create(created);
    	
    	FeatureDefinition retrieved = _service.findFeatureByKey("key1");
    	assertNotNull(retrieved);

        assertEquals(created, retrieved);
        
        _service.delete(created);
        retrieved = _service.retrieve(created.getId());
        assertTrue(retrieved == null);
    }

    @Test
    public void testReCreation() throws NrgServiceException {
    	FeatureDefinition created = _service.newEntity();
    	created.setKey("key2");
    	created.setName("name2");
    	_service.create(created);
    	
    	FeatureDefinition retrieved = _service.findFeatureByKey("key2");
    	assertNotNull(retrieved);

        assertEquals(created, retrieved);
        
        _service.delete(created);
        retrieved = _service.retrieve(created.getId());
        assertTrue(retrieved == null);
        
        created = _service.newEntity();
    	created.setKey("key2");
    	created.setName("name2");
    	_service.create(created);
    	
    	retrieved = _service.findFeatureByKey("key2");
    	assertNotNull(retrieved);

        assertEquals(created, retrieved);
        
        _service.delete(created);
        retrieved = _service.retrieve(created.getId());
        assertTrue(retrieved == null);
    }

    @Inject
    private FeatureDefinitionService _service;
}
