package org.nrg.framework.ajax.hibernate;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections.MapUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.metadata.ClassMetadata;
import org.nrg.framework.ajax.PaginatedRequest;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class HibernatePaginatedRequest extends PaginatedRequest {
    public boolean hasFilters() {
        return !MapUtils.isEmpty(filtersMap);
    }

    @Nonnull
    public List<Criterion> getCriterion(final ClassMetadata classMetadata) {
        if (filtersMap.entrySet().stream().anyMatch(entry -> !(entry.getValue() instanceof HibernateFilter))) {
            throw new RuntimeException("Invalid filter");
        }
        return filtersMap.entrySet().stream().map(entry -> ((HibernateFilter) entry.getValue()).makeCriterion(entry.getKey(), classMetadata)).collect(Collectors.toList());
    }

    public Order getOrder() {
        return AbstractHibernateDAO.getOrder(getSortColumn(), getSortDir());
    }
}
