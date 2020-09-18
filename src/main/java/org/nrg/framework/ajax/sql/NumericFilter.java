// Copyright 2019 Radiologics, Inc
// Developer: Kate Alpert <kate@radiologics.com>

package org.nrg.framework.ajax.sql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.ajax.PaginatedRequest;
import org.nrg.framework.ajax.hibernate.HibernateFilter;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.annotation.Nullable;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides filtering for {@link PaginatedRequest pure SQL-based paginated requests}. You can also use {@link
 * {@link StringFilter} and {@link TimestampFilter}, but not {@link HibernateFilter}, which is solely for filtering
 * Hibernate services and DAOs that extend {@link AbstractHibernateDAO}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class NumericFilter extends SqlFilter {
    @Nullable @JsonProperty private Number eq;
    @Nullable @JsonProperty private Number neq;
    @Nullable @JsonProperty private Number gt;
    @Nullable @JsonProperty private Number ge;
    @Nullable @JsonProperty private Number lt;
    @Nullable @JsonProperty private Number le;

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public String constructQueryString(final String dbColumnName, final MapSqlParameterSource namedParams) throws SortOrFilterException {
        if (eq != null) {
            if (ObjectUtils.anyNotNull(neq, gt, ge, lt, le)) {
                throw new SortOrFilterException("Cannot have both eq and any neq, gt, ge, lt, or le params");
            }
            namedParams.addValue(dbColumnName + "eq", eq, Types.DECIMAL);
            return dbColumnName + " = :" + dbColumnName + "eq";
        }
        if (gt != null && ge != null || lt != null && le != null) {
            throw new SortOrFilterException("Cannot have both *t and *e params");
        }
        final List<String> filters = new ArrayList<>();
        if (gt != null) {
            namedParams.addValue(dbColumnName + "gt", gt, Types.DECIMAL);
            filters.add(dbColumnName + " > :" + dbColumnName + "gt");
        }
        if (ge != null) {
            namedParams.addValue(dbColumnName +"ge", ge, Types.DECIMAL);
            filters.add(dbColumnName + " >= :" + dbColumnName + "ge");
        }
        if (lt != null) {
            namedParams.addValue(dbColumnName +"lt", lt, Types.DECIMAL);
            filters.add(dbColumnName + " < :" + dbColumnName + "lt");
        }
        if (le != null) {
            namedParams.addValue(dbColumnName + "le", le, Types.DECIMAL);
            filters.add(dbColumnName + " <= :" + dbColumnName + "le");
        }
        return StringUtils.join(filters, " AND ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    void validate(String uiValue) {
        // Validation occurs when casting to Number, no need for addl validation here
    }
}