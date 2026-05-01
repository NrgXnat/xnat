package org.nrg.prefs.tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.prefs.configuration.FreeformPreferencesTestsConfiguration;
import org.nrg.prefs.tools.freeform.SiteConfigPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = FreeformPreferencesTestsConfiguration.class)
public class FreeformPreferencesTests {
    private SiteConfigPreferences _preferences;

    @Autowired
    public void setPreferences(final SiteConfigPreferences preferences) {
        _preferences = preferences;
    }

    @Test
    public void testMapFailure() {
        assertThrows(NullPointerException.class, () -> {
            _preferences.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        });
    }
}
