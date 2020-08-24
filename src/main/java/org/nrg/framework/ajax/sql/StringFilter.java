// Copyright 2019 Radiologics, Inc
// Developer: Kate Alpert <kate@radiologics.com>

package org.nrg.framework.ajax.sql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

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

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public Boolean getNot() {
        return not;
    }

    public void setNot(Boolean not) {
        this.not = not;
    }
}