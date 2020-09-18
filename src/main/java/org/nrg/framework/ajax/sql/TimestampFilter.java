// Copyright 2019 Radiologics, Inc
// Developer: Kate Alpert <kate@radiologics.com>

package org.nrg.framework.ajax.sql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.ajax.PaginatedRequest;
import org.nrg.framework.ajax.hibernate.HibernateFilter;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.annotation.Nullable;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Provides filtering for {@link PaginatedRequest pure SQL-based paginated requests}. You can also use {@link
 * NumericFilter} and {@link StringFilter}, but not {@link HibernateFilter}, which is solely for filtering Hibernate
 * services and DAOs that extend {@link AbstractHibernateDAO}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class TimestampFilter extends SqlFilter {
    @JsonIgnore private final static Pattern validRegex = Pattern.compile("^[A-Za-z0-9.: +\\-]+$");

    @Nullable @JsonProperty private String before;
    @Nullable @JsonProperty private String after;
    @Nullable @JsonProperty private String beforeOrOn;
    @Nullable @JsonProperty private String afterOrOn;

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public String constructQueryString(String dbColumnName, MapSqlParameterSource namedParams) throws SortOrFilterException {
        if (StringUtils.isNotBlank(before) && StringUtils.isNotBlank(beforeOrOn) ||
                StringUtils.isNotBlank(after) && StringUtils.isNotBlank(afterOrOn)) {
            throw new SortOrFilterException("Cannot have both * and *OrOn params");
        }
        List<String> filters = new ArrayList<>();
        if (StringUtils.isNotBlank(after)) {
            validate(after);
            namedParams.addValue(dbColumnName + "after", after, Types.TIMESTAMP);
            filters.add(dbColumnName + " > :" + dbColumnName + "after");
        }
        if (StringUtils.isNotBlank(afterOrOn)) {
            validate(afterOrOn);
            namedParams.addValue(dbColumnName + "afterOrOn", afterOrOn, Types.TIMESTAMP);
            filters.add(dbColumnName + " >= :" + dbColumnName + "afterOrOn");
        }
        if (StringUtils.isNotBlank(before)) {
            validate(before);
            namedParams.addValue(dbColumnName + "before", before, Types.TIMESTAMP);
            filters.add(dbColumnName + " < :" + dbColumnName + "before");
        }
        if (StringUtils.isNotBlank(beforeOrOn)) {
            validate(beforeOrOn);
            namedParams.addValue(dbColumnName + "beforeOrOn", beforeOrOn, Types.TIMESTAMP);
            filters.add(dbColumnName + " <= :" + dbColumnName + "beforeOrOn");
        }
        return StringUtils.join(filters, " AND ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    void validate(String uiValue) throws SortOrFilterException {
        if (!validRegex.matcher(uiValue).matches()) {
            throw new SortOrFilterException("Invalid timestamp filter parameter: " + uiValue);
        }
    }
}