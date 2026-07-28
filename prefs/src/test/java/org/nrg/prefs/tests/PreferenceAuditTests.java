/*
 * prefs: org.nrg.prefs.tests.PreferenceAuditTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tests;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.framework.orm.hibernate.audit.NrgRevisionEntity;
import org.nrg.prefs.configuration.PreferenceServiceTestsConfiguration;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.prefs.services.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that preference changes are audited: every insert, update, and delete gets a revision in the preference
 * audit table, with the acting username captured on the {@link NrgRevisionEntity revision}.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PreferenceServiceTestsConfiguration.class)
@Slf4j
public class PreferenceAuditTests {
    private final ToolService                _toolService;
    private final PreferenceService          _prefService;
    private final SessionFactory             _sessionFactory;
    private final PlatformTransactionManager _transactionManager;

    @Autowired
    public PreferenceAuditTests(final ToolService toolService, final PreferenceService prefService, final SessionFactory sessionFactory, final PlatformTransactionManager transactionManager) {
        _toolService        = toolService;
        _prefService        = prefService;
        _sessionFactory     = sessionFactory;
        _transactionManager = transactionManager;
    }

    @AfterEach
    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void auditsCreateUpdateAndDeleteWithUsername() {
        authenticateAs("kate");
        final Preference preference = createPreference("auditTool1", "auditPref1", "first value");
        final long       id         = preference.getId();

        authenticateAs("michael");
        preference.setValue("second value");
        _prefService.update(preference);
        _prefService.delete(preference);

        inTransaction(reader -> {
            final List<Number> revisions = reader.getRevisions(Preference.class, id);
            assertThat(revisions).hasSize(3);

            final Preference createRevision = reader.find(Preference.class, id, revisions.get(0));
            assertThat(createRevision).isNotNull();
            assertThat(createRevision.getValue()).isEqualTo("first value");
            assertThat(reader.findRevision(NrgRevisionEntity.class, revisions.get(0)).getUsername()).isEqualTo("kate");

            final Preference updateRevision = reader.find(Preference.class, id, revisions.get(1));
            assertThat(updateRevision).isNotNull();
            assertThat(updateRevision.getValue()).isEqualTo("second value");
            assertThat(reader.findRevision(NrgRevisionEntity.class, revisions.get(1)).getUsername()).isEqualTo("michael");

            // The delete revision: the entity no longer exists at that revision.
            assertThat(reader.find(Preference.class, id, revisions.get(2))).isNull();
            assertThat(reader.findRevision(NrgRevisionEntity.class, revisions.get(2)).getUsername()).isEqualTo("michael");
        });
    }

    @Test
    public void doesNotAuditNoOpReSaves() throws Exception {
        // The admin UI submits every field on the page, not just the edited one, so re-saving an identical value must
        // not record a revision — otherwise one save buries the real change under a revision per preference.
        final Tool tool = _toolService.newEntity();
        tool.setToolId("noopTool");
        tool.setToolName("noopTool");
        _toolService.create(tool);

        _prefService.setPreference("noopTool", "noopPref", "same value");
        _prefService.setPreference("noopTool", "noopPref", "same value"); // no-op: must not create a revision
        _prefService.setPreference("noopTool", "noopPref", "same value"); // no-op

        final long id = _prefService.getPreference("noopTool", "noopPref").getId();

        // only the initial create is recorded, not the two identical re-saves
        inTransaction(reader -> assertThat(reader.getRevisions(Preference.class, id)).hasSize(1));

        // a genuine change still records a revision
        _prefService.setPreference("noopTool", "noopPref", "new value");
        inTransaction(reader -> assertThat(reader.getRevisions(Preference.class, id)).hasSize(2));
    }

    @Test
    public void recordsNullUsernameOutsideAuthenticatedContext() {
        final Preference preference = createPreference("auditTool2", "auditPref2", "system value");

        inTransaction(reader -> {
            final List<Number> revisions = reader.getRevisions(Preference.class, preference.getId());
            assertThat(revisions).hasSize(1);
            assertThat(reader.findRevision(NrgRevisionEntity.class, revisions.get(0)).getUsername()).isNull();
        });
    }

    private static void authenticateAs(final String username) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(username, "credentials"));
    }

    private Preference createPreference(final String toolId, final String name, final String value) {
        final Tool tool = _toolService.newEntity();
        tool.setToolId(toolId);
        tool.setToolName(toolId);
        _toolService.create(tool);

        final Preference preference = _prefService.newEntity();
        preference.setTool(tool);
        preference.setName(name);
        preference.setValue(value);
        _prefService.create(preference);
        return preference;
    }

    private void inTransaction(final Consumer<AuditReader> assertions) {
        new TransactionTemplate(_transactionManager).execute(status -> {
            assertions.accept(AuditReaderFactory.get(_sessionFactory.getCurrentSession()));
            return null;
        });
    }
}
