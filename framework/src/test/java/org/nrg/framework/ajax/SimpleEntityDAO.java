/*
 * framework: org.nrg.framework.ajax.SimpleEntityDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.ajax;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.ajax.hibernate.HibernateFilter;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

@Repository
public class SimpleEntityDAO extends AbstractHibernateDAO<SimpleEntity> {
    /**
     * {@inheritDoc}
     */
    @Override
    public List<SimpleEntity> findAllByExample(SimpleEntity example, String[] excludedProperties) {
        return super.findAllByExample(example, AbstractHibernateEntity.getExcludedProperties(excludedProperties));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SimpleEntity> findByExample(SimpleEntity example, String[] excludedProperties) {
        return super.findByExample(example, AbstractHibernateEntity.getExcludedProperties(excludedProperties));
    }

    public List<SimpleEntity> findAllOrderedByTimestamp() {
        return findAllOrderedByTimestamp(getUnpaginatedRequest());
    }

    public List<SimpleEntity> findAllOrderedByTimestamp(final @Nonnull SimpleEntityPaginatedRequest request) {
        return findPaginated(request.toBuilder().sortBy(Pair.of(PaginatedRequest.SortDir.ASC, "timestamp")).sortBy(Pair.of(PaginatedRequest.SortDir.ASC, "id")).build());
    }

    public List<SimpleEntity> findAllByUsername(final String username) {
        return findAllByUsername(username, getUnpaginatedRequest());
    }

    public List<SimpleEntity> findAllByUsername(final String username, final @Nonnull SimpleEntityPaginatedRequest request) {
        return findPaginated(request.toBuilder().filter("username", HibernateFilter.builder().operator(HibernateFilter.Operator.EQ).value(username).build()).build());
    }

    public Optional<SimpleEntity> findByIdAndUsername(final long id, final String username) {
        final SimpleEntity entity = findById(id);
        if (entity == null || !StringUtils.equals(username, entity.getUsername()))  {
            return Optional.empty();
        }
        return Optional.of(entity);
    }

    private static SimpleEntityPaginatedRequest getUnpaginatedRequest() {
        return SimpleEntityPaginatedRequest.builder().pageNumber(0).pageSize(0).build();
    }
}
