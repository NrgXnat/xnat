// Copyright 2019 Radiologics, Inc
// Developer: Kate Alpert <kate@radiologics.com>

package org.nrg.framework.ajax.sql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.annotation.Nullable;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class NumericFilter extends SqlFilter {
    @Nullable @JsonProperty private Number gt;
    @Nullable @JsonProperty private Number ge;
    @Nullable @JsonProperty private Number lt;
    @Nullable @JsonProperty private Number le;

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public String constructQueryString(String dbColumnName, MapSqlParameterSource namedParams) throws SortOrFilterException {
        if (gt != null && ge != null || lt != null && le != null) {
            throw new SortOrFilterException("Cannot have both *t and *e params");
        }
        List<String> filters = new ArrayList<>();
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

    public void setGt(Number gt) {
        this.gt = gt;
    }

    public Number getGe() {
        return ge;
    }

    public void setGe(Number ge) {
        this.ge = ge;
    }

    public Number getLt() {
        return lt;
    }

    public void setLt(Number lt) {
        this.lt = lt;
    }

    public Number getLe() {
        return le;
    }

    public void setLe(Number le) {
        this.le = le;
    }

}