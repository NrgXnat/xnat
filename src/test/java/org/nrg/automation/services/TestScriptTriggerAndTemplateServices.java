/*
 * org.nrg.automation.services.TestScriptTriggerAndTemplateServices
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.automation.configuration.AutomationTestsConfiguration;
import org.nrg.automation.entities.Script;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.entities.ScriptTriggerTemplate;
import org.nrg.framework.constants.Scope;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

import static org.junit.Assert.*;

/**
 * TestScriptTriggerAndTemplateServices class.
 *
 * @author Rick Herrick
 */
@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AutomationTestsConfiguration.class)
@Rollback
@Transactional
public class TestScriptTriggerAndTemplateServices {

    public static final String EVENT_ID_1 = "Something happened!";
    public static final String EVENT_ID_2 = "Something else happened!";
    public static final String EVENT_CLASS = "org.nrg.xft.event.entities.WorkflowStatusEvent";
    public static final String STATUS_COMPLETE = "Complete";
    public static final Map<String,String> EVENT_FILTER = new HashMap<String, String>() {{
        put("status", STATUS_COMPLETE);
    }};

    @Test
    public void testSimpleScript() {
        Script script = _scriptService.newEntity("script1", "script1 label", "This is my first script!", "groovy", "2.3.6", "println \"Hello world!\"");
        List<Script> retrieved = _scriptService.getAll();
        assertNotNull(retrieved);
        assertEquals(1, retrieved.size());
        assertEquals(script, retrieved.get(0));
        assertTrue(_scriptService.hasScript("script1"));
        assertFalse(_scriptService.hasScript("script2"));
    }

    @Test
    public void testSimpleTrigger() {
        ScriptTrigger trigger = _triggerService.newEntity("trigger1", "This is my first trigger!", "script1", "associatedThing1", EVENT_CLASS, EVENT_ID_1);
        List<ScriptTrigger> retrieved = _triggerService.getAll();
        assertNotNull(retrieved);
        assertEquals(1, retrieved.size());
        assertEquals(trigger, retrieved.get(0));
    }

    @Test
    public void testSimpleTemplate() {
        ScriptTrigger[] triggers = { _triggerService.newEntity("trigger1", "Trigger 1", "script1", "associatedThing1", EVENT_CLASS, EVENT_ID_1),
                _triggerService.newEntity("trigger2", "Trigger 2", "script2", "associatedThing2", EVENT_CLASS, EVENT_ID_1),
                _triggerService.newEntity("trigger3", "Trigger 3", "script3", "associatedThing3", EVENT_CLASS, EVENT_ID_1)};
        ScriptTriggerTemplate template = _templateService.newEntity("template1", "Here's a template!", new HashSet<>(Arrays.asList(triggers)), new HashSet<>(Arrays.asList("0", "1", "2")));
        List<ScriptTriggerTemplate> retrieved = _templateService.getAll();
        assertNotNull(retrieved);
        assertEquals(1, retrieved.size());
        assertEquals(template, retrieved.get(0));
        assertEquals(3, retrieved.get(0).getTriggers().size());
        assertEquals(3, retrieved.get(0).getAssociatedEntities().size());
    }

    @Test
    public void testSimpleTemplatesAndEntities() {
        ScriptTrigger trigger0 = _triggerService.newEntity("trigger0", "Trigger 0", "script0", "associatedThing0", EVENT_CLASS, EVENT_ID_1);
        ScriptTrigger trigger1 = _triggerService.newEntity("trigger1", "Trigger 1", "script1", "associatedThing1", EVENT_CLASS, EVENT_ID_1);
        ScriptTrigger trigger2 = _triggerService.newEntity("trigger2", "Trigger 2", "script2", "associatedThing2", EVENT_CLASS, EVENT_ID_1);

        ScriptTriggerTemplate template = _templateService.newEntity("template1", "Here's a template!", new HashSet<>(Arrays.asList(trigger0, trigger1, trigger2)), new HashSet<>(Arrays.asList("0", "1", "2")));

        List<ScriptTriggerTemplate> all = _templateService.getAll();
        assertNotNull(all);
        assertEquals(1, all.size());

        ScriptTriggerTemplate retrieved = _templateService.retrieve(template.getId());

        assertNotNull(retrieved);
        assertEquals(template, retrieved);
        assertEquals(3, retrieved.getTriggers().size());
        assertEquals(3, retrieved.getAssociatedEntities().size());

        ScriptTrigger retrievedTrigger0 = _triggerService.getByTriggerId("trigger0");
        ScriptTrigger retrievedTrigger1 = _triggerService.getByTriggerId("trigger1");
        ScriptTrigger retrievedTrigger2 = _triggerService.getByTriggerId("trigger2");

        assertNotNull(retrievedTrigger0);
        assertEquals("trigger0", retrievedTrigger0.getTriggerId());
        assertNotNull(retrievedTrigger1);
        assertEquals("trigger1", retrievedTrigger1.getTriggerId());
        assertNotNull(retrievedTrigger2);
        assertEquals("trigger2", retrievedTrigger2.getTriggerId());

        List<ScriptTriggerTemplate> templates0 = _templateService.getTemplatesForTrigger(retrievedTrigger0);
        List<ScriptTriggerTemplate> templates1 = _templateService.getTemplatesForTrigger(retrievedTrigger1);
        List<ScriptTriggerTemplate> templates2 = _templateService.getTemplatesForTrigger(retrievedTrigger2);

        assertNotNull(templates0);
        assertEquals(1, templates0.size());
        assertTrue(templates0.contains(template));
        assertNotNull(templates1);
        assertEquals(1, templates1.size());
        assertTrue(templates1.contains(template));
        assertNotNull(templates2);
        assertEquals(1, templates2.size());
        assertTrue(templates2.contains(template));

        List<ScriptTriggerTemplate> entities0 = _templateService.getTemplatesForEntity("0");
        List<ScriptTriggerTemplate> entities1 = _templateService.getTemplatesForEntity("1");
        List<ScriptTriggerTemplate> entities2 = _templateService.getTemplatesForEntity("2");

        assertNotNull(entities0);
        assertEquals(1, entities0.size());
        assertNotNull(entities1);
        assertEquals(1, entities1.size());
        assertNotNull(entities2);
        assertEquals(1, entities2.size());
    }

    @Test
    public void testDoubleTemplatesAndEntities() throws JsonProcessingException {
        ScriptTrigger trigger0 = _triggerService.newEntity("trigger0", "Trigger 0", "script0", "associatedThing0", EVENT_CLASS, EVENT_ID_1);
        ScriptTrigger trigger1 = _triggerService.newEntity("trigger1", "Trigger 1", "script1", "associatedThing1", EVENT_CLASS, EVENT_ID_1);
        ScriptTrigger trigger2 = _triggerService.newEntity("trigger2", "Trigger 2", "script2", "associatedThing2", EVENT_CLASS, EVENT_ID_1);

        ScriptTriggerTemplate template0 = _templateService.newEntity("template0", "Here's a template!", new HashSet<>(Arrays.asList(trigger0, trigger1)), new HashSet<>(Arrays.asList("0", "1")));
        ScriptTriggerTemplate template1 = _templateService.newEntity("template1", "Here's a template!", new HashSet<>(Arrays.asList(trigger1, trigger2)), new HashSet<>(Arrays.asList("1", "2")));

        List<ScriptTriggerTemplate> all = _templateService.getAll();
        assertNotNull(all);
        assertEquals(2, all.size());

        ScriptTriggerTemplate retrieved0 = _templateService.retrieve(template0.getId());
        ScriptTriggerTemplate retrieved1 = _templateService.retrieve(template1.getId());

        assertNotNull(retrieved0);
        assertEquals(template0, retrieved0);
        assertEquals(2, retrieved0.getTriggers().size());
        assertEquals(2, retrieved1.getAssociatedEntities().size());
        assertNotNull(retrieved1);
        assertEquals(template1, retrieved1);
        assertEquals(2, retrieved1.getTriggers().size());
        assertEquals(2, retrieved1.getAssociatedEntities().size());

        ScriptTrigger retrievedTrigger0 = _triggerService.getByTriggerId("trigger0");
        ScriptTrigger retrievedTrigger1 = _triggerService.getByTriggerId("trigger1");
        ScriptTrigger retrievedTrigger2 = _triggerService.getByTriggerId("trigger2");

        assertNotNull(retrievedTrigger0);
        assertEquals(trigger0, retrievedTrigger0);
        assertNotNull(retrievedTrigger1);
        assertEquals(trigger1, retrievedTrigger1);
        assertNotNull(retrievedTrigger2);
        assertEquals(trigger2, retrievedTrigger2);

        List<ScriptTriggerTemplate> templates0 = _templateService.getTemplatesForTrigger(retrievedTrigger0);
        List<ScriptTriggerTemplate> templates1 = _templateService.getTemplatesForTrigger(retrievedTrigger1);
        List<ScriptTriggerTemplate> templates2 = _templateService.getTemplatesForTrigger(retrievedTrigger2);

        assertNotNull(templates0);
        assertEquals(1, templates0.size());
        assertTrue(templates0.contains(template0));
        assertNotNull(templates1);
        assertEquals(2, templates1.size());
        assertTrue(templates1.contains(template0));
        assertTrue(templates1.contains(template1));
        assertNotNull(templates2);
        assertEquals(1, templates2.size());
        assertTrue(templates2.contains(template1));

        List<ScriptTriggerTemplate> entities0 = _templateService.getTemplatesForEntity("0");
        List<ScriptTriggerTemplate> entities1 = _templateService.getTemplatesForEntity("1");
        List<ScriptTriggerTemplate> entities2 = _templateService.getTemplatesForEntity("2");

        assertNotNull(entities0);
        assertEquals(1, entities0.size());
        assertNotNull(entities1);
        assertEquals(2, entities1.size());
        assertNotNull(entities2);
        assertEquals(1, entities2.size());
    }

    /*
    @Test(expected = ConstraintViolationException.class)
    public void testUniqueAssociationAndEventConstraint() {
        _triggerService.newEntity("trigger1", "Trigger 1", "script1", Scope.encode(Scope.Project, "1"), EVENT_CLASS, EVENT_ID_1);
        _triggerService.newEntity("trigger2", "Trigger 2", "script2", Scope.encode(Scope.Project, "1"), EVENT_CLASS, EVENT_ID_1);
        _triggerService.newEntity("trigger3", "Trigger 3", "script3", Scope.encode(Scope.Project, "1"), EVENT_CLASS, EVENT_ID_1);
    }
    */

    @Test
    public void testRelatedTemplatesAndEntities() throws JsonProcessingException {
        ScriptTrigger trigger0 = _triggerService.newEntity("trigger0", "Trigger 0", "script0", Scope.encode(Scope.Project, "0"), EVENT_CLASS, EVENT_ID_1);
        ScriptTrigger trigger1 = _triggerService.newEntity("trigger1", "Trigger 1", "script1", Scope.encode(Scope.Project, "1"), EVENT_CLASS, EVENT_ID_1);
        ScriptTrigger trigger2 = _triggerService.newEntity("trigger2", "Trigger 2", "script2", Scope.encode(Scope.Project, "2"), EVENT_CLASS, EVENT_ID_1);
        ScriptTrigger trigger3 = _triggerService.newEntity("trigger3", "Trigger 3", "script3", Scope.encode(Scope.Project, "3"), EVENT_CLASS, EVENT_ID_1);
        ScriptTrigger trigger4 = _triggerService.newEntity("trigger4", "Trigger 4", "script4", Scope.encode(Scope.Project, "4"), EVENT_CLASS, EVENT_ID_1);
        ScriptTrigger trigger5 = _triggerService.newEntity("trigger5", "Trigger 5", "script5", Scope.encode(Scope.Project, "5"), EVENT_CLASS, EVENT_ID_1);
        ScriptTrigger trigger6 = _triggerService.newEntity("trigger6", "Trigger 6", "script6", Scope.encode(Scope.Project, "6"), EVENT_CLASS, EVENT_ID_1);

        // These are just to make sure that the getByEvent() call truly distinguishes by event.
        ScriptTrigger trigger7 = _triggerService.newEntity("trigger7", "Trigger 7", "script7", Scope.encode(Scope.Project, "0"), EVENT_CLASS, EVENT_ID_2);
        _triggerService.newEntity("trigger8", "Trigger 8", "script8", Scope.encode(Scope.Project, "8"), EVENT_CLASS, EVENT_ID_2);
        _triggerService.newEntity("trigger9", "Trigger 9", "script9", Scope.encode(Scope.Project, "9"), EVENT_CLASS, EVENT_ID_2);

        List<ScriptTrigger> triggers = _triggerService.getByEventAndFilters(EVENT_CLASS, EVENT_ID_1, EVENT_FILTER);
        assertNotNull(triggers);
        assertEquals(7, triggers.size());

        List<ScriptTrigger> compareByEvent = _triggerService.getByEventAndFilters(EVENT_CLASS, EVENT_ID_2, EVENT_FILTER);
        assertNotNull(compareByEvent);
        assertEquals(3, compareByEvent.size());

        List<ScriptTrigger> compareByAssociation = _triggerService.getByScope(Scope.Project, "0");
        assertNotNull(compareByAssociation);
        assertEquals(2, compareByAssociation.size());
        assertTrue(compareByAssociation.contains(trigger0));
        assertTrue(compareByAssociation.contains(trigger7));

        ScriptTrigger getByEventAndAssociation = _triggerService.getByScopeEntityAndEventAndFilters(Scope.Project, "0", EVENT_CLASS, EVENT_ID_1, EVENT_FILTER);
        assertNotNull(getByEventAndAssociation);
        assertEquals("script0", getByEventAndAssociation.getScriptId());
        assertEquals(Scope.encode(Scope.Project, "0"), getByEventAndAssociation.getAssociation());
        assertEquals(EVENT_ID_1, getByEventAndAssociation.getEvent());

        ScriptTriggerTemplate template0 = _templateService.newEntity("template1", "Here's a template!", new HashSet<>(Arrays.asList(trigger0, trigger1, trigger2)), new HashSet<>(Arrays.asList("0", "1", "2")));
        ScriptTriggerTemplate template1 = _templateService.newEntity("template2", "Yet another template!", new HashSet<>(Arrays.asList(trigger2, trigger3, trigger4)), new HashSet<>(Arrays.asList("2", "3", "4")));
        ScriptTriggerTemplate template2 = _templateService.newEntity("template3", "Yet another template!", new HashSet<>(Arrays.asList(trigger4, trigger5, trigger6)), new HashSet<>(Arrays.asList("4", "5", "6")));

        List<ScriptTriggerTemplate> retrieved = _templateService.getAll();
        assertNotNull(retrieved);
        assertEquals(3, retrieved.size());

        ScriptTriggerTemplate retrieved0 = _templateService.retrieve(template0.getId());
        ScriptTriggerTemplate retrieved1 = _templateService.retrieve(template1.getId());
        ScriptTriggerTemplate retrieved2 = _templateService.retrieve(template2.getId());

        assertNotNull(retrieved0);
        assertEquals(template0, retrieved0);
        assertEquals(3, template0.getTriggers().size());
        assertEquals(3, template0.getAssociatedEntities().size());
        assertNotNull(retrieved1);
        assertEquals(template1, retrieved1);
        assertEquals(3, template1.getTriggers().size());
        assertEquals(3, template1.getAssociatedEntities().size());
        assertNotNull(retrieved2);
        assertEquals(template2, retrieved2);
        assertEquals(3, template2.getTriggers().size());
        assertEquals(3, template2.getAssociatedEntities().size());

        ScriptTrigger retrievedTrigger0 = _triggerService.getByTriggerId("trigger0");
        ScriptTrigger retrievedTrigger1 = _triggerService.getByTriggerId("trigger1");
        ScriptTrigger retrievedTrigger2 = _triggerService.getByTriggerId("trigger2");
        ScriptTrigger retrievedTrigger3 = _triggerService.getByTriggerId("trigger3");
        ScriptTrigger retrievedTrigger4 = _triggerService.getByTriggerId("trigger4");
        ScriptTrigger retrievedTrigger5 = _triggerService.getByTriggerId("trigger5");
        ScriptTrigger retrievedTrigger6 = _triggerService.getByTriggerId("trigger6");

        assertNotNull(retrievedTrigger0);
        assertEquals("trigger0", retrievedTrigger0.getTriggerId());
        assertNotNull(retrievedTrigger1);
        assertEquals("trigger1", retrievedTrigger1.getTriggerId());
        assertNotNull(retrievedTrigger2);
        assertEquals("trigger2", retrievedTrigger2.getTriggerId());
        assertNotNull(retrievedTrigger3);
        assertEquals("trigger3", retrievedTrigger3.getTriggerId());
        assertNotNull(retrievedTrigger4);
        assertEquals("trigger4", retrievedTrigger4.getTriggerId());
        assertNotNull(retrievedTrigger5);
        assertEquals("trigger5", retrievedTrigger5.getTriggerId());
        assertNotNull(retrievedTrigger6);
        assertEquals("trigger6", retrievedTrigger6.getTriggerId());

        List<ScriptTriggerTemplate> triggers0 = _templateService.getTemplatesForTrigger(trigger0);
        List<ScriptTriggerTemplate> triggers1 = _templateService.getTemplatesForTrigger(trigger1);
        List<ScriptTriggerTemplate> triggers2 = _templateService.getTemplatesForTrigger(trigger2);
        List<ScriptTriggerTemplate> triggers3 = _templateService.getTemplatesForTrigger(trigger3);
        List<ScriptTriggerTemplate> triggers4 = _templateService.getTemplatesForTrigger(trigger4);
        List<ScriptTriggerTemplate> triggers5 = _templateService.getTemplatesForTrigger(trigger5);
        List<ScriptTriggerTemplate> triggers6 = _templateService.getTemplatesForTrigger(trigger6);

        assertNotNull(triggers0);
        assertEquals(1, triggers0.size());
        assertTrue(triggers0.contains(retrieved0));
        assertNotNull(triggers1);
        assertEquals(1, triggers1.size());
        assertTrue(triggers1.contains(retrieved0));
        assertNotNull(triggers2);
        assertEquals(2, triggers2.size());
        assertTrue(triggers2.contains(retrieved0));
        assertTrue(triggers2.contains(retrieved1));
        assertNotNull(triggers3);
        assertEquals(1, triggers3.size());
        assertTrue(triggers3.contains(retrieved1));
        assertNotNull(triggers4);
        assertEquals(2, triggers4.size());
        assertTrue(triggers4.contains(retrieved1));
        assertTrue(triggers4.contains(retrieved2));
        assertNotNull(triggers5);
        assertEquals(1, triggers5.size());
        assertTrue(triggers5.contains(retrieved2));
        assertNotNull(triggers6);
        assertEquals(1, triggers6.size());
        assertTrue(triggers6.contains(retrieved2));

        List<ScriptTriggerTemplate> entities0 = _templateService.getTemplatesForEntity("0");
        List<ScriptTriggerTemplate> entities1 = _templateService.getTemplatesForEntity("1");
        List<ScriptTriggerTemplate> entities2 = _templateService.getTemplatesForEntity("2");
        List<ScriptTriggerTemplate> entities3 = _templateService.getTemplatesForEntity("3");
        List<ScriptTriggerTemplate> entities4 = _templateService.getTemplatesForEntity("4");
        List<ScriptTriggerTemplate> entities5 = _templateService.getTemplatesForEntity("5");
        List<ScriptTriggerTemplate> entities6 = _templateService.getTemplatesForEntity("6");

        assertNotNull(entities0);
        assertEquals(1, entities0.size());
        assertNotNull(entities1);
        assertEquals(1, entities1.size());
        assertNotNull(entities2);
        assertEquals(2, entities2.size());
        assertNotNull(entities3);
        assertEquals(1, entities3.size());
        assertNotNull(entities4);
        assertEquals(2, entities4.size());
        assertNotNull(entities5);
        assertEquals(1, entities5.size());
        assertNotNull(entities6);
        assertEquals(1, entities6.size());

        ObjectMapper mapper = new ObjectMapper();
        final String template0json = mapper.writeValueAsString(retrieved0);
        final String template1json = mapper.writeValueAsString(retrieved1);
        System.out.println(template0json);
        System.out.println(template1json);
    }

    /*
    @Test
    public void testCascadedEventDeletion() {
        //try {
            ScriptTrigger trigger1 = _triggerService.newEntity("trigger1", "This is my first trigger!", "script1", "associatedThing1", EVENT_CLASS, EVENT_ID_1);
            assertNotNull(trigger1);
            assertEquals(EVENT_ID_1, trigger1.getEvent());

            ScriptTrigger trigger2 = _triggerService.newEntity("trigger2", "This is my second trigger!", "script2", "associatedThing2", EVENT_CLASS, EVENT_ID_1);
            assertNotNull(trigger2);
            assertEquals(EVENT_ID_1, trigger2.getEvent());

            ScriptTrigger trigger3 = _triggerService.newEntity("trigger3", "This is my third trigger!", "script3", "associatedThing3", EVENT_CLASS, EVENT_ID_2);
            assertNotNull(trigger3);
            assertEquals(EVENT_ID_2, trigger3.getEvent());

            List<ScriptTrigger> eventId1Triggers = _triggerService.getByEvent(EVENT_ID_1);
            assertNotNull(eventId1Triggers);
            assertEquals(2, eventId1Triggers.size());

            //_eventService.delete(EVENT_ID_1, true);

            //assertFalse(_eventService.hasEvent(EVENT_ID_1));
            ScriptTrigger nope = _triggerService.getByTriggerId("trigger1");
            assertNull(nope);

            eventId1Triggers = _triggerService.getByEvent(EVENT_ID_1);
            assertNull(eventId1Triggers);

            List<ScriptTrigger> eventId2Triggers = _triggerService.getByEvent(EVENT_ID_2);
            assertNotNull(eventId2Triggers);
            assertEquals(1, eventId2Triggers.size());
        //} catch (EventReferencedException e) {
        //    fail("An EventReferencedException occurred even though the cascade flag was set to true. ");
        //}
    }

    @Test(expected = EventReferencedException.class)
    public void testUncascadedEventDeletion() throws EventReferencedException {
        ScriptTrigger trigger = _triggerService.newEntity("trigger1", "This is my first trigger!", "script1", "associatedThing1", EVENT_CLASS, EVENT_ID_1);
        assertNotNull(trigger);
        assertEquals(EVENT_ID_1, trigger.getEvent());

        List<ScriptTrigger> retrieved = _triggerService.getByEvent(EVENT_ID_1);
        assertNotNull(retrieved);
        assertEquals(1, retrieved.size());

        //_eventService.delete(EVENT_ID_1, false);
    }
    */

    //@Inject
    //private EventService _eventService;

    @Inject
    private ScriptService _scriptService;

    @Inject
    private ScriptTriggerService _triggerService;

    @Inject
    private ScriptTriggerTemplateService _templateService;
}
