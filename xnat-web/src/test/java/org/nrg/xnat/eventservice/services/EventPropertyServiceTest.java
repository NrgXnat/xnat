package org.nrg.xnat.eventservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xdat.model.XnatSubjectassessordataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.config.EventServiceTestConfig;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.EventPropertyNode;
import org.nrg.xnat.eventservice.model.JsonPathFilterNode;
import org.nrg.xnat.eventservice.model.xnat.Scan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = EventServiceTestConfig.class)
public class EventPropertyServiceTest {

    @Autowired private EventServiceComponentManager componentManager;
    @Autowired private EventPropertyService eventPropertyService;
    @Autowired private ObjectMapper objectMapper;

    private UserI mockUser;
    private final String FAKE_USER = "mockUser";
    private final Integer FAKE_USER_ID = 1234;

    private Scan mrScan1 = new Scan();
    private Scan mrScan2 = new Scan();
    private Scan ctScan1 = new Scan();


    @Before
    public void setUp() throws Exception {
        mrScan1.setId("1111");
        mrScan1.setLabel("TestLabel");
        mrScan1.setXsiType("xnat:Scan");
        mrScan1.setNote("Test note.");
        mrScan1.setModality("MR");
        mrScan1.setIntegerId(1111);
        mrScan1.setProjectId("PROJECTID-1");
        mrScan1.setSeriesDescription("This is the description of a series which is this one.");

        mrScan2.setId("2222");
        mrScan2.setLabel("TestLabel");
        mrScan2.setXsiType("xnat:Scan");
        mrScan2.setNote("Test note.");
        mrScan2.setModality("MR");
        mrScan2.setIntegerId(2222);
        mrScan2.setProjectId("PROJECTID-1");
        mrScan2.setSeriesDescription("This is the description of a series which is this one.");

        ctScan1.setId("3333");
        ctScan1.setLabel("TestLabel");
        ctScan1.setXsiType("xnat:Scan");
        ctScan1.setNote("Test note.");
        ctScan1.setModality("CT");
        ctScan1.setIntegerId(3333);
        ctScan1.setProjectId("PROJECTID-1");
        ctScan1.setSeriesDescription("This is the description of a series which is this one.");

        // Mock the userI
        mockUser = Mockito.mock(UserI.class);
        when(mockUser.getLogin()).thenReturn(FAKE_USER);
        when(mockUser.getID()).thenReturn(FAKE_USER_ID);

    }


    @Test
    public void filterSerializedModelObjects() throws Exception {
        String mrFilter = "$[?(@.modality == \"MR\")]";
        String ctFilter = "$[?(@.modality == \"CT\")]";
        String mrCtProj1Filter = "$[?(@.project-id == \"PROJECTID-1\" && (@.modality == \"MR\" || @.modality == \"CT\"))]";
        String mrCtProj2Filter = "$[?(@.project-id == \"PROJECTID-2\" && (@.modality == \"MR\" || @.modality == \"CT\"))]";
        String proj2Filter = "$[?(@.project-id == \"PROJECTID-2\" && (@.modality == \"MR\" || @.modality == \"CT\"))]";

        assertThat(objectMapper.canSerialize(Scan.class), is(true));
        String jsonMrScan1 = objectMapper.writeValueAsString(mrScan1);
        String jsonMrScan2 = objectMapper.writeValueAsString(mrScan2);
        String jsonCtScan1 = objectMapper.writeValueAsString(ctScan1);


        List<String> match = JsonPath.parse(jsonMrScan1).read(mrFilter);
        assertThat("JsonPath result should not be null", match, notNullValue());
        assertThat("JsonPath match result should not be empty", match, is(not(empty())));

        List<String> mismatch = JsonPath.parse(jsonCtScan1).read(mrFilter);
        assertThat("JsonPath result should not be null", mismatch, notNullValue());
        assertThat("JsonPath mismatch result should be empty" + mismatch, mismatch, is(empty()));

        match = JsonPath.parse(jsonCtScan1).read(mrCtProj1Filter);
        assertThat("JsonPath result should not be null", match, notNullValue());
        assertThat("JsonPath match result should not be empty", match, is(not(empty())));

        mismatch = JsonPath.parse(jsonMrScan1).read(mrCtProj2Filter);
        assertThat("JsonPath result should not be null", mismatch, notNullValue());
        assertThat("JsonPath mismatch result should be empty: " + mismatch, mismatch, is(empty()));


    }


    @Test
    public void matchFilter() {
    }

    @Test
    public void serializePayloadObject() {
    }

    @Test
    public void generateEventFilterNodes() throws Exception{
        Class testClass = XnatImagesessiondataI.class;
        Map<String, JsonPathFilterNode> nodeMap = eventPropertyService.generateEventFilterNodes(testClass);
        assertThat("EventFilterNodes for " + testClass.getSimpleName() + " should not be null.", nodeMap, notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should exist", nodeMap.get("xsiType"), notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should have a sample value", nodeMap.get("xsiType").sampleValue(), notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should have a sample value", nodeMap.get("xsiType").sampleValue(), is(not("")));


        testClass = XnatProjectdata.class;
        nodeMap = eventPropertyService.generateEventFilterNodes(testClass);
        assertThat("EventFilterNodes for " + testClass.getSimpleName() + " should not be null.", nodeMap, notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should exist", nodeMap.get("xsiType"), notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should have a sample value", nodeMap.get("xsiType").sampleValue(), notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should have a sample value", nodeMap.get("xsiType").sampleValue(), is(not("")));

        testClass = XnatResourcecatalog.class;
        nodeMap = eventPropertyService.generateEventFilterNodes(testClass);
        assertThat("EventFilterNodes for " + testClass.getSimpleName() + " should not be null.", nodeMap, notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should exist", nodeMap.get("xsiType"), notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should have a sample value", nodeMap.get("xsiType").sampleValue(), notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should have a sample value", nodeMap.get("xsiType").sampleValue(), is(not("")));

        testClass = XnatImagescandataI.class;
        nodeMap = eventPropertyService.generateEventFilterNodes(testClass);
        assertThat("EventFilterNodes for " + testClass.getSimpleName() + " should not be null.", nodeMap, notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should exist", nodeMap.get("xsiType"), notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should have a sample value", nodeMap.get("xsiType").sampleValue(), notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should have a sample value", nodeMap.get("xsiType").sampleValue(), is(not("")));

        testClass = XnatImagesessiondataI.class;
        nodeMap = eventPropertyService.generateEventFilterNodes(testClass);
        assertThat("EventFilterNodes for " + testClass.getSimpleName() + " should not be null.", nodeMap, notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should exist", nodeMap.get("xsiType"), notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should have a sample value", nodeMap.get("xsiType").sampleValue(), notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should have a sample value", nodeMap.get("xsiType").sampleValue(), is(not("")));

        testClass = XnatSubjectdataI.class;
        nodeMap = eventPropertyService.generateEventFilterNodes(testClass);
        assertThat("EventFilterNodes for " + testClass.getSimpleName() + " should not be null.", nodeMap, notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should exist", nodeMap.get("xsiType"), notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should have a sample value", nodeMap.get("xsiType").sampleValue(), notNullValue());
        assertThat("xsiType filter node for " + testClass.getSimpleName() + "should have a sample value", nodeMap.get("xsiType").sampleValue(), is(not("")));

        testClass = Date.class;
        nodeMap = eventPropertyService.generateEventFilterNodes(testClass);
        assertThat("EventFilterNodes for " + testClass.getSimpleName() + " should not be null.", nodeMap, notNullValue());
        assertThat("EventFilterNodes expected to be empty for " + testClass.getSimpleName(), nodeMap.size(), is(0));

    }

    @Test
    public void generateEventFilterNodesByEvent() throws Exception {
        for(EventServiceEvent event : componentManager.getInstalledEvents().stream()
                                                // Filter out Scheduled Events for now.  TODO: XNAT-7021
                                                .filter(event -> !"Scheduled Event".equals(event.getDisplayName()))
                                                .collect(Collectors.toList())) {
            Map<String, JsonPathFilterNode> nodeMap = eventPropertyService.generateEventFilterNodes(event);
            assertThat("EventFilterNodes for " + event.getObjectClass().getSimpleName() + " should not be null.", nodeMap, notNullValue());

            if(     event.getObjectClass().isAssignableFrom(XnatProjectdataI.class) ||
                    event.getObjectClass().isAssignableFrom(XnatSubjectdataI.class) ||
                    event.getObjectClass().isAssignableFrom(XnatImagesessiondataI.class) ||
                    event.getObjectClass().isAssignableFrom(XnatImagescandataI.class) ||
                    event.getObjectClass().isAssignableFrom(XnatImageassessordataI.class) ||
                    event.getObjectClass().isAssignableFrom(XnatSubjectassessordataI.class) ||
                    event.getObjectClass().isAssignableFrom(XnatResourcecatalogI.class)){
                assertThat("Event Filter Node map for XnatModelObject" + event.getObjectClass().getSimpleName() + " should not be empty", nodeMap.size(), greaterThan(0));
            }
        }
    }


    @Test
    public void generateJsonPathFilter() throws Exception {

        String scanJson = objectMapper.writeValueAsString(Scan.populateSample());

        Map<String, JsonPathFilterNode> nodeMap = eventPropertyService.generateEventFilterNodes(XnatImagescandataI.class);
        // ** set values to sample values ** //
        for (String nodeKey : nodeMap.keySet()){
            String sampleValue = nodeMap.get(nodeKey).sampleValue();
            JsonPathFilterNode node = nodeMap.get(nodeKey).toBuilder().value(sampleValue).build();
            nodeMap.put(nodeKey, node);
            //if(nodeKey.equalsIgnoreCase("frames"))
            //break;
        }


        String jsonPathFilter = eventPropertyService.generateJsonPathFilter(nodeMap);

        assertThat(jsonPathFilter, notNullValue());
    }

    @Test
    public void testGenerateEventPropertyKeys() throws Exception {
        List<EventServiceEvent> installedEvents = componentManager.getInstalledEvents();
        for(EventServiceEvent event : installedEvents) {
            List<EventPropertyNode> eventPropertyNodes = eventPropertyService.generateEventPropertyKeys(event);
            assertThat("EventPropertyNodes for " + event.getClass() + " is null.", eventPropertyNodes, notNullValue());
            assertThat("EventPropertyNodes for " + event.getClass() + " is empty.", eventPropertyNodes, not(empty()));
        }
    }

}