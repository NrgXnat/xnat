package org.nrg.framework.ajax.hibernate;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.metadata.ClassMetadata;
import org.nrg.framework.ajax.Filter;
import org.nrg.framework.ajax.PaginatedRequest;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class HibernatePaginatedRequest extends PaginatedRequest {

    public boolean hasFilters() {
        return filtersMap != null && !filtersMap.isEmpty();
    }

    @Nonnull
    public List<Criterion> getCriterion(ClassMetadata classMetadata) {
        List<Criterion> criteria = new ArrayList<>();
        if (filtersMap == null) {
            return criteria;
        }
        for (String property : filtersMap.keySet()) {
            Filter filter = filtersMap.get(property);
            if (!(filter instanceof HibernateFilter)) {
                throw new RuntimeException("Invalid filter");
            }
            criteria.add(((HibernateFilter) filter).makeCriterion(property, classMetadata));
        }
        return criteria;
    }

    public Order getOrder() {
        if (sortDir == SortDir.ASC) {
            return Order.asc(getSortColumn());
        } else {
            return Order.desc(getSortColumn());
        }
    }
}
