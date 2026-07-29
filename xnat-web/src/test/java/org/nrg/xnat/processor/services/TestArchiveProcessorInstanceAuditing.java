/*
 * web: org.nrg.xnat.processor.services.TestArchiveProcessorInstanceAuditing
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.processor.services;

import org.hibernate.SessionFactory;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.audit.NrgRevisionEntity;
import org.nrg.xnat.config.TestArchiveProcessorInstanceServiceConfig;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that archive processor configuration changes are audited with correct element-collection membership and
 * the acting username. The write flow mirrored here is the one the REST API actually uses —
 * {@code ArchiveProcessorInstanceApi.updateSiteProcessor} loads the processor, copies the JSON body onto it with
 * {@link ArchiveProcessorInstance#update(ArchiveProcessorInstance)} (which replaces changed collections wholesale),
 * and saves through the service — because that detached-replacement shape is exactly what used to lose collection
 * removals from the audit history before {@code ArchiveProcessorInstanceDAO.update} switched to merge.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestArchiveProcessorInstanceServiceConfig.class)
public class TestArchiveProcessorInstanceAuditing {
    private static final String PROCESSOR_CLASS = "org.nrg.xnat.processors.TestProcessor";

    @Autowired
    public void setArchiveProcessorInstanceService(final ArchiveProcessorInstanceService service) {
        _service = service;
    }

    @Autowired
    public void setSessionFactory(final SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }

    @Autowired
    public void setTransactionManager(final PlatformTransactionManager transactionManager) {
        _transactionManager = transactionManager;
    }

    @After
    public void cleanup() {
        // This context — and its database — is shared with TestArchiveProcessInstanceService, which asserts exact
        // table contents, so leave the table as we found it. These are hard deletes: the entity is not @Auditable.
        for (final ArchiveProcessorInstance instance : _created) {
            _service.delete(instance);
        }
        _created.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    public void auditsScpWhitelistRemovalsThroughTheApiUpdateFlow() throws Exception {
        authenticateAs("installing-admin");
        final ArchiveProcessorInstance created = new ArchiveProcessorInstance("Site processor", Scope.Site.code(),
                                                                              new HashSet<>(Arrays.asList("XNAT:8104", "CCIR:8104")),
                                                                              new HashSet<>(), 10, "location",
                                                                              new HashMap<>(), PROCESSOR_CLASS);
        _service.create(created);
        _created.add(created);
        final long id = created.getId();

        // Mirror ArchiveProcessorInstanceApi.updateSiteProcessor: load, copy the submitted body on, save.
        authenticateAs("changing-admin");
        final ArchiveProcessorInstance existing  = _service.findSiteProcessorById(id);
        final ArchiveProcessorInstance submitted = new ArchiveProcessorInstance();
        submitted.setScpWhitelist(new HashSet<>(Collections.singleton("XNAT:8104"))); // CCIR:8104 removed
        submitted.setPriority(10);
        submitted.setParameters(new HashMap<>());
        assertThat(existing.update(submitted)).isTrue();
        _service.update(existing);

        inTransaction(reader -> {
            final List<Number> revisions = reader.getRevisions(ArchiveProcessorInstance.class, id);
            assertThat(revisions).hasSize(2);

            final ArchiveProcessorInstance asInstalled = reader.find(ArchiveProcessorInstance.class, id, revisions.get(0));
            assertThat(asInstalled).isNotNull();
            assertThat(asInstalled.getScpWhitelist()).containsExactlyInAnyOrder("XNAT:8104", "CCIR:8104");
            assertThat(usernameFor(reader, revisions.get(0))).isEqualTo("installing-admin");

            // The removal is the part the pre-merge write path lost from the audit history.
            final ArchiveProcessorInstance asChanged = reader.find(ArchiveProcessorInstance.class, id, revisions.get(1));
            assertThat(asChanged).isNotNull();
            assertThat(asChanged.getScpWhitelist()).containsExactly("XNAT:8104");
            assertThat(usernameFor(reader, revisions.get(1))).isEqualTo("changing-admin");
        });
    }

    @Test
    public void auditsEnabledToggleThroughTheApiUpdateFlow() throws Exception {
        authenticateAs("installing-admin");
        final ArchiveProcessorInstance created = new ArchiveProcessorInstance("Toggled processor", Scope.Site.code(),
                                                                              new HashSet<>(Collections.singleton("XNAT:8104")),
                                                                              new HashSet<>(), 20, "location",
                                                                              new HashMap<>(), PROCESSOR_CLASS);
        _service.create(created);
        _created.add(created);
        final long id = created.getId();

        // A PUT that only disables the processor. The enabled flag gates which processors run (the DAO's
        // enabled-site-processor queries filter on it), and ArchiveProcessorInstance.update copies it — without
        // @AuditOverride the resulting revision would be byte-identical to its predecessor, recording that something
        // changed but not what.
        authenticateAs("disabling-admin");
        final ArchiveProcessorInstance existing  = _service.findSiteProcessorById(id);
        final ArchiveProcessorInstance submitted = new ArchiveProcessorInstance();
        submitted.setEnabled(false);
        submitted.setPriority(20);
        submitted.setParameters(new HashMap<>());
        submitted.setScpWhitelist(new HashSet<>(Collections.singleton("XNAT:8104")));
        assertThat(existing.update(submitted)).isTrue();
        _service.update(existing);

        inTransaction(reader -> {
            final List<Number> revisions = reader.getRevisions(ArchiveProcessorInstance.class, id);
            assertThat(revisions).hasSize(2);
            assertThat(reader.find(ArchiveProcessorInstance.class, id, revisions.get(0)).isEnabled()).isTrue();

            final ArchiveProcessorInstance disabled = reader.find(ArchiveProcessorInstance.class, id, revisions.get(1));
            assertThat(disabled.isEnabled()).isFalse();
            assertThat(disabled.getScpWhitelist()).containsExactly("XNAT:8104"); // unchanged collection survives the merge
            assertThat(usernameFor(reader, revisions.get(1))).isEqualTo("disabling-admin");
        });
    }

    private static void authenticateAs(final String username) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(username, "credentials"));
    }

    private static String usernameFor(final AuditReader reader, final Number revision) {
        return reader.findRevision(NrgRevisionEntity.class, revision).getUsername();
    }

    private void inTransaction(final Consumer<AuditReader> assertions) {
        new TransactionTemplate(_transactionManager).execute(status -> {
            assertions.accept(AuditReaderFactory.get(_sessionFactory.getCurrentSession()));
            return null;
        });
    }

    private final List<ArchiveProcessorInstance> _created = new ArrayList<>();

    private ArchiveProcessorInstanceService _service;
    private SessionFactory                  _sessionFactory;
    private PlatformTransactionManager      _transactionManager;
}
