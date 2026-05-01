/*
 * automation: org.nrg.automation.services.TestScriptTriggerAndTemplateServices
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.automation.configuration.AutomationTestsConfiguration;
import org.nrg.automation.entities.Script;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.entities.ScriptTriggerTemplate;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.services.SerializerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * TestScriptTriggerAndTemplateServices class.
 *
 * @author Rick Herrick
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AutomationTestsConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
public class TestScriptTriggerAndTemplateServices {
    public static final String              EVENT_ID_1      = "Something happened!";
    public static final String              EVENT_ID_2      = "Something else happened!";
    public static final String              EVENT_CLASS     = "org.nrg.xft.event.entities.WorkflowStatusEvent";
    public static final String              STATUS_COMPLETE = "Complete";
    public static final Map<String, String> EVENT_FILTER    = ImmutableMap.of("status", STATUS_COMPLETE);

    private final ScriptService                _scriptService;
    private final ScriptTriggerService         _triggerService;
    private final ScriptTriggerTemplateService _templateService;
    private final SerializerService            _serializer;

    @Autowired
    public TestScriptTriggerAndTemplateServices(final ScriptService scriptService, final ScriptTriggerService triggerService, final ScriptTriggerTemplateService templateService, final SerializerService serializer) {
        _scriptService   = scriptService;
        _triggerService  = triggerService;
        _templateService = templateService;
        _serializer      = serializer;
    }

    @Test
    public void testSimpleScript() {
        Script       script    = _scriptService.newEntity("script1", "script1 label", "This is my first script!", "groovy", "2.3.6", "println \"Hello world!\"");
        List<Script> retrieved = _scriptService.getAll();
        assertThat(retrieved).isNotNull()
                             .isNotEmpty()
                             .hasSize(1)
                             .containsExactly(script);
    }

    @Test
    public void testSimpleTrigger() {
        ScriptTrigger       trigger   = _triggerService.newEntity("trigger1", "This is my first trigger!", "script1", "associatedThing1", EVENT_CLASS, EVENT_ID_1);
        List<ScriptTrigger> retrieved = _triggerService.getAll();
        assertThat(retrieved).isNotNull()
                             .isNotEmpty()
                             .hasSize(1)
                             .containsExactly(trigger);
    }

    @Test
    public void testSimpleTemplate() {
        ScriptTrigger[] triggers = {_triggerService.newEntity("trigger1", "Trigger 1", "script1", "associatedThing1", EVENT_CLASS, EVENT_ID_1),
                                    _triggerService.newEntity("trigger2", "Trigger 2", "script2", "associatedThing2", EVENT_CLASS, EVENT_ID_1),
                                    _triggerService.newEntity("trigger3", "Trigger 3", "script3", "associatedThing3", EVENT_CLASS, EVENT_ID_1)};
        ScriptTriggerTemplate       template  = _templateService.newEntity("template1", "Here's a template!", new HashSet<>(Arrays.asList(triggers)), new HashSet<>(Arrays.asList("0", "1", "2")));
        List<ScriptTriggerTemplate> retrieved = _templateService.getAll();
        assertThat(retrieved).isNotNull()
                             .isNotEmpty()
                             .hasSize(1)
                             .containsExactly(template);
        assertThat(template).isNotNull()
                            .extracting(ScriptTriggerTemplate::getTriggers, as(InstanceOfAssertFactories.collection(ScriptTrigger.class)))
                            .isNotNull()
                            .hasSize(3);
        assertThat(template).extracting(ScriptTriggerTemplate::getAssociatedEntities, as(InstanceOfAssertFactories.collection(String.class)))
                            .isNotNull()
                            .hasSize(3);
    }

    @Test
    public void testSimpleTemplatesAndEntities() {
        ScriptTrigger trigger0 = _triggerService.newEntity("trigger0", "Trigger 0", "script0", "associatedThing0", EVENT_CLASS, EVENT_ID_1);
        ScriptTrigger trigger1 = _triggerService.newEntity("trigger1", "Trigger 1", "script1", "associatedThing1", EVENT_CLASS, EVENT_ID_1);
        ScriptTrigger trigger2 = _triggerService.newEntity("trigger2", "Trigger 2", "script2", "associatedThing2", EVENT_CLASS, EVENT_ID_1);

        ScriptTriggerTemplate template = _templateService.newEntity("template1", "Here's a template!", new HashSet<>(Arrays.asList(trigger0, trigger1, trigger2)), new HashSet<>(Arrays.asList("0", "1", "2")));

        List<ScriptTriggerTemplate> all = _templateService.getAll();
        assertThat(all).isNotNull()
                       .isNotEmpty()
                       .hasSize(1)
                       .containsExactly(template);

        ScriptTriggerTemplate retrieved = _templateService.retrieve(template.getId());
        assertThat(retrieved).isNotNull()
                             .isEqualTo(template)
                             .extracting(ScriptTriggerTemplate::getTriggers, as(InstanceOfAssertFactories.collection(ScriptTrigger.class)))
                             .isNotNull()
                             .hasSize(3);
        assertThat(retrieved).extracting(ScriptTriggerTemplate::getAssociatedEntities, as(InstanceOfAssertFactories.collection(String.class)))
                             .isNotNull()
                             .hasSize(3);

        ScriptTrigger retrievedTrigger0 = _triggerService.getByTriggerId("trigger0");
        ScriptTrigger retrievedTrigger1 = _triggerService.getByTriggerId("trigger1");
        ScriptTrigger retrievedTrigger2 = _triggerService.getByTriggerId("trigger2");

        assertThat(retrievedTrigger0).isNotNull()
                                     .hasFieldOrPropertyWithValue("triggerId", "trigger0");
        assertThat(retrievedTrigger1).isNotNull()
                                     .hasFieldOrPropertyWithValue("triggerId", "trigger1");
        assertThat(retrievedTrigger2).isNotNull()
                                     .hasFieldOrPropertyWithValue("triggerId", "trigger2");

        List<ScriptTriggerTemplate> templates0 = _templateService.getTemplatesForTrigger(retrievedTrigger0);
        List<ScriptTriggerTemplate> templates1 = _templateService.getTemplatesForTrigger(retrievedTrigger1);
        List<ScriptTriggerTemplate> templates2 = _templateService.getTemplatesForTrigger(retrievedTrigger2);

        Stream.of(templates0, templates1, templates2).forEach(templates -> assertThat(templates).isNotNull().isNotEmpty().hasSize(1).containsExactly(template));

        List<ScriptTriggerTemplate> entities0 = _templateService.getTemplatesForEntity("0");
        List<ScriptTriggerTemplate> entities1 = _templateService.getTemplatesForEntity("1");
        List<ScriptTriggerTemplate> entities2 = _templateService.getTemplatesForEntity("2");

        Stream.of(entities0, entities1, entities2).forEach(entities -> assertThat(entities).isNotNull().isNotEmpty().hasSize(1));
    }

    @Test
    public void testDoubleTemplatesAndEntities() {
        ScriptTrigger trigger0 = _triggerService.newEntity("trigger0", "Trigger 0", "script0", "associatedThing0", EVENT_CLASS, EVENT_ID_1);
        ScriptTrigger trigger1 = _triggerService.newEntity("trigger1", "Trigger 1", "script1", "associatedThing1", EVENT_CLASS, EVENT_ID_1);
        ScriptTrigger trigger2 = _triggerService.newEntity("trigger2", "Trigger 2", "script2", "associatedThing2", EVENT_CLASS, EVENT_ID_1);

        ScriptTriggerTemplate template0 = _templateService.newEntity("template0", "Here's a template!", new HashSet<>(Arrays.asList(trigger0, trigger1)), new HashSet<>(Arrays.asList("0", "1")));
        ScriptTriggerTemplate template1 = _templateService.newEntity("template1", "Here's a template!", new HashSet<>(Arrays.asList(trigger1, trigger2)), new HashSet<>(Arrays.asList("1", "2")));

        List<ScriptTriggerTemplate> all = _templateService.getAll();
        assertThat(all).isNotNull()
                       .isNotEmpty()
                       .hasSize(2)
                       .containsExactly(template0, template1);

        ScriptTriggerTemplate retrieved0 = _templateService.retrieve(template0.getId());
        ScriptTriggerTemplate retrieved1 = _templateService.retrieve(template1.getId());

        assertThat(retrieved0).isNotNull().isEqualTo(template0).extracting(ScriptTriggerTemplate::getTriggers, as(InstanceOfAssertFactories.collection(ScriptTrigger.class))).hasSize(2);
        assertThat(retrieved0).extracting(ScriptTriggerTemplate::getAssociatedEntities, as(InstanceOfAssertFactories.collection(String.class))).hasSize(2);
        assertThat(retrieved1).isNotNull().isEqualTo(template1).extracting(ScriptTriggerTemplate::getTriggers, as(InstanceOfAssertFactories.collection(ScriptTrigger.class))).hasSize(2);
        assertThat(retrieved1).extracting(ScriptTriggerTemplate::getAssociatedEntities, as(InstanceOfAssertFactories.collection(String.class))).hasSize(2);

        ScriptTrigger retrievedTrigger0 = _triggerService.getByTriggerId("trigger0");
        ScriptTrigger retrievedTrigger1 = _triggerService.getByTriggerId("trigger1");
        ScriptTrigger retrievedTrigger2 = _triggerService.getByTriggerId("trigger2");

        assertThat(retrievedTrigger0).isNotNull().isEqualTo(trigger0);
        assertThat(retrievedTrigger1).isNotNull().isEqualTo(trigger1);
        assertThat(retrievedTrigger2).isNotNull().isEqualTo(trigger2);

        List<ScriptTriggerTemplate> templates0 = _templateService.getTemplatesForTrigger(retrievedTrigger0);
        List<ScriptTriggerTemplate> templates1 = _templateService.getTemplatesForTrigger(retrievedTrigger1);
        List<ScriptTriggerTemplate> templates2 = _templateService.getTemplatesForTrigger(retrievedTrigger2);

        assertThat(templates0).isNotNull().isNotEmpty().hasSize(1).containsExactly(template0);
        assertThat(templates1).isNotNull().isNotEmpty().hasSize(2).containsExactly(template0, template1);
        assertThat(templates2).isNotNull().isNotEmpty().hasSize(1).containsExactly(template1);

        List<ScriptTriggerTemplate> entities0 = _templateService.getTemplatesForEntity("0");
        List<ScriptTriggerTemplate> entities1 = _templateService.getTemplatesForEntity("1");
        List<ScriptTriggerTemplate> entities2 = _templateService.getTemplatesForEntity("2");

        assertThat(entities0).isNotNull().isNotEmpty().hasSize(1);
        assertThat(entities1).isNotNull().isNotEmpty().hasSize(2);
        assertThat(entities2).isNotNull().isNotEmpty().hasSize(1);
    }

    @Test
    public void testRelatedTemplatesAndEntities() throws IOException {
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
        assertThat(triggers).isNotNull().isNotEmpty().hasSize(7).containsExactly(trigger0, trigger1, trigger2, trigger3, trigger4, trigger5, trigger6);

        List<ScriptTrigger> compareByEvent = _triggerService.getByEventAndFilters(EVENT_CLASS, EVENT_ID_2, EVENT_FILTER);
        assertThat(compareByEvent).isNotNull().isNotEmpty().hasSize(3);

        List<ScriptTrigger> compareByAssociation = _triggerService.getByScope(Scope.Project, "0");
        assertThat(compareByAssociation).isNotNull().isNotEmpty().hasSize(2).containsExactlyInAnyOrder(trigger0, trigger7);

        ScriptTrigger getByEventAndAssociation = _triggerService.getByScopeEntityAndEventAndFilters(Scope.Project, "0", EVENT_CLASS, EVENT_ID_1, EVENT_FILTER);
        assertThat(getByEventAndAssociation).isNotNull()
                                            .hasFieldOrPropertyWithValue("scriptId", "script0")
                                            .hasFieldOrPropertyWithValue("association", Scope.encode(Scope.Project, "0"))
                                            .hasFieldOrPropertyWithValue("event", EVENT_ID_1);

        ScriptTriggerTemplate template0 = _templateService.newEntity("template1", "Here's a template!", new HashSet<>(Arrays.asList(trigger0, trigger1, trigger2)), new HashSet<>(Arrays.asList("0", "1", "2")));
        ScriptTriggerTemplate template1 = _templateService.newEntity("template2", "Yet another template!", new HashSet<>(Arrays.asList(trigger2, trigger3, trigger4)), new HashSet<>(Arrays.asList("2", "3", "4")));
        ScriptTriggerTemplate template2 = _templateService.newEntity("template3", "Yet another template!", new HashSet<>(Arrays.asList(trigger4, trigger5, trigger6)), new HashSet<>(Arrays.asList("4", "5", "6")));

        List<ScriptTriggerTemplate> retrieved = _templateService.getAll();
        assertThat(retrieved).isNotNull().isNotEmpty().hasSize(3);

        ScriptTriggerTemplate retrieved0 = _templateService.retrieve(template0.getId());
        ScriptTriggerTemplate retrieved1 = _templateService.retrieve(template1.getId());
        ScriptTriggerTemplate retrieved2 = _templateService.retrieve(template2.getId());

        assertThat(retrieved0).isNotNull()
                              .isEqualTo(template0)
                              .extracting(ScriptTriggerTemplate::getTriggers, as(InstanceOfAssertFactories.collection(ScriptTrigger.class)))
                              .hasSize(3);
        assertThat(retrieved0).extracting(ScriptTriggerTemplate::getAssociatedEntities, as(InstanceOfAssertFactories.collection(String.class)))
                              .hasSize(3);
        assertThat(retrieved1).isNotNull()
                              .isEqualTo(template1)
                              .extracting(ScriptTriggerTemplate::getTriggers, as(InstanceOfAssertFactories.collection(ScriptTrigger.class)))
                              .hasSize(3);
        assertThat(retrieved1).extracting(ScriptTriggerTemplate::getAssociatedEntities, as(InstanceOfAssertFactories.collection(String.class)))
                              .hasSize(3);
        assertThat(retrieved2).isNotNull()
                              .isEqualTo(template2)
                              .extracting(ScriptTriggerTemplate::getTriggers, as(InstanceOfAssertFactories.collection(ScriptTrigger.class)))
                              .hasSize(3);
        assertThat(retrieved2).extracting(ScriptTriggerTemplate::getAssociatedEntities, as(InstanceOfAssertFactories.collection(String.class)))
                              .hasSize(3);

        ScriptTrigger retrievedTrigger0 = _triggerService.getByTriggerId("trigger0");
        ScriptTrigger retrievedTrigger1 = _triggerService.getByTriggerId("trigger1");
        ScriptTrigger retrievedTrigger2 = _triggerService.getByTriggerId("trigger2");
        ScriptTrigger retrievedTrigger3 = _triggerService.getByTriggerId("trigger3");
        ScriptTrigger retrievedTrigger4 = _triggerService.getByTriggerId("trigger4");
        ScriptTrigger retrievedTrigger5 = _triggerService.getByTriggerId("trigger5");
        ScriptTrigger retrievedTrigger6 = _triggerService.getByTriggerId("trigger6");

        assertThat(retrievedTrigger0).isNotNull().hasFieldOrPropertyWithValue("triggerId", "trigger0");
        assertThat(retrievedTrigger1).isNotNull().hasFieldOrPropertyWithValue("triggerId", "trigger1");
        assertThat(retrievedTrigger2).isNotNull().hasFieldOrPropertyWithValue("triggerId", "trigger2");
        assertThat(retrievedTrigger3).isNotNull().hasFieldOrPropertyWithValue("triggerId", "trigger3");
        assertThat(retrievedTrigger4).isNotNull().hasFieldOrPropertyWithValue("triggerId", "trigger4");
        assertThat(retrievedTrigger5).isNotNull().hasFieldOrPropertyWithValue("triggerId", "trigger5");
        assertThat(retrievedTrigger6).isNotNull().hasFieldOrPropertyWithValue("triggerId", "trigger6");

        List<ScriptTriggerTemplate> triggers0 = _templateService.getTemplatesForTrigger(trigger0);
        List<ScriptTriggerTemplate> triggers1 = _templateService.getTemplatesForTrigger(trigger1);
        List<ScriptTriggerTemplate> triggers2 = _templateService.getTemplatesForTrigger(trigger2);
        List<ScriptTriggerTemplate> triggers3 = _templateService.getTemplatesForTrigger(trigger3);
        List<ScriptTriggerTemplate> triggers4 = _templateService.getTemplatesForTrigger(trigger4);
        List<ScriptTriggerTemplate> triggers5 = _templateService.getTemplatesForTrigger(trigger5);
        List<ScriptTriggerTemplate> triggers6 = _templateService.getTemplatesForTrigger(trigger6);

        assertThat(triggers0).isNotNull().isNotEmpty().hasSize(1).containsExactly(retrieved0);
        assertThat(triggers1).isNotNull().isNotEmpty().hasSize(1).containsExactly(retrieved0);
        assertThat(triggers2).isNotNull().isNotEmpty().hasSize(2).containsExactly(retrieved0, retrieved1);
        assertThat(triggers3).isNotNull().isNotEmpty().hasSize(1).containsExactly(retrieved1);
        assertThat(triggers4).isNotNull().isNotEmpty().hasSize(2).containsExactly(retrieved1, retrieved2);
        assertThat(triggers5).isNotNull().isNotEmpty().hasSize(1).containsExactly(retrieved2);
        assertThat(triggers6).isNotNull().isNotEmpty().hasSize(1).containsExactly(retrieved2);

        List<ScriptTriggerTemplate> entities0 = _templateService.getTemplatesForEntity("0");
        List<ScriptTriggerTemplate> entities1 = _templateService.getTemplatesForEntity("1");
        List<ScriptTriggerTemplate> entities2 = _templateService.getTemplatesForEntity("2");
        List<ScriptTriggerTemplate> entities3 = _templateService.getTemplatesForEntity("3");
        List<ScriptTriggerTemplate> entities4 = _templateService.getTemplatesForEntity("4");
        List<ScriptTriggerTemplate> entities5 = _templateService.getTemplatesForEntity("5");
        List<ScriptTriggerTemplate> entities6 = _templateService.getTemplatesForEntity("6");

        assertThat(entities0).isNotNull().isNotEmpty().hasSize(1);
        assertThat(entities1).isNotNull().isNotEmpty().hasSize(1);
        assertThat(entities2).isNotNull().isNotEmpty().hasSize(2);
        assertThat(entities3).isNotNull().isNotEmpty().hasSize(1);
        assertThat(entities4).isNotNull().isNotEmpty().hasSize(2);
        assertThat(entities5).isNotNull().isNotEmpty().hasSize(1);
        assertThat(entities6).isNotNull().isNotEmpty().hasSize(1);

        final String template0json = _serializer.toJson(retrieved0);
        final String template1json = _serializer.toJson(retrieved0);
        System.out.println(template0json);
        System.out.println(template1json);
    }
}
