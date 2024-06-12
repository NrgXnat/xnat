/*
 * framework: org.nrg.framework.services.TestMarshallerCacheService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.test.models.containers.MarshalableList;
import org.nrg.framework.test.models.entities.MarshalableThingy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMarshallerCacheServiceConfiguration.class)
public class TestMarshallerCacheService {
    @Test
    public void testMarshalToString() throws NrgServiceException {
        MarshalableThingy thingy = new MarshalableThingy();
        thingy.setName("Herman Munster");
        thingy.setAddress("1313 Mockingbird Lane");
        thingy.setAge(42);
        thingy.setNotImportant("xxxxxxxx");
        String xml = _service.marshal(thingy);
        assertNotNull(xml);
        assertTrue(xml.contains("<marshalableThingy>"));
        assertTrue(xml.contains("    <address>1313 Mockingbird Lane</address>"));
        assertTrue(xml.contains("    <age>42</age>"));
        assertTrue(xml.contains("    <name>Herman Munster</name>"));
        assertTrue(xml.contains("</marshalableThingy>"));
    }
    
    @Test
    public void testMarshalToDocument() {
        MarshalableThingy thingy = new MarshalableThingy();
        thingy.setName("Herman Munster");
        thingy.setAddress("1313 Mockingbird Lane");
        thingy.setAge(42);
        thingy.setNotImportant("xxxxxxxx");
        Document document = _service.marshalToDocument(thingy);
        assertNotNull(document);
        NodeList elements = document.getElementsByTagName("address");
        assertEquals(1, elements.getLength());
        Node address = elements.item(0);
        assertEquals("address", address.getNodeName());
        assertEquals("1313 Mockingbird Lane", address.getTextContent());
        elements = document.getElementsByTagName("notImportant");
        assertEquals(0, elements.getLength());
    }
    
    @Test
    public void testMarshalListToString() throws NrgServiceException {
        MarshalableThingy thingy1 = new MarshalableThingy();
        thingy1.setName("Herman Munster");
        thingy1.setAddress("1313 Mockingbird Lane");
        thingy1.setAge(42);
        thingy1.setNotImportant("xxxxxxxx");
        MarshalableThingy thingy2 = new MarshalableThingy();
        thingy2.setName("Lily Munster");
        thingy2.setAddress("1313 Mockingbird Lane");
        thingy2.setAge(35);
        thingy2.setNotImportant("yyyy");
        MarshalableThingy thingy3 = new MarshalableThingy();
        thingy3.setName("Grandpa Munster");
        thingy3.setAddress("1313 Mockingbird Lane");
        thingy3.setAge(644);
        thingy3.setNotImportant("zzzz");
        MarshalableList list = new MarshalableList();
        list.add(thingy1);
        list.add(thingy2);
        list.add(thingy3);
        String xml = _service.marshal(list);
        assertNotNull(xml);
        // TODO: This doesn't work, even when the generic list is in the same package as MarshalableThingy. It fails with the exception:
        // [javax.xml.bind.JAXBException: class org.nrg.framework.test.models.MarshalableThingy nor any of its super class is known to this context.]
        // Figure this out!
        // MarshalableGenericList<MarshalableThingy> genericList = new MarshalableGenericList<MarshalableThingy>();
        // genericList.add(thingy1);
        // genericList.add(thingy2);
        // genericList.add(thingy3);
    }
    
    @Inject
    private MarshallerCacheService _service;
}
