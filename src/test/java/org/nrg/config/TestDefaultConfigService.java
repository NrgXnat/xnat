package org.nrg.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.config.configuration.TestDefaultConfigServiceConfig;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.nrg.config.entities.Configuration.DISABLED_STRING;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestDefaultConfigServiceConfig.class)
public class TestDefaultConfigService {
    private static final String USERNAME            = "username";
    private static final String REASON              = "reason";
    private static final String DISABLE             = "disabling";
    private static final String TOOL                = "tool";
    private static final String PATH                = "path";
    private static final String PROJECT_1           = "project1";
    private static final String VALUE_1             = "value1";
    private static final String VALUE_2             = "value2";
    private static final String VALUE_3             = "value3";
    private static final String VALUE_4             = "value4";
    private static final String VALUE_6             = "value6";
    private static final String VALUE_7             = "value7";
    private static final String PROPERTIES_CONTENTS = "contents";
    private static final String PROPERTIES_STATUS   = "status";
    public static final  String PROPERTIES_VERSION  = "version";

    private ConfigService _configService;

    @Autowired
    public void setConfigService(final ConfigService configService) {
        _configService = configService;
    }

    @Test
    public void testVersionedSiteConfiguration() throws ConfigServiceException {
        final Configuration config1 = _configService.replaceConfig(USERNAME, REASON, TOOL, PATH, VALUE_1);
        assertThat(config1).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, VALUE_1).hasFieldOrPropertyWithValue(PROPERTIES_VERSION, 1);
        final Configuration config2 = _configService.replaceConfig(USERNAME, REASON, TOOL, PATH, VALUE_2);
        assertThat(config2).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, VALUE_2).hasFieldOrPropertyWithValue(PROPERTIES_VERSION, 2);
        final Configuration config3 = _configService.replaceConfig(USERNAME, REASON, TOOL, PATH, VALUE_3);
        assertThat(config3).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, VALUE_3).hasFieldOrPropertyWithValue(PROPERTIES_VERSION, 3);
        final Configuration config4 = _configService.replaceConfig(USERNAME, REASON, TOOL, PATH, VALUE_4);
        assertThat(config4).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, VALUE_4).hasFieldOrPropertyWithValue(PROPERTIES_VERSION, 4);

        _configService.disable(USERNAME, DISABLE, TOOL, PATH);
        final Configuration config5 = _configService.getConfig(TOOL, PATH);
        assertThat(config5).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_STATUS, DISABLED_STRING).hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, null);

        final Configuration retrieved1 = _configService.getConfigByVersion(TOOL, PATH, 1);
        assertThat(retrieved1).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, VALUE_1).hasFieldOrPropertyWithValue(PROPERTIES_VERSION, 1).isEqualTo(config1);

        _configService.replaceConfig(USERNAME, REASON, TOOL, PATH, VALUE_6);
        final Configuration config6 = _configService.getConfig(TOOL, PATH);
        assertThat(config6).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, VALUE_6).hasFieldOrPropertyWithValue(PROPERTIES_VERSION, 6);

        final Configuration config7 = _configService.replaceConfig(USERNAME, REASON, TOOL, PATH, VALUE_7);
        assertThat(config7).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, VALUE_7).hasFieldOrPropertyWithValue(PROPERTIES_VERSION, 7);
    }

    @Test
    public void testVersionedProjectConfiguration() throws ConfigServiceException {
        final Configuration config1 = _configService.replaceConfig(USERNAME, REASON, TOOL, PATH, VALUE_1, Scope.Project, PROJECT_1);
        assertThat(config1).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, VALUE_1).hasFieldOrPropertyWithValue(PROPERTIES_VERSION, 1);
        final Configuration config2 = _configService.replaceConfig(USERNAME, REASON, TOOL, PATH, VALUE_2, Scope.Project, PROJECT_1);
        assertThat(config2).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, VALUE_2).hasFieldOrPropertyWithValue(PROPERTIES_VERSION, 2);
        final Configuration config3 = _configService.replaceConfig(USERNAME, REASON, TOOL, PATH, VALUE_3, Scope.Project, PROJECT_1);
        assertThat(config3).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, VALUE_3).hasFieldOrPropertyWithValue(PROPERTIES_VERSION, 3);
        final Configuration config4 = _configService.replaceConfig(USERNAME, REASON, TOOL, PATH, VALUE_4, Scope.Project, PROJECT_1);
        assertThat(config4).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, VALUE_4).hasFieldOrPropertyWithValue(PROPERTIES_VERSION, 4);

        _configService.disable(USERNAME, DISABLE, TOOL, PATH, Scope.Project, PROJECT_1);
        final Configuration config5 = _configService.getConfig(TOOL, PATH, Scope.Project, PROJECT_1);
        assertThat(config5).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_STATUS, DISABLED_STRING).hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, null);

        final Configuration retrieved1 = _configService.getConfigByVersion(TOOL, PATH, 1, Scope.Project, PROJECT_1);
        assertThat(retrieved1).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, VALUE_1).hasFieldOrPropertyWithValue(PROPERTIES_VERSION, 1).isEqualTo(config1);

        _configService.replaceConfig(USERNAME, REASON, TOOL, PATH, VALUE_6, Scope.Project, PROJECT_1);
        final Configuration config6 = _configService.getConfig(TOOL, PATH, Scope.Project, PROJECT_1);
        assertThat(config6).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, VALUE_6).hasFieldOrPropertyWithValue(PROPERTIES_VERSION, 6);

        final Configuration config7 = _configService.replaceConfig(USERNAME, REASON, TOOL, PATH, VALUE_7, Scope.Project, PROJECT_1);
        assertThat(config7).isNotNull().hasFieldOrPropertyWithValue(PROPERTIES_CONTENTS, VALUE_7).hasFieldOrPropertyWithValue(PROPERTIES_VERSION, 7);
    }
}
