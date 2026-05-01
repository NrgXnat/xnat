// Developer: Kate Alpert <kate@radiologics.com>

package org.nrg.framework.ajax.sql;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.ajax.Filter;
import org.nrg.framework.ajax.PaginatedRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Slf4j
public abstract class SqlPaginatedRequest extends PaginatedRequest {
    /**
     * Construct query suffix with filters, sorting, and limits
     * @param repoMapping mapping of ui to db col
     * @param allowableSortCols cols we can sort
     * @param allowableFilterCols cols we can filter
     * @param namedParams the named parameters object
     * @return query suffix
     * @throws SortOrFilterException if filter parameters are invalid
     */
    public String getQuerySuffix(Map<String, PageableRepository.ColumnDataType> repoMapping,
                                 Set<String> allowableSortCols,
                                 Set<String> allowableFilterCols,
                                 MapSqlParameterSource namedParams) throws SortOrFilterException {
        StringBuilder suffix = new StringBuilder();

        //add filter
        addFilterSuffix(repoMapping, allowableFilterCols, suffix, namedParams);

        //add sort
        String sortColumnDb = getDbColumnFromMapping(repoMapping, getSortColumn());
        if (allowableSortCols.contains(sortColumnDb)) {
            suffix.append(String.format(" ORDER BY %s ", sortColumnDb));
            suffix.append(sortDir.getDirection());
        } else {
            throw new SortOrFilterException("Sorting on column " + sortColumnDb + " is not allowed");
        }

        //add pagination
        if (pageSize > 0) {
            suffix.append(String.format(" LIMIT %d", pageSize));
        }
        int offset = getOffset();
        if (offset > 0) {
            suffix.append(String.format(" OFFSET %d", offset));
        }

        return suffix.toString();
    }

    /**
     * Get database column name from Ui column name
     * @param repoMapping mapping of ui to db col
     * @param uiColumn the ui column name
     * @return the db column name
     * @throws SortOrFilterException if no corresponding column
     */
    private String getDbColumnFromMapping(Map<String, PageableRepository.ColumnDataType> repoMapping, String uiColumn) throws SortOrFilterException {
        for (String key : repoMapping.keySet()) {
            if (repoMapping.get(key).columnName.equals(uiColumn)) {
                return key;
            }
        }
        throw new SortOrFilterException("No column corresponding to " + uiColumn);
    }

    /**
     * Append filters to query suffix
     * @param repoMapping mapping of ui to db col
     * @param allowableFilterCols cols we can filter
     * @param suffix string builder of query suffix
     * @param namedParams the named params object
     * @throws SortOrFilterException if filter parameters are invalid
     */
    private void addFilterSuffix(Map<String, PageableRepository.ColumnDataType> repoMapping, Set<String> allowableFilterCols, StringBuilder suffix, MapSqlParameterSource namedParams) throws SortOrFilterException {
        boolean needsWhere = true;
        for (String key : filtersMap.keySet()) {
            // From UI to DB column name
            String column = getDbColumnFromMapping(repoMapping, key);
            // Allowed to filter?
            if (allowableFilterCols.contains(column)) {
                if (needsWhere) {
                    suffix.append(" WHERE ");
                    needsWhere = false;
                } else {
                    suffix.append(" AND ");
                }
                Filter filter = filtersMap.get(key);
                if (!(filter instanceof SqlFilter)) {
                    throw new RuntimeException("Invalid filter");
                }
                suffix.append(((SqlFilter) filter).constructQueryString(column, namedParams));
            } else {
                throw new SortOrFilterException("Filtering on column " + column + " is not allowed");
            }
        }
    }
}
