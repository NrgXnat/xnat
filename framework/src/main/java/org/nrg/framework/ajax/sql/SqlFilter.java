package org.nrg.framework.ajax.sql;

import org.nrg.framework.ajax.Filter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public abstract class SqlFilter extends Filter {
    /**
     * Construct SQL query string from column name and parameters
     * @param dbColumnName column name
     * @param namedParams parameters
     * @return the SQL query
     * @throws SortOrFilterException if parameters clash
     */
    public abstract String constructQueryString(String dbColumnName, MapSqlParameterSource namedParams)
            throws SortOrFilterException;

    /**
     * Validate UI filter input
     * @param uiValue the value
     * @throws SortOrFilterException if UI value isn't permitted
     */
    abstract void validate(String uiValue) throws SortOrFilterException;
}
