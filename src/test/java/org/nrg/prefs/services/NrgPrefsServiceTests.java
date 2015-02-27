package org.nrg.prefs.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.constants.Scope;
import org.nrg.prefs.entities.Tool;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests the NRG preferences service. This tests the full range of operations of the preferences service.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@TransactionConfiguration(defaultRollback = true)
@Transactional
public class NrgPrefsServiceTests {
    private static final Map<String, String> prefs1 = new HashMap<>();
    static {
        prefs1.put("preference1", "defaultValue1");
        prefs1.put("preference2", "defaultValue2");
        prefs1.put("preference3", "defaultValue3");
    }
    private static final Map<String, String> prefs2 = new HashMap<>();
    static {
        prefs2.put("preference1", "defaultValue1");
        prefs2.put("preference2", "defaultValue2");
        prefs2.put("preference3", "defaultValue3");
        prefs2.put("preference4", "defaultValue4");
        prefs2.put("preference5", "defaultValue5");
    }

    @Test
    public void testCreateToolWithoutDefaults() {
        final Tool tool = _service.createTool("tool1", "Tool 1", "This is tool 1, let's have some fun.");
        final Set<Tool> tools = _service.getTools();
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertTrue(tools.contains(tool));
        assertEquals(tool, tools.toArray()[0]);
        assertEquals(0, tool.getToolPreferences().size());
    }

    @Test
    public void testCreateTool() {
        final Tool tool = _service.createTool("tool1", "Tool 1", "This is tool 1, let's have some fun.", prefs1);
        final Set<Tool> tools = _service.getTools();
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertTrue(tools.contains(tool));
        assertEquals(tool, tools.toArray()[0]);
        assertEquals(3, tool.getToolPreferences().size());
    }

    @Test
    public void testSimpleToolAndPreference() {
        final Tool tool = _service.createTool("tool1", "Tool 1", "This is tool 1, let's have some fun.", prefs1);
        assertNotNull(tool);
        assertEquals("tool1", tool.getToolId());
        assertEquals("defaultValue1", _service.getPreferenceValue("tool1", "preference1"));
        assertEquals("defaultValue2", _service.getPreferenceValue("tool1", "preference2"));
        assertEquals("defaultValue3", _service.getPreferenceValue("tool1", "preference3"));
        _service.setPreferenceValue("tool1", "preference1", "value1");
        _service.setPreferenceValue("tool1", "preference2", "value2");
        _service.setPreferenceValue("tool1", "preference3", "value3");
        assertEquals("value1", _service.getPreferenceValue("tool1", "preference1"));
        assertEquals("value2", _service.getPreferenceValue("tool1", "preference2"));
        assertEquals("value3", _service.getPreferenceValue("tool1", "preference3"));
    }

    /**
     * Tests that preferences with the same name in a different tool don't update together.
     */
    @Test
    public void testMultipleToolsAndPreference() {
        final Tool tool1 = _service.createTool("tool1", "Tool 1", "This is tool 1, let's have some fun.", prefs1);
        final Tool tool2 = _service.createTool("tool2", "Tool 2", "This is tool 2, let's get kinda fou.", prefs1);
        assertNotNull(tool1);
        assertEquals("tool1", tool1.getToolId());
        assertEquals("defaultValue1", _service.getPreferenceValue("tool1", "preference1"));
        assertEquals("defaultValue2", _service.getPreferenceValue("tool1", "preference2"));
        assertEquals("defaultValue3", _service.getPreferenceValue("tool1", "preference3"));
        assertNotNull(tool2);
        assertEquals("tool2", tool2.getToolId());
        assertEquals("defaultValue1", _service.getPreferenceValue("tool2", "preference1"));
        assertEquals("defaultValue2", _service.getPreferenceValue("tool2", "preference2"));
        assertEquals("defaultValue3", _service.getPreferenceValue("tool2", "preference3"));
        _service.setPreferenceValue("tool1", "preference1", "value1");
        _service.setPreferenceValue("tool1", "preference2", "value2");
        _service.setPreferenceValue("tool1", "preference3", "value3");
        assertEquals("value1", _service.getPreferenceValue("tool1", "preference1"));
        assertEquals("value2", _service.getPreferenceValue("tool1", "preference2"));
        assertEquals("value3", _service.getPreferenceValue("tool1", "preference3"));
        assertEquals("defaultValue1", _service.getPreferenceValue("tool2", "preference1"));
        assertEquals("defaultValue2", _service.getPreferenceValue("tool2", "preference2"));
        assertEquals("defaultValue3", _service.getPreferenceValue("tool2", "preference3"));
    }

    @Test
    public void testToolWithScope() {
        final Tool tool = _service.createTool("tool1", "Tool 1", "This is tool 1, let's have some fun.", prefs2);
        assertNotNull(tool);
        assertEquals("tool1", tool.getToolId());
        assertEquals("defaultValue1", _service.getPreferenceValue("tool1", "preference1"));
        assertEquals("defaultValue2", _service.getPreferenceValue("tool1", "preference2"));
        assertEquals("defaultValue3", _service.getPreferenceValue("tool1", "preference3"));
        assertEquals("defaultValue4", _service.getPreferenceValue("tool1", "preference4"));
        assertEquals("defaultValue5", _service.getPreferenceValue("tool1", "preference5"));

        final Set<String> tools = _service.getToolIds();
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertTrue(tools.contains("tool1"));

        final Set<String> propertyNames = _service.getToolPropertyNames("tool1");
        assertTrue(propertyNames.contains("preference1"));
        assertTrue(propertyNames.contains("preference2"));
        assertTrue(propertyNames.contains("preference3"));
        assertTrue(propertyNames.contains("preference4"));
        assertTrue(propertyNames.contains("preference5"));

        final Properties properties = _service.getToolProperties("tool1");
        assertTrue(properties.containsKey("preference1"));
        assertTrue(properties.containsKey("preference2"));
        assertTrue(properties.containsKey("preference3"));
        assertTrue(properties.containsKey("preference4"));
        assertTrue(properties.containsKey("preference5"));
        assertEquals("defaultValue1", properties.getProperty("preference1"));
        assertEquals("defaultValue2", properties.getProperty("preference2"));
        assertEquals("defaultValue3", properties.getProperty("preference3"));
        assertEquals("defaultValue4", properties.getProperty("preference4"));
        assertEquals("defaultValue5", properties.getProperty("preference5"));

        // Now set some values scoped to specific entities.
        _service.setPreferenceValue("tool1", "preference2", Scope.Project, "project1", "t1p2p1");
        _service.setPreferenceValue("tool1", "preference3", Scope.Subject, "p2s2", "t1p3p2s2");
        _service.setPreferenceValue("tool1", "preference4", Scope.Experiment, "p2s3e3", "t1p4p2s3e3");
        _service.setPreferenceValue("tool1", "preference5", Scope.Experiment, "p2s6e2", "t1p5p2s6e2");

        // Make sure all of our top-level values are still the same.
        assertEquals("defaultValue1", _service.getPreferenceValue("tool1", "preference1"));
        assertEquals("defaultValue2", _service.getPreferenceValue("tool1", "preference2"));
        assertEquals("defaultValue3", _service.getPreferenceValue("tool1", "preference3"));
        assertEquals("defaultValue4", _service.getPreferenceValue("tool1", "preference4"));
        assertEquals("defaultValue5", _service.getPreferenceValue("tool1", "preference5"));

        // Now check in the hierarchy of project1. Everything there should get the same value for preference2.
        assertEquals("t1p2p1", _service.getPreferenceValue("tool1", "preference2", Scope.Subject, "p1s1"));
        assertEquals("t1p2p1", _service.getPreferenceValue("tool1", "preference2", Scope.Experiment, "p1s1e1"));

        // Now check the hierarchy with subject p2s2. Its project and other subjects should still have its default value
        // but p2s2 and its experiments should have a different value.
        assertEquals("defaultValue3", _service.getPreferenceValue("tool1", "preference3", Scope.Project, "project2"));
        assertEquals("defaultValue3", _service.getPreferenceValue("tool1", "preference3", Scope.Subject, "p2s1"));
        assertEquals("defaultValue3", _service.getPreferenceValue("tool1", "preference3", Scope.Experiment, "p2s1e3"));
        assertEquals("t1p3p2s2", _service.getPreferenceValue("tool1", "preference3", Scope.Subject, "p2s2"));
        assertEquals("t1p3p2s2", _service.getPreferenceValue("tool1", "preference3", Scope.Experiment, "p2s2e1"));

        // Now check the hierarchy with experiments p2s3e3 and p2s6e2. Their project and subjects should have the
        // default value for preference4 and preference5, but just those experiments should change.
        assertEquals("defaultValue4", _service.getPreferenceValue("tool1", "preference4", Scope.Project, "project2"));
        assertEquals("defaultValue5", _service.getPreferenceValue("tool1", "preference5", Scope.Project, "project2"));
        assertEquals("defaultValue4", _service.getPreferenceValue("tool1", "preference4", Scope.Subject, "p2s3"));
        assertEquals("defaultValue5", _service.getPreferenceValue("tool1", "preference5", Scope.Subject, "p2s6"));
        assertEquals("defaultValue4", _service.getPreferenceValue("tool1", "preference4", Scope.Experiment, "p2s6e2"));
        assertEquals("defaultValue5", _service.getPreferenceValue("tool1", "preference5", Scope.Experiment, "p2s3e3"));
        assertEquals("t1p4p2s3e3", _service.getPreferenceValue("tool1", "preference4", Scope.Experiment, "p2s3e3"));
        assertEquals("t1p5p2s6e2", _service.getPreferenceValue("tool1", "preference5", Scope.Experiment, "p2s6e2"));
    }

    @Test
    public void testLoadSiteConfigurationProperties() throws IOException {
        final Properties siteConfiguration = new Properties();
        siteConfiguration.load(Properties.class.getResourceAsStream("/org/nrg/prefs/services/siteConfiguration.properties"));
        assertNotNull(siteConfiguration);
        assertTrue(siteConfiguration.size() > 0);
        _service.createTool("siteConfig", "Site Configuration", "This is the main tool for mapping the site configuration", siteConfiguration);
        assertEquals("true", _service.getPreferenceValue("siteConfig", "enableDicomReceiver"));
        assertEquals("org.nrg.xnat.utils.ChecksumsSiteConfigurationListener", _service.getPreferenceValue("siteConfig", "checksums.property.changed.listener"));
    }

    @Inject
    private NrgPrefsService _service;
}
