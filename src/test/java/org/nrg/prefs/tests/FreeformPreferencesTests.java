package org.nrg.prefs.tests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.prefs.configuration.FreeformPreferencesTestsConfiguration;
import org.nrg.prefs.tools.freeform.SiteConfigPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FreeformPreferencesTestsConfiguration.class)
@Rollback
@Transactional
public class FreeformPreferencesTests {
    private SiteConfigPreferences _preferences;

    @Autowired
    public void setPreferences(final SiteConfigPreferences preferences) {
        _preferences = preferences;
    }

    @Test(expected = NullPointerException.class)
    public void testMapFailure() {
        final Map<String, Object> preferences = _preferences.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        failBecauseExceptionWasNotThrown(NullPointerException.class);
    }
}
