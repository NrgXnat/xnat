/**
 * TestScriptTriggerAndTemplateServices
 * (C) 2014 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 9/19/2014 by Rick Herrick
 */
package org.nrg.automation.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.automation.entities.Script;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.entities.ScriptTriggerTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * TestScriptTriggerAndTemplateServices class.
 *
 * @author Rick Herrick
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@TransactionConfiguration(defaultRollback = true)
@Transactional
public class TestScriptTriggerAndTemplateServices {

    @Test
    public void testSimpleScript() {
        Script script = _scriptService.newEntity("script1", "This is my first script!", "groovy", "2.3.6", "println \"Hello world!\"");
        List<Script> retrieved = _scriptService.getAll();
        assertNotNull(retrieved);
        assertEquals(1, retrieved.size());
        assertEquals(script, retrieved.get(0));
        assertTrue(_scriptService.hasScript("script1"));
        assertFalse(_scriptService.hasScript("script2"));
    }

    @Test
    public void testSimpleTrigger() {
        ScriptTrigger trigger = _triggerService.newEntity("trigger1", "This is my first trigger!", "script1", "associatedThing1", "Something happened!");
        List<ScriptTrigger> retrieved = _triggerService.getAll();
        assertNotNull(retrieved);
        assertEquals(1, retrieved.size());
        assertEquals(trigger, retrieved.get(0));
    }

    @Test
    public void testSimpleTemplate() {
        ScriptTrigger[] triggers = { _triggerService.newEntity("trigger1", "Trigger 1", "script1", "associatedThing1", "Something happened!"),
                _triggerService.newEntity("trigger2", "Trigger 2", "script2", "associatedThing2", "Something happened!"),
                _triggerService.newEntity("trigger3", "Trigger 3", "script3", "associatedThing3", "Something happened!")};
        ScriptTriggerTemplate template = _templateService.newEntity("template1", "Here's a template!", new HashSet<ScriptTrigger>(Arrays.asList(triggers)), new HashSet<Long>(Arrays.asList(0L, 1L, 2L)));
        List<ScriptTriggerTemplate> retrieved = _templateService.getAll();
        assertNotNull(retrieved);
        assertEquals(1, retrieved.size());
        assertEquals(template, retrieved.get(0));
        assertEquals(3, retrieved.get(0).getTriggers().size());
        assertEquals(3, retrieved.get(0).getAssociatedEntities().size());
    }

    @Test
    public void testSimpleTemplatesAndEntities() {
        ScriptTrigger trigger0 = _triggerService.newEntity("trigger0", "Trigger 0", "script0", "associatedThing0", "Something happened!");
        ScriptTrigger trigger1 = _triggerService.newEntity("trigger1", "Trigger 1", "script1", "associatedThing1", "Something happened!");
        ScriptTrigger trigger2 = _triggerService.newEntity("trigger2", "Trigger 2", "script2", "associatedThing2", "Something happened!");

        ScriptTriggerTemplate template = _templateService.newEntity("template1", "Here's a template!", new HashSet<ScriptTrigger>(Arrays.asList(trigger0, trigger1, trigger2)), new HashSet<Long>(Arrays.asList(0L, 1L, 2L)));

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

        List<ScriptTriggerTemplate> entities0 = _templateService.getTemplatesForEntity(0L);
        List<ScriptTriggerTemplate> entities1 = _templateService.getTemplatesForEntity(1L);
        List<ScriptTriggerTemplate> entities2 = _templateService.getTemplatesForEntity(2L);

        assertNotNull(entities0);
        assertEquals(1, entities0.size());
        assertNotNull(entities1);
        assertEquals(1, entities1.size());
        assertNotNull(entities2);
        assertEquals(1, entities2.size());
    }
    @Test
    public void testDoubleTemplatesAndEntities() throws JsonProcessingException {
        ScriptTrigger trigger0 = _triggerService.newEntity("trigger0", "Trigger 0", "script0", "associatedThing0", "Something happened!");
        ScriptTrigger trigger1 = _triggerService.newEntity("trigger1", "Trigger 1", "script1", "associatedThing1", "Something happened!");
        ScriptTrigger trigger2 = _triggerService.newEntity("trigger2", "Trigger 2", "script2", "associatedThing2", "Something happened!");

        ScriptTriggerTemplate template0 = _templateService.newEntity("template0", "Here's a template!", new HashSet<ScriptTrigger>(Arrays.asList(trigger0, trigger1)), new HashSet<Long>(Arrays.asList(0L, 1L)));
        ScriptTriggerTemplate template1 = _templateService.newEntity("template1", "Here's a template!", new HashSet<ScriptTrigger>(Arrays.asList(trigger1, trigger2)), new HashSet<Long>(Arrays.asList(1L, 2L)));

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

        List<ScriptTriggerTemplate> entities0 = _templateService.getTemplatesForEntity(0L);
        List<ScriptTriggerTemplate> entities1 = _templateService.getTemplatesForEntity(1L);
        List<ScriptTriggerTemplate> entities2 = _templateService.getTemplatesForEntity(2L);

        assertNotNull(entities0);
        assertEquals(1, entities0.size());
        assertNotNull(entities1);
        assertEquals(2, entities1.size());
        assertNotNull(entities2);
        assertEquals(1, entities2.size());
    }

    @Test
    public void testRelatedTemplatesAndEntities() throws JsonProcessingException {
        ScriptTrigger trigger0 = _triggerService.newEntity("trigger0", "Trigger 0", "script0", "associatedThing0", "Something happened!");
        ScriptTrigger trigger1 = _triggerService.newEntity("trigger1", "Trigger 1", "script1", "associatedThing1", "Something happened!");
        ScriptTrigger trigger2 = _triggerService.newEntity("trigger2", "Trigger 2", "script2", "associatedThing2", "Something happened!");
        ScriptTrigger trigger3 = _triggerService.newEntity("trigger3", "Trigger 3", "script3", "associatedThing3", "Something happened!");
        ScriptTrigger trigger4 = _triggerService.newEntity("trigger4", "Trigger 4", "script4", "associatedThing4", "Something happened!");
        ScriptTrigger trigger5 = _triggerService.newEntity("trigger5", "Trigger 5", "script5", "associatedThing5", "Something happened!");
        ScriptTrigger trigger6 = _triggerService.newEntity("trigger6", "Trigger 6", "script6", "associatedThing6", "Something happened!");

        ScriptTriggerTemplate template0 = _templateService.newEntity("template1", "Here's a template!", new HashSet<ScriptTrigger>(Arrays.asList(trigger0, trigger1, trigger2)), new HashSet<Long>(Arrays.asList(0L, 1L, 2L)));
        ScriptTriggerTemplate template1 = _templateService.newEntity("template2", "Yet another template!", new HashSet<ScriptTrigger>(Arrays.asList(trigger2, trigger3, trigger4)), new HashSet<Long>(Arrays.asList(2L, 3L, 4L)));
        ScriptTriggerTemplate template2 = _templateService.newEntity("template3", "Yet another template!", new HashSet<ScriptTrigger>(Arrays.asList(trigger4, trigger5, trigger6)), new HashSet<Long>(Arrays.asList(4L, 5L, 6L)));

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

        List<ScriptTriggerTemplate> entities0 = _templateService.getTemplatesForEntity(0L);
        List<ScriptTriggerTemplate> entities1 = _templateService.getTemplatesForEntity(1L);
        List<ScriptTriggerTemplate> entities2 = _templateService.getTemplatesForEntity(2L);
        List<ScriptTriggerTemplate> entities3 = _templateService.getTemplatesForEntity(3L);
        List<ScriptTriggerTemplate> entities4 = _templateService.getTemplatesForEntity(4L);
        List<ScriptTriggerTemplate> entities5 = _templateService.getTemplatesForEntity(5L);
        List<ScriptTriggerTemplate> entities6 = _templateService.getTemplatesForEntity(6L);

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

    @Inject
    private ScriptService _scriptService;

    @Inject
    private ScriptTriggerService _triggerService;

    @Inject
    private ScriptTriggerTemplateService _templateService;
}
