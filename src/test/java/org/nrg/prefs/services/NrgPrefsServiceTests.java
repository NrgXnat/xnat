package org.nrg.prefs.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.prefs.beans.BaseNrgPreferences;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.preferences.SimplePrefsEntityResolver;
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
@ContextConfiguration
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
        assertEquals("defaultA", _relaxedPrefsTool.getRelaxedPrefA());
        assertEquals("defaultB", _relaxedPrefsTool.getRelaxedPrefB());
        _relaxedPrefsTool.setRelaxedPrefA("defaultAMod");
        _relaxedPrefsTool.setRelaxedPrefB("defaultBMod");
        assertEquals("defaultAMod", _relaxedPrefsTool.getRelaxedPrefA());
        assertEquals("defaultBMod", _relaxedPrefsTool.getRelaxedPrefB());
        assertNull(_relaxedPrefsTool.getRelaxedPrefC());
        _relaxedPrefsTool.setRelaxedPrefC("defaultC");
        assertEquals("defaultC", _relaxedPrefsTool.getRelaxedPrefC());
    }

    @Test(expected = InvalidPreferenceName.class)
    public void testStrictPrefsTool() throws InvalidPreferenceName {
        assertNotNull(_strictPrefsTool);
        assertEquals("defaultA", _strictPrefsTool.getStrictPrefA());
        assertEquals("defaultB", _strictPrefsTool.getStrictPrefB());
        _strictPrefsTool.setStrictPrefA("defaultAMod");
        _strictPrefsTool.setStrictPrefB("defaultBMod");
        assertEquals("defaultAMod", _strictPrefsTool.getStrictPrefA());
        assertEquals("defaultBMod", _strictPrefsTool.getStrictPrefB());
        final String prefC = _strictPrefsTool.getStrictPrefC();
        assertNull(prefC);

        // This will throw the InvalidPreferenceName exception.
        _strictPrefsTool.setStrictPrefC("defaultC");
    }

    @Test
    public void testCreateTool() {
    }

    @Test
    public void testSimpleToolAndPreference() throws NrgServiceException {
    }

    /**
     * Tests that preferences with the same name in a different tool don't update together.
     */
    @Test
    public void testMultipleToolsAndPreference() throws NrgServiceException {
    }

    @Test
    public void testToolWithScope() throws NrgServiceException {}

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
        _service.createTool("siteConfig", "Site Configuration", "This is the main tool for mapping the site configuration", defaults, false, BaseNrgPreferences.class.getName(), null);
        assertEquals("true", _service.getPreferenceValue("siteConfig", "enableDicomReceiver"));
        assertEquals("org.nrg.xnat.utils.ChecksumsSiteConfigurationListener", _service.getPreferenceValue("siteConfig", "checksums.property.changed.listener"));
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
