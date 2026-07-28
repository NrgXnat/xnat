/*
 * web: org.nrg.dcm.scp.TestDicomSCPInstanceAuditing
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp;

import org.hibernate.SessionFactory;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.dcm.scp.services.DicomSCPInstanceService;
import org.nrg.framework.orm.hibernate.audit.NrgRevisionEntity;
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
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that DICOM SCP receiver configuration changes are audited, including whitelist element-collection
 * membership, with the username of the administrator responsible recorded on the {@link NrgRevisionEntity revision}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestDicomSCPInstanceAuditingConfig.class)
public class TestDicomSCPInstanceAuditing {
    @Autowired
    public void setDicomSCPInstanceService(final DicomSCPInstanceService service) {
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
    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void auditsSecuritySettingChangesWithUsername() {
        authenticateAs("installing-admin");
        final DicomSCPInstance instance = _service.newEntity();
        instance.setAeTitle("AUDIT1");
        instance.setPort(8204);
        instance.setIdentifier("dicomObjectIdentifier");
        instance.setWhitelistEnabled(true);
        _service.create(instance);
        final long id = instance.getId();

        // The changes that most need a trail: silently turning anonymization off, disabling whitelisting, and taking
        // the receiver offline. "enabled" is audited via @AuditOverride because on a receiver it is real
        // configuration rather than the soft-delete bookkeeping it represents on most entities.
        authenticateAs("changing-admin");
        instance.setAnonymizationEnabled(false);
        instance.setWhitelistEnabled(false);
        instance.setEnabled(false);
        instance.setProjectRoutingExpression("(0010,0020)");
        _service.update(instance);

        inTransaction(reader -> {
            final List<Number> revisions = reader.getRevisions(DicomSCPInstance.class, id);
            assertThat(revisions).hasSize(2);

            final DicomSCPInstance asInstalled = reader.find(DicomSCPInstance.class, id, revisions.get(0));
            assertThat(asInstalled).isNotNull();
            assertThat(asInstalled.isAnonymizationEnabled()).isTrue();
            assertThat(asInstalled.isWhitelistEnabled()).isTrue();
            assertThat(asInstalled.isEnabled()).isTrue();
            assertThat(asInstalled.getAeTitle()).isEqualTo("AUDIT1");
            assertThat(asInstalled.getProjectRoutingExpression()).isNull();
            assertThat(usernameFor(reader, revisions.get(0))).isEqualTo("installing-admin");

            final DicomSCPInstance asChanged = reader.find(DicomSCPInstance.class, id, revisions.get(1));
            assertThat(asChanged).isNotNull();
            assertThat(asChanged.isAnonymizationEnabled()).isFalse();
            assertThat(asChanged.isWhitelistEnabled()).isFalse();
            assertThat(asChanged.isEnabled()).isFalse();
            assertThat(asChanged.getProjectRoutingExpression()).isEqualTo("(0010,0020)");
            assertThat(usernameFor(reader, revisions.get(1))).isEqualTo("changing-admin");
        });
    }

    @Test
    public void auditsWhitelistMembershipIncludingRemovals() {
        authenticateAs("whitelist-admin");
        final DicomSCPInstance instance = create("AUDIT2", 8205);
        final long             id       = instance.getId();

        // A detached instance whose collection was replaced wholesale — the same shape DicomSCPManager hands the
        // service after JSON deserialization. DicomSCPInstanceDAO.update applies it with Session.merge(); a plain
        // Session.update() here records the additions but silently drops the removal from the audit history.
        instance.setWhitelistEnabled(true);
        instance.setWhitelist(new ArrayList<>(Arrays.asList("SCANNER_A", "SCANNER_B")));
        _service.update(instance);

        instance.setWhitelist(new ArrayList<>(Collections.singletonList("SCANNER_A")));
        _service.update(instance);

        inTransaction(reader -> {
            final List<Number> revisions = reader.getRevisions(DicomSCPInstance.class, id);
            assertThat(revisions).hasSize(3);
            assertThat(reader.find(DicomSCPInstance.class, id, revisions.get(0)).getWhitelist()).isEmpty();
            assertThat(reader.find(DicomSCPInstance.class, id, revisions.get(1)).getWhitelist())
                    .containsExactlyInAnyOrder("SCANNER_A", "SCANNER_B");
            // The removal is the part a naive reattach loses.
            assertThat(reader.find(DicomSCPInstance.class, id, revisions.get(2)).getWhitelist())
                    .containsExactly("SCANNER_A");
        });
    }

    @Test
    public void auditsDeletionAndRecordsNullUsernameOutsideAuthenticatedContext() {
        // DicomSCPInstance is not @Auditable, so removing a receiver is a hard delete, which Envers records as a
        // deletion rather than a modification.
        final DicomSCPInstance instance = create("AUDIT3", 8206);
        final long             id       = instance.getId();
        _service.delete(instance);

        inTransaction(reader -> {
            final List<Number> revisions = reader.getRevisions(DicomSCPInstance.class, id);
            assertThat(revisions).hasSize(2);
            assertThat(reader.find(DicomSCPInstance.class, id, revisions.get(1))).isNull();
            assertThat(usernameFor(reader, revisions.get(0))).isNull();
            assertThat(usernameFor(reader, revisions.get(1))).isNull();
        });
    }

    private DicomSCPInstance create(final String aeTitle, final int port) {
        final DicomSCPInstance instance = _service.newEntity();
        instance.setAeTitle(aeTitle);
        instance.setPort(port);
        instance.setIdentifier("dicomObjectIdentifier");
        _service.create(instance);
        return instance;
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

    private DicomSCPInstanceService     _service;
    private SessionFactory             _sessionFactory;
    private PlatformTransactionManager _transactionManager;
}
