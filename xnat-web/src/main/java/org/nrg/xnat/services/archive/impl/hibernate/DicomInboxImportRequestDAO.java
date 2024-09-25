/*
 * web: org.nrg.xnat.daos.HostInfoDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.archive.impl.hibernate;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.orm.hibernate.QueryBuilder;
import org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest.Status.Completed;
import static org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest.Status.Failed;

@Repository
public class DicomInboxImportRequestDAO extends AbstractHibernateDAO<DicomInboxImportRequest> {
    public static final String STATUS   = "status";
    public static final String USERNAME = "username";

    @Transactional
    public List<DicomInboxImportRequest> findAllOutstandingDicomInboxImportRequests() {
        return findDicomInboxImportRequests(null, true);
    }

    @Transactional
    public List<DicomInboxImportRequest> findAllOutstandingDicomInboxImportRequestsForUser(final String username) {
        return findDicomInboxImportRequests(username, true);
    }

    @Transactional
    public List<DicomInboxImportRequest> findAllDicomInboxImportRequestsForUser(final String username) {
        return findDicomInboxImportRequests(username, false);
    }

    private List<DicomInboxImportRequest> findDicomInboxImportRequests(final String username, final boolean limitToOutstanding) {
        List<Predicate>                       predicates = new ArrayList<>();
        QueryBuilder<DicomInboxImportRequest> builder    = newQueryBuilder();
        if (limitToOutstanding) {
            predicates.add(builder.not(builder.in(STATUS, NOT_OUTSTANDING_VALUES)));
        }
        if (StringUtils.isNotBlank(username)) {
            predicates.add(builder.eq(USERNAME, username));
        }
        builder.where(builder.and(predicates));
        return builder.getResults();
    }

    private static final List<DicomInboxImportRequest.Status> NOT_OUTSTANDING_VALUES = Arrays.asList(Failed, Completed);
}
