package org.nrg.prefs.tests;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.prefs.configuration.NrgPrefsServiceTestsConfiguration;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.services.NrgPrefsService;
import org.nrg.prefs.tools.basic.BasicTestTool;
import org.nrg.prefs.tools.relaxed.RelaxedPrefsTool;
import org.nrg.prefs.tools.strict.StrictPrefsTool;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Tests the NRG preferences service. This tests the full range of operations of the preferences service.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NrgPrefsServiceTestsConfiguration.class)
@Rollback
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
    public void testBasicPrefsTool() throws InvalidPreferenceName {
        assertNotNull(_basicPrefsTool);
        assertEquals("valueA", _basicPrefsTool.getPrefA());
        assertEquals("valueB", _basicPrefsTool.getPrefB());
        _basicPrefsTool.setPrefA("valueAMod");
        _basicPrefsTool.setPrefB("valueBMod");
        assertEquals("valueAMod", _basicPrefsTool.getPrefA());
        assertEquals("valueBMod", _basicPrefsTool.getPrefB());
    }

    @Test
    public void testRelaxedPrefsTool() throws InvalidPreferenceName {
        assertNotNull(_relaxedPrefsTool);
        assertEquals(null, _relaxedPrefsTool.getRelaxedPrefA());
        assertEquals(null, _relaxedPrefsTool.getRelaxedPrefB());
        _relaxedPrefsTool.setRelaxedPrefA("valueASet");
        _relaxedPrefsTool.setRelaxedPrefB("valueBSet");
        _relaxedPrefsTool.setRelaxedPrefC("valueCSet");
        assertEquals("valueASet", _relaxedPrefsTool.getRelaxedPrefA());
        assertEquals("valueBSet", _relaxedPrefsTool.getRelaxedPrefB());
        assertEquals("valueCSet", _relaxedPrefsTool.getRelaxedPrefC());
        _relaxedPrefsTool.setRelaxedPrefA("valueAMod");
        _relaxedPrefsTool.setRelaxedPrefB("valueBMod");
        _relaxedPrefsTool.setRelaxedPrefC("valueCMod");
        assertEquals("valueAMod", _relaxedPrefsTool.getRelaxedPrefA());
        assertEquals("valueBMod", _relaxedPrefsTool.getRelaxedPrefB());
        assertEquals("valueCMod", _relaxedPrefsTool.getRelaxedPrefC());
        _relaxedPrefsTool.setRelaxedWhatever("freeform", "this can be anything");
        assertEquals("this can be anything", _relaxedPrefsTool.getRelaxedWhatever("freeform"));
    }

    @Test(expected = InvalidPreferenceName.class)
    public void testStrictPrefsTool() throws InvalidPreferenceName {
        assertNotNull(_strictPrefsTool);
        assertEquals("strictValueA", _strictPrefsTool.getStrictPrefA());
        assertEquals("strictValueB", _strictPrefsTool.getStrictPrefB());
        _strictPrefsTool.setStrictPrefA("strictValueAMod");
        _strictPrefsTool.setStrictPrefB("strictValueBMod");
        assertEquals("strictValueAMod", _strictPrefsTool.getStrictPrefA());
        assertEquals("strictValueBMod", _strictPrefsTool.getStrictPrefB());
        final String prefC = _strictPrefsTool.getStrictPrefC();
        assertNull(prefC);

        // This will throw the InvalidPreferenceName exception.
        _strictPrefsTool.setStrictPrefC("defaultC");
    }

    @Ignore
    @Test
    public void testCreateTool() {
    }

    @Ignore
    @Test
    public void testSimpleToolAndPreference() throws NrgServiceException {
    }

    /**
     * Tests that preferences with the same name in a different tool don't update together.
     */
    @Ignore
    @Test
    public void testMultipleToolsAndPreference() throws NrgServiceException {
    }

    @Ignore
    @Test
    public void testToolWithScope() throws NrgServiceException {}

    @Ignore
    @Test
    public void testLoadSiteConfigurationProperties() throws IOException, InvalidPreferenceName, UnknownToolId {
        final Properties properties = new Properties();
        properties.load(Properties.class.getResourceAsStream("/org/nrg/prefs/services/siteConfiguration.properties"));
        assertNotNull(properties);
        assertTrue(properties.size() > 0);
        final Map<String, String> defaults = new HashMap<>();
        for (final String property : properties.stringPropertyNames()) {
            defaults.put(property, properties.getProperty(property));
        }
        // _service.createTool("siteConfig", "Site Configuration", "This is the main tool for mapping the site configuration", defaults, false, AbstractPreferencesBean.class.getName(), null);
        // Assert.assertEquals("true", _service.getPreferenceValue("siteConfig", "enableDicomReceiver"));
        // Assert.assertEquals("org.nrg.xnat.utils.ChecksumsSiteConfigurationListener", _service.getPreferenceValue("siteConfig", "checksums.property.changed.listener"));
    }

    @Inject
    private NrgPrefsService _service;

    @Inject
    private BasicTestTool _basicPrefsTool;

    @Inject
    private RelaxedPrefsTool _relaxedPrefsTool;

    @Inject
    private StrictPrefsTool _strictPrefsTool;
}
