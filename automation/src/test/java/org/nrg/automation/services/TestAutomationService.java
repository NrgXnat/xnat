package org.nrg.automation.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.automation.configuration.AutomationTestsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.nrg.automation.services.AutomationService.AUTOMATION_ENABLED_PROPERTY;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AutomationTestsConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
public class TestAutomationService {
    private final AutomationService automationService;

    @Value(AUTOMATION_ENABLED_PROPERTY)
    private boolean automationEnabled;

    @Autowired
    public TestAutomationService(final AutomationService automationService) {
        this.automationService = automationService;
    }

    @Test
    public void testGetAutomationEnabled() {
        assertThat(automationService).isNotNull();
        assertThat(automationEnabled).isEqualTo(automationService.getAutomationEnabled());
    }
}