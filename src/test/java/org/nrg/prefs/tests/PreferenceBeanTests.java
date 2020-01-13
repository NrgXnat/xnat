/*
 * prefs: org.nrg.prefs.tests.NrgPrefsServiceTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tests;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.assertj.core.api.Condition;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.utilities.Reflection;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.beans.PreferenceBean;
import org.nrg.prefs.configuration.NrgPrefsServiceTestsConfiguration;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.tools.basic.BasicEnum;
import org.nrg.prefs.tools.basic.BasicTestTool;
import org.nrg.prefs.tools.beans.BeanPrefsTool;
import org.nrg.prefs.tools.beans.BeanPrefsToolPreference;
import org.nrg.prefs.tools.properties.PropertiesPrefsTool;
import org.nrg.prefs.tools.relaxed.RelaxedPrefsTool;
import org.nrg.prefs.tools.strict.StrictPrefsTool;
import org.reflections.ReflectionUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;
import static org.nrg.prefs.services.PreferenceBeanHelper.propertize;

/**
 * Tests the NRG preferences service. This tests the full range of operations of the preferences service.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NrgPrefsServiceTestsConfiguration.class)
@Rollback
@Transactional
public class PreferenceBeanTests {
    @Test
    public void testBasicPrefsTool() throws InvalidPreferenceName {
        final Map<String, Object> preferences = _basicPrefsTool.getPreferences();
        assertThat(preferences).size().isEqualTo(3);

        assertThat(_basicPrefsTool).isNotNull();
        assertThat(_basicPrefsTool.getPrefA()).isEqualTo("valueA").isEqualTo(preferences.get("prefA"));
        assertThat(_basicPrefsTool.getPrefB()).isEqualTo("valueB").isEqualTo(preferences.get("prefB"));
        assertThat(_basicPrefsTool.getPrefC()).isEqualTo(BasicEnum.Value1).isEqualTo(preferences.get("prefC"));

        _basicPrefsTool.setPrefA("valueAMod");
        _basicPrefsTool.setPrefB("valueBMod");
        _basicPrefsTool.setPrefC(BasicEnum.Value2);

        assertThat(_basicPrefsTool.getPrefA()).isEqualTo("valueAMod").isEqualTo(preferences.get("prefA"));
        assertThat(_basicPrefsTool.getPrefB()).isEqualTo("valueBMod").isEqualTo(preferences.get("prefB"));
        assertThat(_basicPrefsTool.getPrefC()).isEqualTo(BasicEnum.Value2).isEqualTo(preferences.get("prefC"));

        validateBeanCache(_basicPrefsTool.getPreferenceBean(), preferences);
    }

    @Test
    @Ignore("This tests for something (using non-bean property names) that I don't want to have happen any more.")
    public void testPropertiesTestTool() throws InvalidPreferenceName {
        assertThat(_propertiesPrefsTool).isNotNull();
        assertThat(_propertiesPrefsTool.getPropertyA()).isEqualTo("valueA");
        assertThat(_propertiesPrefsTool.getPropertyB()).isEqualTo("valueB");
        _propertiesPrefsTool.setPropertyA("valueAMod");
        _propertiesPrefsTool.setPropertyB("valueBMod");
        assertThat(_propertiesPrefsTool.getPropertyA()).isEqualTo("valueAMod");
        assertThat(_propertiesPrefsTool.getPropertyB()).isEqualTo("valueBMod");
        final Map<String, Object> preferences = _propertiesPrefsTool.getPreferences();
        assertThat(preferences).size().isEqualTo(2);
        validateBeanCache(_propertiesPrefsTool.getPreferenceBean(), preferences);
    }

    @Test
    public void testRelaxedPrefsTool() throws InvalidPreferenceName {
        assertThat(_relaxedPrefsTool).isNotNull();
        assertThat(_relaxedPrefsTool.getRelaxedPrefA()).isNull();
        assertThat(_relaxedPrefsTool.getRelaxedPrefB()).isNull();
        _relaxedPrefsTool.setRelaxedPrefA("valueASet");
        _relaxedPrefsTool.setRelaxedPrefB("valueBSet");
        _relaxedPrefsTool.setRelaxedPrefC("valueCSet");
        assertThat(_relaxedPrefsTool.getRelaxedPrefA()).isEqualTo("valueASet");
        assertThat(_relaxedPrefsTool.getRelaxedPrefB()).isEqualTo("valueBSet");
        assertThat(_relaxedPrefsTool.getRelaxedPrefC()).isEqualTo("valueCSet");
        _relaxedPrefsTool.setRelaxedPrefA("valueAMod");
        _relaxedPrefsTool.setRelaxedPrefB("valueBMod");
        _relaxedPrefsTool.setRelaxedPrefC("valueCMod");
        assertThat(_relaxedPrefsTool.getRelaxedPrefA()).isEqualTo("valueAMod");
        assertThat(_relaxedPrefsTool.getRelaxedPrefB()).isEqualTo("valueBMod");
        assertThat(_relaxedPrefsTool.getRelaxedPrefC()).isEqualTo("valueCMod");
        _relaxedPrefsTool.setRelaxedWhatever("freeForm", "This can be anything!");
        assertThat(_relaxedPrefsTool.getRelaxedWhatever("freeForm")).isEqualTo("This can be anything!");

        final Map<String, Object> preferences = _relaxedPrefsTool.getPreferences();
        assertThat(preferences).size().isEqualTo(4);

        validateBeanCache(_relaxedPrefsTool.getPreferenceBean(), preferences);
    }

    @Test(expected = InvalidPreferenceName.class)
    public void testStrictPrefsTool() throws InvalidPreferenceName {
        assertThat(_strictPrefsTool).isNotNull();
        assertThat(_strictPrefsTool.getStrictPrefA()).isEqualTo("strictValueA");
        assertThat(_strictPrefsTool.getStrictPrefB()).isEqualTo("strictValueB");
        _strictPrefsTool.setStrictPrefA("strictValueAMod");
        _strictPrefsTool.setStrictPrefB("strictValueBMod");
        assertThat(_strictPrefsTool.getStrictPrefA()).isEqualTo("strictValueAMod");
        assertThat(_strictPrefsTool.getStrictPrefB()).isEqualTo("strictValueBMod");
        final String prefC = _strictPrefsTool.getStrictPrefC();
        assertThat(prefC).isBlank();

        final Map<String, Object> preferences = _strictPrefsTool.getPreferences();
        assertThat(preferences).size().isEqualTo(2);

        validateBeanCache(_strictPrefsTool.getPreferenceBean(), preferences);

        // This will throw the InvalidPreferenceName exception.
        _strictPrefsTool.setStrictPrefC("defaultC");
    }

    @Test
    public void testBeanPrefsTool() throws InvalidPreferenceName, IOException {
        assertThat(_beanPrefsTool).isNotNull();

        final PreferenceBean bean = _beanPrefsTool.getPreferenceBean();
        assertThat(bean).isNotNull();

        // {'scpId':'CCIR','aeTitle':'CCIR','port':8104,'enabled':true}
        final BeanPrefsToolPreference prefA = _beanPrefsTool.getPrefA();

        assertThat(prefA).isNotNull().hasFieldOrPropertyWithValue("scpId", "CCIR").hasFieldOrPropertyWithValue("enabled", true);

        prefA.setEnabled(false);
        _beanPrefsTool.setPrefA(prefA);
        final BeanPrefsToolPreference modifiedAs = _beanPrefsTool.getPrefA();

        assertThat(modifiedAs).isNotNull().hasFieldOrPropertyWithValue("scpId", "CCIR").hasFieldOrPropertyWithValue("enabled", false);

        // ['XNAT','CCIR']
        final List<String> prefBs = _beanPrefsTool.getPrefBs();
        assertThat(prefBs).isNotNull().size().isEqualTo(2);
        assertThat(prefBs).containsExactlyInAnyOrder("XNAT", "CCIR").doesNotContain("CCF");

        prefBs.remove("XNAT");
        prefBs.add("CCF");
        _beanPrefsTool.setPrefBs(prefBs);
        final List<String> modifiedBs = _beanPrefsTool.getPrefBs();

        assertThat(modifiedBs).isNotNull().size().isEqualTo(2);
        assertThat(modifiedBs).containsExactlyInAnyOrder("CCF", "CCIR").doesNotContain("XNAT");

        bean.set("[]", "prefBs");
        final List<String> overriddenBs = _beanPrefsTool.getPrefBs();

        assertThat(overriddenBs).isNotNull().isEmpty();

        bean.set("['XNAT','CCIR','CCF']", "prefBs");
        overriddenBs.addAll(_beanPrefsTool.getPrefBs());

        assertThat(overriddenBs).size().isEqualTo(3).returnToIterable().containsExactlyInAnyOrder("XNAT", "CCIR", "CCF");

        // [{'scpId':'XNAT','aeTitle':'XNAT','port':8104,'enabled':true},
        //  {'scpId':'CCIR','aeTitle':'CCIR','port':8104,'enabled':true}]
        final List<BeanPrefsToolPreference> prefCs = _beanPrefsTool.getPrefCs();
        assertThat(prefCs).isNotNull();
        assertThat(prefCs).size().isEqualTo(2);
        assertThat(prefCs).extracting("scpId", "port", "enabled").containsOnly(tuple("XNAT", 8104, true), tuple("CCIR", 8104, true));

        final boolean                 isZeroXnat = prefCs.get(0).getScpId().equals("XNAT");
        final BeanPrefsToolPreference xnat       = prefCs.get(isZeroXnat ? 0 : 1);
        final BeanPrefsToolPreference ccir       = prefCs.get(isZeroXnat ? 1 : 0);

        assertThat(xnat).hasFieldOrPropertyWithValue("aeTitle", "XNAT").hasFieldOrPropertyWithValue("port", 8104).hasFieldOrPropertyWithValue("enabled", true);
        assertThat(ccir).hasFieldOrPropertyWithValue("aeTitle", "CCIR").hasFieldOrPropertyWithValue("port", 8104).hasFieldOrPropertyWithValue("enabled", true);

        ccir.setEnabled(false);
        _beanPrefsTool.setPrefC(ccir);

        _beanPrefsTool.setPrefC(BeanPrefsToolPreference.builder().scpId("CCF").aeTitle("CCF").identifier("CCF").port(8104).build());
        final List<BeanPrefsToolPreference> modifiedCs = _beanPrefsTool.getPrefCs();

        assertThat(modifiedCs).isNotNull().size().isEqualTo(3);
        assertThat(modifiedCs).extracting("scpId", "port", "enabled").containsOnly(tuple("XNAT", 8104, true), tuple("CCIR", 8104, false), tuple("CCF", 8104, true));

        _beanPrefsTool.deletePrefC("XNAT");
        final List<BeanPrefsToolPreference> deletedCs = _beanPrefsTool.getPrefCs();

        assertThat(deletedCs).isNotNull().size().isEqualTo(2);
        assertThat(deletedCs).extracting("scpId", "port", "enabled").containsOnly(tuple("CCIR", 8104, false), tuple("CCF", 8104, true));

        // {'XNAT':'192.168.10.1,'CCIR':'192.168.10.100'}
        final Map<String, String> prefDs = _beanPrefsTool.getPrefDs();

        assertThat(prefDs).isNotNull().size().isEqualTo(2);
        assertThat(prefDs).containsEntry("XNAT", "192.168.10.1").containsEntry("CCIR", "192.168.10.100");
        assertThat(prefDs.get("XNAT")).isEqualTo("192.168.10.1");
        assertThat(prefDs.get("CCIR")).isEqualTo("192.168.10.100");

        _beanPrefsTool.deletePrefD("XNAT");
        _beanPrefsTool.setPrefD("CCF", "192.168.10.255");
        final Map<String, String> modifiedDs = _beanPrefsTool.getPrefDs();

        assertThat(modifiedDs).isNotNull().size().isEqualTo(2);
        assertThat(modifiedDs).containsEntry("CCIR", "192.168.10.100").containsEntry("CCF", "192.168.10.255");
        assertThat(_beanPrefsTool.getPrefD("CCIR")).isEqualTo("192.168.10.100");
        assertThat(_beanPrefsTool.getPrefD("CCF")).isEqualTo("192.168.10.255");

        // {'XNAT':{'port':8104,'scpId':'XNAT','identifier':'XNAT','aeTitle':'XNAT','enabled':true},
        //  'CCIR':{'port':8104,'scpId':'CCIR','identifier':'CCIR','aeTitle':'CCIR','enabled':true}}
        final Map<String, BeanPrefsToolPreference> prefEs = _beanPrefsTool.getPrefEs();
        assertThat(prefEs).isNotNull()
                          .size().isEqualTo(2).returnToMap()
                          .hasEntrySatisfying("XNAT", new BeanPrefsToolPreferenceCondition("XNAT", 8104, true))
                          .hasEntrySatisfying("CCIR", new BeanPrefsToolPreferenceCondition("CCIR", 8104, true));

        // []
        final List<String> prefFs = _beanPrefsTool.getPrefFs();

        assertThat(prefFs).isNotNull().size().isEqualTo(0);

        bean.set("['one','two','three']", "prefFs");
        prefFs.addAll(_beanPrefsTool.getPrefFs());

        assertThat(prefFs).size().isEqualTo(3).returnToIterable().containsExactlyInAnyOrder("one", "two", "three");

        final Map<String, Object> preferences = _beanPrefsTool.getPreferences();

        assertThat(preferences).size().isEqualTo(6);

        validateBeanCache(_beanPrefsTool.getPreferenceBean(), preferences);
    }

    @Ignore
    @Test
    public void testCreateTool() {
    }

    @Ignore
    @Test
    public void testSimpleToolAndPreference() {
    }

    /**
     * Tests that preferences with the same name in a different tool don't update together.
     */
    @Ignore
    @Test
    public void testMultipleToolsAndPreference() {
    }

    @Ignore
    @Test
    public void testToolWithScope() {}

    @Ignore
    @Test
    public void testLoadSiteConfigurationProperties() throws IOException, UnknownToolId {
        final Properties properties = new Properties();
        properties.load(Properties.class.getResourceAsStream("/org/nrg/prefs/configuration/siteConfiguration.properties"));
        assertThat(properties).isNotNull().isNotEmpty();

        final Map<String, String> defaults = new HashMap<>();
        for (final String property : properties.stringPropertyNames()) {
            defaults.put(property, properties.getProperty(property));
        }
        assertThat(defaults).isNotNull().isNotEmpty();

        // _service.createTool("siteConfig", "Site Configuration", "This is the main tool for mapping the site configuration", defaults, false, AbstractPreferenceBean.class.getName(), null);
        // Assert.assertEquals("true", _service.getPreferenceValue("siteConfig", "enableDicomReceiver"));
        // Assert.assertEquals("org.nrg.xnat.utils.ChecksumsSiteConfigurationListener", _service.getPreferenceValue("siteConfig", "checksums.property.changed.listener"));
    }

    private void validateBeanCache(final PreferenceBean bean, final Map<String, Object> preferences) {
        @SuppressWarnings("unchecked") final List<Method> getters = Reflection.getGetters(bean.getClass(), AbstractPreferenceBean.class, ReflectionUtils.withAnnotation(NrgPreference.class));
        for (final Method getter : getters) {
            final String name = propertize(getter.getName());
            assertThat(preferences).containsKey(name);

            final Object value = preferences.get(name);
            assertThat(value).isNotNull();
            assertThat(getter.getReturnType()).isAssignableFrom(value.getClass());
            try {
                if (value instanceof List) {
                    //noinspection unchecked,rawtypes
                    assertThat(((List) value)).containsAll((List) getter.invoke(bean));
                } else {
                    assertThat(getter.invoke(bean)).isEqualTo(value);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                fail("An error of type %s error occurred invoking the %s() method on the %s class: %s", e.getClass().getName(), getter.getName(), bean.getClass().getName(), e.getMessage());
            }
        }
    }

    @Data
    @Accessors(prefix = "_")
    @EqualsAndHashCode(callSuper = false)
    private static class BeanPrefsToolPreferenceCondition extends Condition<BeanPrefsToolPreference> {
        BeanPrefsToolPreferenceCondition(final String scpId, final int port, final boolean enabled) {
            this(scpId, scpId, scpId, port, enabled);
        }

        BeanPrefsToolPreferenceCondition(final String scpId, final String identifier, final String aeTitle, final int port, final boolean enabled) {
            _preference = new BeanPrefsToolPreference();
            _preference.setScpId(scpId);
            _preference.setIdentifier(identifier);
            _preference.setAeTitle(aeTitle);
            _preference.setPort(port);
            _preference.setEnabled(enabled);
        }

        @Override
        public boolean matches(final BeanPrefsToolPreference preference) {
            if (preference == null) {
                return false;
            }
            _preference.setFileNamer(preference.getFileNamer());
            return _preference.equals(preference);
        }

        private final BeanPrefsToolPreference _preference;
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
