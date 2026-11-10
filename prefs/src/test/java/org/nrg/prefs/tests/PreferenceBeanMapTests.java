/*
 * prefs: org.nrg.prefs.tests.PreferenceBeanMapTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.beans.PreferenceBean;
import org.nrg.prefs.configuration.ConcurrentPrefsTestsConfiguration;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.tools.basic.BasicEnum;
import org.nrg.prefs.tools.basic.BasicTestToolPreferenceBean;
import org.nrg.prefs.tools.concurrent.ConcurrentPrefsToolPreferenceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests preference beans through the {@link Map} interface.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ConcurrentPrefsTestsConfiguration.class)
public class PreferenceBeanMapTests {
    @Autowired
    public PreferenceBeanMapTests(final BasicTestToolPreferenceBean strictBean, final ConcurrentPrefsToolPreferenceBean relaxedBean, final NrgPreferenceService preferenceService) {
        _strictBean        = strictBean;
        _relaxedBean       = relaxedBean;
        _preferenceService = preferenceService;
    }

    @AfterEach
    public void resetValues() throws InvalidPreferenceName {
        if (!_preferenceService.hasPreference(_relaxedBean.getToolId(), "plainPref")) {
            _preferenceService.create(_relaxedBean.getToolId(), "plainPref", "plainInitial");
        }
        _relaxedBean.setPlainPref("plainInitial");
        _relaxedBean.invalidate("derivedPref");
    }

    @Test
    public void testMapViewMatchesGetters() {
        assertThat(_strictBean.keySet()).containsExactlyInAnyOrder("prefA", "prefB", "prefC");
        assertThat(_strictBean.get("prefA")).isEqualTo(_strictBean.getPrefA());
        assertThat(_strictBean.get("prefB")).isEqualTo(_strictBean.getPrefB());
        assertThat(_strictBean.get("prefC")).isInstanceOf(BasicEnum.class).isEqualTo(_strictBean.getPrefC());

        assertThat(_relaxedBean.keySet()).containsExactlyInAnyOrder("slowPref", "plainPref", "derivedPref");
        assertThat(_relaxedBean.get("plainPref")).isEqualTo(_relaxedBean.getPlainPref());
        assertThat(_relaxedBean.get("derivedPref")).isEqualTo(_relaxedBean.getDerivedPref());

        for (final AbstractPreferenceBean bean : List.of(_strictBean, _relaxedBean)) {
            final Map<String, Object> entries = bean.entrySet().stream().collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
            assertThat(entries.keySet()).isEqualTo(bean.keySet());
            for (final String key : bean.keySet()) {
                assertThat(bean.containsKey(key)).isTrue();
                assertThat(bean.get(key)).isEqualTo(entries.get(key)).isEqualTo(bean.getProperty(key));
            }
            assertThat(bean.size()).isEqualTo(bean.keySet().size());
            assertThat(bean.isEmpty()).isFalse();
            assertThat(bean.asProperties().stringPropertyNames()).isEqualTo(bean.keySet());
            final String first = bean.keySet().iterator().next();
            assertThat(bean.getPreferences(new HashSet<>(Arrays.asList(first, "notARealSetting")))).containsOnlyKeys(first).containsEntry(first, bean.get(first));
            assertThat(bean).isEqualTo(bean);
            assertThat(bean.hashCode()).isEqualTo(bean.hashCode());
        }
    }

    @Test
    public void testUnknownKeys() {
        for (final PreferenceBean bean : List.of(_strictBean, _relaxedBean)) {
            assertThat(bean.containsKey("notARealSetting")).isFalse();
            assertThat(bean.get("notARealSetting")).isNull();
            assertThat(bean.containsKey(null)).isFalse();
            assertThat(bean.get(null)).isNull();
        }
    }

    @Test
    public void testRelaxedBeanListsStoredSettingWithoutGetter() throws InvalidPreferenceName {
        try {
            _relaxedBean.create("extraValue", "extraPref");
            assertThat(_relaxedBean.keySet()).contains("extraPref");
            assertThat(_relaxedBean.get("extraPref")).isEqualTo("extraValue");
            assertThat(_relaxedBean.entrySet()).contains(Map.entry("extraPref", "extraValue"));
        } finally {
            _relaxedBean.delete("extraPref");
        }
        assertThat(_relaxedBean.containsKey("extraPref")).isFalse();
        assertThat(_relaxedBean.get("extraPref")).isNull();
    }

    @Test
    public void testRelaxedBeanKeepsListingSettingWhoseGetterReturnsNull() throws InvalidPreferenceName {
        // The stored value is deleted without going through this bean, as when another node deletes it.
        _preferenceService.deletePreference(_relaxedBean.getToolId(), "plainPref");
        _relaxedBean.invalidate("plainPref");
        assertThat(_relaxedBean.get("plainPref")).isNull();
        assertThat(_relaxedBean.keySet()).contains("plainPref");
        assertThat(_relaxedBean.entrySet()).anySatisfy(entry -> {
            assertThat(entry.getKey()).isEqualTo("plainPref");
            assertThat(entry.getValue()).isNull();
        });
    }

    @Test
    public void testSetAndInvalidateThroughMap() throws InvalidPreferenceName {
        _relaxedBean.setPlainPref("setThroughBean");
        assertThat(_relaxedBean.get("plainPref")).isEqualTo("setThroughBean");

        _preferenceService.setPreferenceValue(_relaxedBean.getToolId(), "plainPref", "storedElsewhere");
        assertThat(_relaxedBean.get("plainPref")).isEqualTo("setThroughBean");
        _relaxedBean.invalidate("plainPref");
        assertThat(_relaxedBean.get("plainPref")).isEqualTo("storedElsewhere");
    }

    @Test
    public void testSerializesAsItsEntries() throws IOException {
        final ObjectMapper                     mapper = new ObjectMapper();
        final TypeReference<Map<String, Object>> type = new TypeReference<>() {};
        for (final AbstractPreferenceBean bean : List.of(_strictBean, _relaxedBean)) {
            final Map<String, Object> entries = bean.entrySet().stream().collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
            assertThat(mapper.readValue(mapper.writeValueAsString(bean), type)).isEqualTo(mapper.readValue(mapper.writeValueAsString(entries), type));
        }
    }

    private final BasicTestToolPreferenceBean       _strictBean;
    private final ConcurrentPrefsToolPreferenceBean _relaxedBean;
    private final NrgPreferenceService              _preferenceService;
}
