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
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class HibernatePaginatedRequest extends PaginatedRequest {
    public boolean hasFilters() {
        return !MapUtils.isEmpty(filtersMap);
    }

    @Nonnull
    public List<Criterion> getCriterion(final ClassMetadata classMetadata) {
        final Map<String, HibernateFilter> filters = filtersMap.entrySet()
                                                               .stream()
                                                               .filter(entry -> HibernateFilter.class.isAssignableFrom(entry.getValue().getClass()))
                                                               .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), (HibernateFilter) entry.getValue()))
                                                               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (filters.isEmpty()) {
            throw new RuntimeException("Invalid filter");
        }

        return filters.entrySet().stream().map(entry -> entry.getValue().makeCriterion(entry.getKey(), classMetadata)).collect(Collectors.toList());
    }

    public Order getOrder() {
        return AbstractHibernateDAO.getOrder(getSortColumn(), getSortDir());
    }
}
