package org.nrg.framework.ajax.hibernate;

import org.apache.commons.collections.MapUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.metadata.ClassMetadata;
import org.nrg.framework.ajax.PaginatedRequest;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class HibernatePaginatedRequest extends PaginatedRequest {
    public boolean hasFilters() {
        return !MapUtils.isEmpty(filtersMap);
    }

    @Nonnull
    public List<Criterion> getCriterion(ClassMetadata classMetadata) {
        if (filtersMap == null) {
            return Collections.emptyList();
        }
        if (filtersMap.entrySet().stream().anyMatch(entry -> !(entry.getValue() instanceof HibernateFilter))) {
            throw new RuntimeException("Invalid filter");
        }
        return filtersMap.entrySet().stream().map(entry -> ((HibernateFilter) entry.getValue()).makeCriterion(entry.getKey(), classMetadata)).collect(Collectors.toList());
    }

    public Order getOrder() {
        return sortDir == SortDir.ASC ? Order.asc(getSortColumn()) : Order.desc(getSortColumn());
    }
}
