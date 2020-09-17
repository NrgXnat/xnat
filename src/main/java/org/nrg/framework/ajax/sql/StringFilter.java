// Copyright 2019 Radiologics, Inc
// Developer: Kate Alpert <kate@radiologics.com>

package org.nrg.framework.ajax.sql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.nrg.framework.ajax.PaginatedRequest;
import org.nrg.framework.ajax.hibernate.HibernateFilter;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

/**
 * Provides filtering for {@link PaginatedRequest pure SQL-based paginated requests}. You can also use {@link
 * NumericFilter} and {@link TimestampFilter}, but not {@link HibernateFilter}, which is solely for filtering Hibernate
 * services and DAOs that extend {@link AbstractHibernateDAO}.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class StringFilter extends SqlFilter {
    @JsonIgnore private final static Pattern validRegex = Pattern.compile("^[A-Za-z0-9_.\\-/ ]+$");
    @JsonProperty private String like;
    @Nullable @JsonProperty private Boolean not;

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public String constructQueryString(String dbColumnName, MapSqlParameterSource namedParams) throws SortOrFilterException {
        String notStr = (not == null || !not) ? "" : " NOT";
        validate(like);
        namedParams.addValue(dbColumnName + "str", "%" + like + "%");
        return dbColumnName + notStr + " ILIKE :" + dbColumnName + "str";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    void validate(String uiValue) throws SortOrFilterException {
        if (!validRegex.matcher(uiValue).matches()) {
            throw new SortOrFilterException("Invalid string filter parameter: " + uiValue);
        }
    }
}