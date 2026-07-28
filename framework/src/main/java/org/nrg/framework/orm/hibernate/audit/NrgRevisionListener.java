/*
 * framework: org.nrg.framework.orm.hibernate.audit.NrgRevisionListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.hibernate.audit;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Stamps each Envers revision with the username of the user responsible for it. Audited writes happen synchronously on
 * the thread handling the request, so for user-initiated changes the Spring Security context holds the authenticated
 * user making the change. System-initiated changes (initialization tasks, schedulers) record a null username.
 */
@Slf4j
public class NrgRevisionListener implements RevisionListener {
    @Override
    public void newRevision(final Object revisionEntity) {
        ((NrgRevisionEntity) revisionEntity).setUsername(getUsername());
    }

    private static String getUsername() {
        try {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null ? authentication.getName() : null;
        } catch (Exception e) {
            // This runs inside the flush of the write it's auditing: never let resolving the username break that write.
            log.debug("An error occurred resolving the username for an audit revision, recording null", e);
            return null;
        }
    }
}
