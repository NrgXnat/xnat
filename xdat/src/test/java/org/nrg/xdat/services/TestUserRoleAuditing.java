/*
 * core: org.nrg.xdat.services.TestUserRoleAuditing
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services;

import org.hibernate.SessionFactory;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.orm.hibernate.audit.NrgRevisionEntity;
import org.nrg.xdat.configuration.TestUserRoleServiceConfig;
import org.nrg.xdat.entities.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that role grants and revocations are audited, with the username of the administrator responsible recorded
 * on the {@link NrgRevisionEntity revision}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestUserRoleServiceConfig.class)
public class TestUserRoleAuditing {
    private static final String SUBJECT_USER = "auditedRoleHolder";

    @Autowired
    public void setUserRoleService(final UserRoleService service) {
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
    public void auditsRoleGrantAndRevocationWithUsername() {
        authenticateAs("granting-admin");
        final UserRole role = _service.newEntity();
        role.setUsername(SUBJECT_USER);
        role.setRole(UserRole.ROLE_ADMINISTRATOR);
        _service.create(role);
        final long id = role.getId();

        authenticateAs("revoking-admin");
        _service.delete(role);

        inTransaction(reader -> {
            final List<Number> revisions = reader.getRevisions(UserRole.class, id);
            assertEquals(2, revisions.size());

            final UserRole granted = reader.find(UserRole.class, id, revisions.get(0));
            assertNotNull(granted);
            assertEquals(SUBJECT_USER, granted.getUsername());
            assertEquals(UserRole.ROLE_ADMINISTRATOR, granted.getRole());
            assertTrue(granted.isEnabled());
            assertEquals("granting-admin", usernameFor(reader, revisions.get(0)));

            // UserRole carries the deprecated @Auditable annotation, which makes delete a soft delete: the row
            // survives with enabled = false rather than disappearing, so Envers records a modification, not a
            // deletion. The revoked role and the administrator who revoked it both remain recoverable.
            final UserRole revoked = reader.find(UserRole.class, id, revisions.get(1));
            assertNotNull(revoked);
            assertFalse(revoked.isEnabled());
            assertEquals(UserRole.ROLE_ADMINISTRATOR, revoked.getRole());
            assertEquals("revoking-admin", usernameFor(reader, revisions.get(1)));
        });
    }

    @Test
    public void recordsNullUsernameOutsideAuthenticatedContext() {
        final UserRole role = _service.newEntity();
        role.setUsername("systemGrantedRoleHolder");
        role.setRole(UserRole.ROLE_NON_EXPIRING);
        _service.create(role);

        inTransaction(reader -> {
            final List<Number> revisions = reader.getRevisions(UserRole.class, role.getId());
            assertEquals(1, revisions.size());
            assertNull(usernameFor(reader, revisions.get(0)));
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

    private UserRoleService            _service;
    private SessionFactory             _sessionFactory;
    private PlatformTransactionManager _transactionManager;
}
