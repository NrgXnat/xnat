/*
 * web: org.nrg.xnat.initialization.tasks.MigrateRetiredPetMrSeparationTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import org.assertj.core.api.AbstractStringAssert;
import org.junit.Before;
import org.junit.Test;
import org.nrg.config.entities.Configuration;
import org.nrg.config.entities.ConfigurationData;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.preferences.HandlePetMr;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers {@link MigrateRetiredPetMrSeparation}'s migration of project-level {@code separatePETMR} configurations.
 *
 * <p>The invariant every test ultimately checks is the same: once the task has run, no project configuration's
 * <em>latest</em> version has the retired "separate" value, whether or not that configuration is disabled. Older
 * versions in the history are left alone: they're immutable audit records, and only the latest version is ever read.</p>
 *
 * <p>The {@link ConfigService} mock is backed by an in-memory version history rather than plain canned returns, since
 * the migration's correctness is a statement about the resulting stored state, not about which calls it made. The
 * fake mirrors {@code DefaultConfigService} in the ways that matter here: {@code getConfigsByTool} returns every
 * version of every configuration for the tool, {@code replaceConfig} appends a new enabled version, and
 * {@code disable} appends a further version carrying the latest contents with a disabled status. It always versions,
 * where the real service updates in place for unversioned configurations; that distinction doesn't affect which
 * configurations this task selects or what value they end up with.</p>
 */
public class MigrateRetiredPetMrSeparationTest {
    private static final String ADMIN     = "admin";
    private static final String PROJECT_A = "PROJECT_A";
    private static final String PROJECT_B = "PROJECT_B";
    private static final String PROJECT_C = "PROJECT_C";
    private static final String PROJECT_D = "PROJECT_D";
    private static final String PROJECT_E = "PROJECT_E";
    private static final String PROJECT_F = "PROJECT_F";

    private final List<Configuration> _stored = new ArrayList<>();

    private long                          _clock;
    private SiteConfigPreferences         _preferences;
    private ConfigService                 _configService;
    private MigrateRetiredPetMrSeparation _task;

    @Before
    public void setUp() throws Exception {
        _stored.clear();
        _clock = 0;

        _preferences = mock(SiteConfigPreferences.class);
        when(_preferences.getSitewidePetMr()).thenReturn(HandlePetMr.PETMR);

        final XnatUserProvider primaryAdminUserProvider = mock(XnatUserProvider.class);
        when(primaryAdminUserProvider.getLogin()).thenReturn(ADMIN);

        // The prearchive reset is exercised elsewhere: here it just needs to not blow up.
        final NamedParameterJdbcTemplate template = mock(NamedParameterJdbcTemplate.class);
        when(template.update(anyString(), anyMap())).thenReturn(0);

        _configService = mock(ConfigService.class);
        when(_configService.getConfigsByTool(HandlePetMr.SEPARATE_PET_MR)).thenAnswer(invocation -> new ArrayList<>(_stored));
        when(_configService.replaceConfig(anyString(), anyString(), eq(HandlePetMr.SEPARATE_PET_MR), eq(HandlePetMr.CONFIG), anyString(), any(Scope.class), anyString()))
                .thenAnswer(invocation -> store(invocation.getArgument(5), invocation.getArgument(6), invocation.getArgument(4), Configuration.ENABLED_STRING));
        doAnswer(invocation -> {
            final Scope         scope    = invocation.getArgument(4);
            final String        entityId = invocation.getArgument(5);
            final Configuration latest   = latest(scope, entityId);
            if (latest == null) {
                throw new ConfigServiceException("Couldn't find the configuration for scope " + scope + " and entity ID " + entityId + ".");
            }
            if (!Configuration.DISABLED_STRING.equals(latest.getStatus())) {
                store(scope, entityId, latest.getContents(), Configuration.DISABLED_STRING);
            }
            return null;
        }).when(_configService).disable(anyString(), anyString(), eq(HandlePetMr.SEPARATE_PET_MR), eq(HandlePetMr.CONFIG), any(Scope.class), anyString());

        _task = new MigrateRetiredPetMrSeparation(_preferences, _configService, primaryAdminUserProvider, template);
    }

    @Test
    public void noConfigurationsAtAllIsANoOp() throws Exception {
        _task.callImpl();

        assertThat(_stored).isEmpty();
        verifyNothingWasWritten();
    }

    @Test
    public void nullConfigurationListIsANoOp() throws Exception {
        // The config service returns null rather than an empty list when nothing matches the tool.
        when(_configService.getConfigsByTool(HandlePetMr.SEPARATE_PET_MR)).thenReturn(null);

        _task.callImpl();

        verifyNothingWasWritten();
    }

    @Test
    public void projectSetOnceToSeparateIsMigratedToPetMr() throws Exception {
        storeProject(PROJECT_A, HandlePetMr.SEPARATE);

        _task.callImpl();

        assertThat(versions(PROJECT_A)).hasSize(2);
        assertThatLatest(PROJECT_A).isEqualTo(HandlePetMr.PETMR);
        assertThat(latestFor(PROJECT_A).getStatus()).isEqualTo(Configuration.ENABLED_STRING);
        assertThat(latestFor(PROJECT_A).getXnatUser()).isEqualTo(ADMIN);
    }

    @Test
    public void projectSetOnceToPetMrIsLeftAlone() throws Exception {
        storeProject(PROJECT_A, HandlePetMr.PETMR);

        _task.callImpl();

        assertThat(versions(PROJECT_A)).hasSize(1);
        assertThatLatest(PROJECT_A).isEqualTo(HandlePetMr.PETMR);
        verifyNothingWasWritten();
    }

    @Test
    public void projectSetOnceToPetIsLeftAlone() throws Exception {
        storeProject(PROJECT_A, HandlePetMr.PET);

        _task.callImpl();

        assertThat(versions(PROJECT_A)).hasSize(1);
        assertThatLatest(PROJECT_A).isEqualTo(HandlePetMr.PET);
        verifyNothingWasWritten();
    }

    /**
     * The stored value is whatever was submitted through the UI or REST API, so it can differ from the canonical
     * value in case and whitespace and still mean "separate".
     */
    @Test
    public void separateIsRecognizedRegardlessOfCaseAndWhitespace() throws Exception {
        storeProject(PROJECT_A, "SEPARATE");
        storeProject(PROJECT_B, " Separate ");
        storeProject(PROJECT_C, "sep arate");

        _task.callImpl();

        assertThatLatest(PROJECT_A).isEqualTo(HandlePetMr.PETMR);
        assertThatLatest(PROJECT_B).isEqualTo(HandlePetMr.PETMR);
        assertThatLatest(PROJECT_C).isEqualTo(HandlePetMr.PETMR);
        assertNoLatestProjectConfigurationIsSeparate();
    }

    /**
     * Only the latest version matters. A project that was set to "separate" at some point but has since been changed
     * is already correct and must not be given a spurious new version.
     */
    @Test
    public void projectSwitchedAwayFromSeparateIsLeftAlone() throws Exception {
        storeProject(PROJECT_A, HandlePetMr.SEPARATE);
        storeProject(PROJECT_A, HandlePetMr.PET);

        _task.callImpl();

        assertThat(versions(PROJECT_A)).hasSize(2);
        assertThatLatest(PROJECT_A).isEqualTo(HandlePetMr.PET);
        verifyNothingWasWritten();
    }

    @Test
    public void projectSwitchedBackToSeparateIsMigrated() throws Exception {
        storeProject(PROJECT_A, HandlePetMr.PETMR);
        storeProject(PROJECT_A, HandlePetMr.PET);
        storeProject(PROJECT_A, HandlePetMr.SEPARATE);

        _task.callImpl();

        assertThat(versions(PROJECT_A)).hasSize(4);
        assertThatLatest(PROJECT_A).isEqualTo(HandlePetMr.PETMR);
        assertThat(latestFor(PROJECT_A).getStatus()).isEqualTo(Configuration.ENABLED_STRING);
    }

    /**
     * A disabled configuration is still read back on the next enable, and {@code getConfigsByTool} returns it either
     * way, so it has to be migrated too — and it has to stay disabled afterwards. Since the config service can only
     * write a new version as enabled, the task re-disables the configuration, which leaves one further version behind.
     */
    @Test
    public void disabledSeparateConfigurationIsMigratedAndStaysDisabled() throws Exception {
        storeProject(PROJECT_A, HandlePetMr.SEPARATE, Configuration.DISABLED_STRING);

        _task.callImpl();

        assertThat(versions(PROJECT_A)).hasSize(3);
        assertThatLatest(PROJECT_A).isEqualTo(HandlePetMr.PETMR);
        assertThat(latestFor(PROJECT_A).getStatus()).isEqualTo(Configuration.DISABLED_STRING);
        assertNoLatestProjectConfigurationIsSeparate();
    }

    @Test
    public void enabledSeparateConfigurationIsNotDisabled() throws Exception {
        storeProject(PROJECT_A, HandlePetMr.SEPARATE);

        _task.callImpl();

        verify(_configService, never()).disable(anyString(), anyString(), anyString(), anyString(), any(Scope.class), anyString());
        assertThat(latestFor(PROJECT_A).getStatus()).isEqualTo(Configuration.ENABLED_STRING);
    }

    /**
     * The disabled status alone isn't a reason to migrate: a disabled configuration whose latest value is already
     * something other than "separate" is left as it is, disabled.
     */
    @Test
    public void disabledConfigurationThatIsNoLongerSeparateIsLeftAlone() throws Exception {
        storeProject(PROJECT_A, HandlePetMr.SEPARATE);
        storeProject(PROJECT_A, HandlePetMr.PETMR, Configuration.DISABLED_STRING);

        _task.callImpl();

        assertThat(versions(PROJECT_A)).hasSize(2);
        assertThatLatest(PROJECT_A).isEqualTo(HandlePetMr.PETMR);
        assertThat(latestFor(PROJECT_A).getStatus()).isEqualTo(Configuration.DISABLED_STRING);
        verifyNothingWasWritten();
    }

    /**
     * The site-wide setting lives in the {@code sitewidePetMr} preference, which is where {@link HandlePetMr} reads it
     * from, so the project migration deliberately ignores site-scoped configurations. Nothing writes a site-scoped
     * {@code separatePETMR} configuration, but if one exists it's inert, and rewriting it under {@link Scope#Project}
     * — the only scope the task writes with — would create a bogus project configuration.
     */
    @Test
    public void siteScopedConfigurationIsLeftToTheSitePreferenceMigration() throws Exception {
        when(_preferences.getSitewidePetMr()).thenReturn(HandlePetMr.SEPARATE);
        store(Scope.Site, null, HandlePetMr.SEPARATE, Configuration.ENABLED_STRING);

        _task.callImpl();

        assertThat(_stored).hasSize(1);
        verify(_preferences).setSitewidePetMr(HandlePetMr.PETMR);
        verifyNothingWasWritten();
    }

    @Test
    public void siteWidePreferenceThatIsNotSeparateIsLeftAlone() throws Exception {
        when(_preferences.getSitewidePetMr()).thenReturn(HandlePetMr.PET);

        _task.callImpl();

        verify(_preferences, never()).setSitewidePetMr(anyString());
    }

    /**
     * A project-scoped configuration with no entity ID can't be addressed for a write, so it's skipped rather than
     * failing the task or, worse, being written back with a null project ID.
     */
    @Test
    public void projectScopedConfigurationWithoutAnEntityIdIsSkipped() throws Exception {
        store(Scope.Project, null, HandlePetMr.SEPARATE, Configuration.ENABLED_STRING);
        store(Scope.Project, "  ", HandlePetMr.SEPARATE, Configuration.ENABLED_STRING);

        _task.callImpl();

        assertThat(_stored).hasSize(2);
        verifyNothingWasWritten();
    }

    /**
     * A configuration the service refuses to write is logged and skipped: {@link HandlePetMr#get(String)} remaps the
     * value at read time anyway, so the remaining projects are worth migrating regardless.
     */
    @Test
    public void failureOnOneProjectDoesNotStopTheOthers() throws Exception {
        storeProject(PROJECT_A, HandlePetMr.SEPARATE);
        storeProject(PROJECT_B, HandlePetMr.SEPARATE);
        doThrow(new ConfigServiceException("Nope.")).when(_configService)
                                                    .replaceConfig(anyString(), anyString(), eq(HandlePetMr.SEPARATE_PET_MR), eq(HandlePetMr.CONFIG), anyString(), eq(Scope.Project), eq(PROJECT_A));

        _task.callImpl();

        assertThatLatest(PROJECT_A).isEqualTo(HandlePetMr.SEPARATE);
        assertThatLatest(PROJECT_B).isEqualTo(HandlePetMr.PETMR);
    }

    @Test
    public void runningTheMigrationAgainChangesNothing() throws Exception {
        storeProject(PROJECT_A, HandlePetMr.SEPARATE);
        storeProject(PROJECT_B, HandlePetMr.SEPARATE, Configuration.DISABLED_STRING);
        storeProject(PROJECT_C, HandlePetMr.PET);

        _task.callImpl();
        final List<Configuration> afterFirstRun = new ArrayList<>(_stored);

        _task.callImpl();

        assertThat(_stored).isEqualTo(afterFirstRun);
        assertNoLatestProjectConfigurationIsSeparate();
    }

    /**
     * The whole point of the task, over a corpus covering every shape of stored history: afterwards, no project's
     * latest configuration is set to the retired value, and every project that was already correct is untouched.
     */
    @Test
    public void noProjectIsLeftSetToSeparate() throws Exception {
        // Set once, to each of the possible values.
        storeProject(PROJECT_A, HandlePetMr.SEPARATE);
        storeProject(PROJECT_B, HandlePetMr.PETMR);
        storeProject(PROJECT_C, HandlePetMr.PET);
        // Was separate, but has since been changed.
        storeProject(PROJECT_D, HandlePetMr.SEPARATE);
        storeProject(PROJECT_D, HandlePetMr.PETMR);
        // Was something else, but was set back to separate.
        storeProject(PROJECT_E, HandlePetMr.PET);
        storeProject(PROJECT_E, HandlePetMr.SEPARATE);
        // Set to separate, then disabled.
        storeProject(PROJECT_F, HandlePetMr.SEPARATE);
        storeProject(PROJECT_F, HandlePetMr.SEPARATE, Configuration.DISABLED_STRING);
        // And the site-wide setting, which lives in the preference rather than in a configuration.
        when(_preferences.getSitewidePetMr()).thenReturn(HandlePetMr.SEPARATE);

        _task.callImpl();

        assertNoLatestProjectConfigurationIsSeparate();

        assertThatLatest(PROJECT_A).isEqualTo(HandlePetMr.PETMR);
        assertThatLatest(PROJECT_D).isEqualTo(HandlePetMr.PETMR);
        assertThatLatest(PROJECT_E).isEqualTo(HandlePetMr.PETMR);
        assertThatLatest(PROJECT_F).isEqualTo(HandlePetMr.PETMR);

        // The projects that were already fine keep their value and their history.
        assertThat(versions(PROJECT_B)).hasSize(1);
        assertThatLatest(PROJECT_B).isEqualTo(HandlePetMr.PETMR);
        assertThat(versions(PROJECT_C)).hasSize(1);
        assertThatLatest(PROJECT_C).isEqualTo(HandlePetMr.PET);
        assertThat(versions(PROJECT_D)).hasSize(2);

        // Disabling survives the migration.
        assertThat(latestFor(PROJECT_F).getStatus()).isEqualTo(Configuration.DISABLED_STRING);
        assertThat(latestFor(PROJECT_A).getStatus()).isEqualTo(Configuration.ENABLED_STRING);

        verify(_preferences).setSitewidePetMr(HandlePetMr.PETMR);
    }

    private void assertNoLatestProjectConfigurationIsSeparate() {
        final Map<String, Configuration> latest = _stored.stream()
                                                         .filter(configuration -> configuration.getScope() == Scope.Project && configuration.getEntityId() != null)
                                                         .collect(Collectors.groupingBy(Configuration::getEntityId,
                                                                                        Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(Configuration::getCreated)), Optional::get)));
        assertThat(latest).allSatisfy((projectId, configuration) -> assertThat(configuration.getContents()).isNotEqualToIgnoringCase(HandlePetMr.SEPARATE));
    }

    private void verifyNothingWasWritten() throws ConfigServiceException {
        verify(_configService, never()).replaceConfig(anyString(), anyString(), anyString(), anyString(), anyString(), any(Scope.class), anyString());
        verify(_configService, never()).disable(anyString(), anyString(), anyString(), anyString(), any(Scope.class), anyString());
    }

    private AbstractStringAssert<?> assertThatLatest(final String projectId) {
        return assertThat(latestFor(projectId).getContents());
    }

    private Configuration latestFor(final String projectId) {
        final Configuration latest = latest(Scope.Project, projectId);
        assertThat(latest).as("configuration for project %s", projectId).isNotNull();
        return latest;
    }

    private List<Configuration> versions(final String projectId) {
        return _stored.stream()
                      .filter(configuration -> configuration.getScope() == Scope.Project && Objects.equals(projectId, configuration.getEntityId()))
                      .collect(Collectors.toList());
    }

    private Configuration latest(final Scope scope, final String entityId) {
        return _stored.stream()
                      .filter(configuration -> configuration.getScope() == scope && Objects.equals(entityId, configuration.getEntityId()))
                      .max(Comparator.comparing(Configuration::getCreated))
                      .orElse(null);
    }

    private void storeProject(final String projectId, final String contents) {
        store(Scope.Project, projectId, contents, Configuration.ENABLED_STRING);
    }

    private void storeProject(final String projectId, final String contents, final String status) {
        store(Scope.Project, projectId, contents, status);
    }

    private Configuration store(final Scope scope, final String entityId, final String contents, final String status) {
        final Configuration configuration = new Configuration();
        configuration.setTool(HandlePetMr.SEPARATE_PET_MR);
        configuration.setPath(HandlePetMr.CONFIG);
        configuration.setScope(scope);
        configuration.setEntityId(entityId);
        configuration.setConfigData(new ConfigurationData(contents));
        configuration.setStatus(status);
        configuration.setXnatUser(ADMIN);
        configuration.setVersion(_stored.stream()
                                        .filter(existing -> existing.getScope() == scope && Objects.equals(entityId, existing.getEntityId()))
                                        .mapToInt(Configuration::getVersion)
                                        .max()
                                        .orElse(0) + 1);
        // Configurations are ordered by creation date, so each one has to land strictly after the last.
        configuration.setCreated(new Date(++_clock));
        _stored.add(configuration);
        return configuration;
    }
}
