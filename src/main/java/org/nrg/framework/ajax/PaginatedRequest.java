package org.nrg.framework.ajax;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Nullable;
import java.util.Map;

public abstract class PaginatedRequest {
    protected @Nullable @JsonProperty(value = "id") String id;
    protected @JsonProperty(value = "sort_col") String sortColumn;
    protected @JsonProperty(value = "sort_dir", defaultValue = "desc") SortDir sortDir = SortDir.DESC;
    protected @JsonProperty(value = "page", defaultValue = "1") int pageNumber = 1;
    protected @JsonProperty(value = "size", defaultValue = "50") int pageSize = 50;
    protected @Nullable @JsonProperty(value = "filters") Map<String, Filter> filtersMap;

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortColumn() {
        if (sortColumn == null) {
            synchronized (this) {
                if (sortColumn == null) {
                    sortColumn = getDefaultSortColumn();
                }
            }
        }
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public SortDir getSortDir() {
        return sortDir;
    }

    public void setSortDir(SortDir sortDir) {
        this.sortDir = sortDir;
    }

    @Nullable
    public Map<String, Filter> getFiltersMap() {
        return filtersMap;
    }

    public void setFiltersMap(@Nullable Map<String, Filter> filtersMap) {
        this.filtersMap = filtersMap;
    }

    protected abstract String getDefaultSortColumn();

    public int getOffset() {
        return pageNumber > 1 ? (pageNumber - 1) * pageSize : 0;
    }

    public enum SortDir {
        DESC("desc"),
        ASC("asc");

        String direction;
        private SortDir(String direction) {
            this.direction = direction;
        }

        @JsonValue
        public String getDirection() {
            return direction;
        }
    }
}
