/*
 * framework: org.nrg.framework.services.TestMarshallerCacheService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services;

import org.assertj.core.api.Condition;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.test.models.containers.MarshalableList;
import org.nrg.framework.test.models.entities.MarshalableThingy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestMarshallerCacheServiceConfiguration.class)
public class TestMarshallerCacheService {
    private final MarshallerCacheService _service;

    @Autowired
    public TestMarshallerCacheService(final MarshallerCacheService service) {
        _service = service;
    }

    @Test
    public void testMarshalToString() throws NrgServiceException {
        MarshalableThingy thingy = new MarshalableThingy();
        thingy.setName("Herman Munster");
        thingy.setAddress("1313 Mockingbird Lane");
        thingy.setAge(42);
        thingy.setNotImportant("xxxxxxxx");
        String xml = _service.marshal(thingy);
        assertThat(xml).isNotNull()
                       .isNotEmpty()
                       .contains("<marshalableThingy>")
                       .contains("    <address>1313 Mockingbird Lane</address>")
                       .contains("    <age>42</age>")
                       .contains("    <name>Herman Munster</name>")
                       .contains("</marshalableThingy>");
    }

    @Test
    public void testMarshalToDocument() {
        MarshalableThingy thingy = new MarshalableThingy();
        thingy.setName("Herman Munster");
        thingy.setAddress("1313 Mockingbird Lane");
        thingy.setAge(42);
        thingy.setNotImportant("xxxxxxxx");

        final Document document = _service.marshalToDocument(thingy);
        assertThat(document).isNotNull();

        final NodeList elements1 = document.getElementsByTagName("address");
        assertThat(elements1).isNotNull()
                             .satisfies(new Condition<>(list -> list.getLength() == 1, "The list should have exactly one element."));

        final Node address = elements1.item(0);
        assertThat(address).isNotNull()
                           .satisfies(new Condition<>(node -> node.getNodeName().equals("address"), "The node should be named 'address'."))
                           .satisfies(new Condition<>(node -> node.getTextContent().equals("1313 Mockingbird Lane"), "The node value should be '1313 Mockingbird Lane'."));

        final NodeList elements2 = document.getElementsByTagName("notImportant");
        assertThat(elements2).isNotNull()
                             .satisfies(new Condition<>(list -> list.getLength() == 0, "The list should be empty."));
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
        assertThat(xml).isNotNull().isNotEmpty();
        // TODO: This doesn't work, even when the generic list is in the same package as MarshalableThingy. It fails with the exception:
        // [javax.xml.bind.JAXBException: class org.nrg.framework.test.models.MarshalableThingy nor any of its super class is known to this context.]
        // Figure this out!
        // MarshalableGenericList<MarshalableThingy> genericList = new MarshalableGenericList<MarshalableThingy>();
        // genericList.add(thingy1);
        // genericList.add(thingy2);
        // genericList.add(thingy3);
    }
}
