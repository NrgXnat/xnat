package org.nrg.framework.ajax.hibernate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.ajax.PaginatedRequest;

import javax.annotation.Nonnull;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public abstract class HibernatePaginatedRequest extends PaginatedRequest {
    public boolean hasFilters() {
        return !MapUtils.isEmpty(filtersMap);
    }

    @JsonIgnore
    @Nonnull
    public Map<String, HibernateFilter> getCriterion() {
        final Map<String, HibernateFilter> filters = filtersMap.entrySet()
                                                               .stream()
                                                               .filter(entry -> HibernateFilter.class.isAssignableFrom(entry.getValue().getClass()))
                                                               .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), (HibernateFilter) entry.getValue()))
                                                               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (filters.isEmpty()) {
            throw new RuntimeException("Invalid filter");
        }

        return filters;
    }

    public Pair<SortDir, String> getOrder() {
        return StringUtils.isBlank(getSortColumn()) ? null : Pair.of(getSortDir(), getSortColumn());
    }
}
