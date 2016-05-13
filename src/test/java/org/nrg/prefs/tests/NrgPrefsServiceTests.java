package org.nrg.prefs.tests;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.prefs.configuration.NrgPrefsServiceTestsConfiguration;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.tools.basic.BasicTestTool;
import org.nrg.prefs.tools.beans.BeanPrefsTool;
import org.nrg.prefs.tools.beans.BeanPrefsToolPreference;
import org.nrg.prefs.tools.properties.PropertiesPrefsTool;
import org.nrg.prefs.tools.relaxed.RelaxedPrefsTool;
import org.nrg.prefs.tools.strict.StrictPrefsTool;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
    @Test
    public void testBasicPrefsTool() throws InvalidPreferenceName {
        assertNotNull(_basicPrefsTool);
        assertEquals("valueA", _basicPrefsTool.getPrefA());
        assertEquals("valueB", _basicPrefsTool.getPrefB());
        _basicPrefsTool.setPrefA("valueAMod");
        _basicPrefsTool.setPrefB("valueBMod");
        assertEquals("valueAMod", _basicPrefsTool.getPrefA());
        assertEquals("valueBMod", _basicPrefsTool.getPrefB());
        final Map<String, Object> preferences = _basicPrefsTool.getPreferenceMap();
        assertEquals(2, preferences.size());
    }

    @Test
    public void testPropertiesTestTool() throws InvalidPreferenceName {
        assertNotNull(_propertiesPrefsTool);
        assertEquals("valueA", _propertiesPrefsTool.getPropertyA());
        assertEquals("valueB", _propertiesPrefsTool.getPropertyB());
        _propertiesPrefsTool.setPropertyA("valueAMod");
        _propertiesPrefsTool.setPropertyB("valueBMod");
        assertEquals("valueAMod", _propertiesPrefsTool.getPropertyA());
        assertEquals("valueBMod", _propertiesPrefsTool.getPropertyB());
        final Map<String, Object> preferences = _propertiesPrefsTool.getPreferenceMap();
        assertEquals(2, preferences.size());
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
        _relaxedPrefsTool.setRelaxedWhatever("freeForm", "This can be anything!");
        assertEquals("This can be anything!", _relaxedPrefsTool.getRelaxedWhatever("freeForm"));
        final Map<String, Object> preferences = _relaxedPrefsTool.getPreferenceMap();
        assertEquals(4, preferences.size());
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

        final Map<String, Object> preferences = _strictPrefsTool.getPreferenceMap();
        assertEquals(2, preferences.size());

        // This will throw the InvalidPreferenceName exception.
        _strictPrefsTool.setStrictPrefC("defaultC");
    }

    @Test
    public void testBeanPrefsTool() throws InvalidPreferenceName, IOException {
        assertNotNull(_beanPrefsTool);

        // {'scpId':'CCIR','aeTitle':'CCIR','port':8104,'enabled':true}
        final BeanPrefsToolPreference prefA = _beanPrefsTool.getPrefA();
        assertNotNull(prefA);
        assertEquals("CCIR", prefA.getScpId());
        assertTrue(prefA.isEnabled());
        prefA.setEnabled(false);
        _beanPrefsTool.setPrefA(prefA);
        final BeanPrefsToolPreference modifiedAs = _beanPrefsTool.getPrefA();
        assertNotNull(modifiedAs);
        assertEquals("CCIR", modifiedAs.getScpId());
        assertFalse(modifiedAs.isEnabled());

        // ['XNAT','CCIR']
        final List<String> prefBs = _beanPrefsTool.getPrefBs();
        assertNotNull(prefBs);
        assertEquals(2, prefBs.size());
        assertTrue(prefBs.contains("XNAT"));
        assertFalse(prefBs.contains("CCF"));
        prefBs.remove("XNAT");
        prefBs.add("CCF");
        _beanPrefsTool.setPrefBs(prefBs);
        final List<String> modifiedBs = _beanPrefsTool.getPrefBs();
        assertNotNull(modifiedBs);
        assertEquals(2, modifiedBs.size());
        assertFalse(modifiedBs.contains("XNAT"));
        assertTrue(modifiedBs.contains("CCF"));

        // [{'scpId':'XNAT','aeTitle':'XNAT','port':8104,'enabled':true},
        //  {'scpId':'CCIR','aeTitle':'CCIR','port':8104,'enabled':true}]
        final List<BeanPrefsToolPreference> prefCs = _beanPrefsTool.getPrefCs();
        assertNotNull(prefCs);
        assertEquals(2, prefCs.size());
        assertTrue(prefCs.get(0).getScpId().equals("XNAT") || prefCs.get(1).getScpId().equals("XNAT"));
        assertTrue(prefCs.get(0).getScpId().equals("CCIR") || prefCs.get(1).getScpId().equals("CCIR"));
        final BeanPrefsToolPreference xnat;
        final BeanPrefsToolPreference ccir;
        if (prefCs.get(0).getScpId().equals("XNAT")) {
            xnat = prefCs.get(0);
            ccir = prefCs.get(1);
        } else {
            xnat = prefCs.get(1);
            ccir = prefCs.get(0);
        }
        assertEquals("XNAT", xnat.getAeTitle());
        assertEquals("CCIR", ccir.getAeTitle());
        assertTrue(xnat.isEnabled());
        assertTrue(ccir.isEnabled());
        ccir.setEnabled(false);
        _beanPrefsTool.setPrefC(ccir);
        final BeanPrefsToolPreference ccf = new BeanPrefsToolPreference();
        ccf.setScpId("CCF");
        ccf.setAeTitle("CCF");
        ccf.setPort(8104);
        _beanPrefsTool.setPrefC(ccf);
        final List<BeanPrefsToolPreference> modifiedCs = _beanPrefsTool.getPrefCs();
        assertNotNull(modifiedCs);
        assertEquals(3, modifiedCs.size());
        assertTrue(modifiedCs.get(0).getScpId().equals("XNAT") || modifiedCs.get(1).getScpId().equals("XNAT") || modifiedCs.get(2).getScpId().equals("XNAT"));
        assertTrue(modifiedCs.get(0).getScpId().equals("CCIR") || modifiedCs.get(1).getScpId().equals("CCIR") || modifiedCs.get(2).getScpId().equals("CCIR"));
        assertTrue(modifiedCs.get(0).getScpId().equals("CCF") || modifiedCs.get(1).getScpId().equals("CCF") || modifiedCs.get(2).getScpId().equals("CCF"));

        // {'XNAT':'192.168.10.1,'CCIR':'192.168.10.100'}
        final Map<String, String> prefDs = _beanPrefsTool.getPrefDs();
        assertNotNull(prefDs);
        assertEquals(2, prefDs.size());
        assertTrue(prefDs.containsKey("XNAT"));
        assertTrue(prefDs.containsKey("CCIR"));
        assertFalse(prefDs.containsKey("CCF"));
        assertEquals("192.168.10.1", prefDs.get("XNAT"));
        assertEquals("192.168.10.100", prefDs.get("CCIR"));
        _beanPrefsTool.deletePrefD("XNAT");
        _beanPrefsTool.setPrefD("CCF", "192.168.10.255");
        final Map<String, String> modifiedDs = _beanPrefsTool.getPrefDs();
        assertNotNull(modifiedDs);
        assertEquals(2, modifiedDs.size());
        assertFalse(modifiedDs.containsKey("XNAT"));
        assertTrue(prefDs.containsKey("CCIR"));
        assertTrue(modifiedDs.containsKey("CCF"));

        // {'XNAT':{'port':8104,'scpId':'XNAT','identifier':'XNAT','aeTitle':'XNAT','enabled':true},
        //  'CCIR':{'port':8104,'scpId':'CCIR','identifier':'CCIR','aeTitle':'CCIR','enabled':true}}
        final Map<String, BeanPrefsToolPreference> prefEs = _beanPrefsTool.getPrefEs();
        assertNotNull(prefEs);
        assertEquals(2, prefEs.size());
        assertTrue(prefEs.containsKey("XNAT"));
        assertTrue(prefEs.containsKey("CCIR"));
        assertEquals("XNAT", prefEs.get("XNAT").getAeTitle());
        assertEquals("CCIR", prefEs.get("CCIR").getAeTitle());
        final Map<String, Object> preferences = _beanPrefsTool.getPreferenceMap();
        assertEquals(5, preferences.size());
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
        assertTrue(defaults.size() > 0);
        // _service.createTool("siteConfig", "Site Configuration", "This is the main tool for mapping the site configuration", defaults, false, AbstractPreferenceBean.class.getName(), null);
        // Assert.assertEquals("true", _service.getPreferenceValue("siteConfig", "enableDicomReceiver"));
        // Assert.assertEquals("org.nrg.xnat.utils.ChecksumsSiteConfigurationListener", _service.getPreferenceValue("siteConfig", "checksums.property.changed.listener"));
    }

    @Inject
    private BasicTestTool _basicPrefsTool;

    @Inject
    private PropertiesPrefsTool _propertiesPrefsTool;

    @Inject
    private RelaxedPrefsTool _relaxedPrefsTool;

    @Inject
    private StrictPrefsTool _strictPrefsTool;

    @Inject
    private BeanPrefsTool _beanPrefsTool;
}
