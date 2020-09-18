package org.nrg.framework.ajax;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Map;

@Data
@NoArgsConstructor
public abstract class PaginatedRequest {
    @Nullable
    @JsonProperty(value = "id")
    protected String id;

    @JsonProperty(value = "sort_col")
    protected String sortColumn;

    @JsonProperty(value = "sort_dir", defaultValue = "desc")
    protected SortDir sortDir = SortDir.DESC;

    @JsonProperty(value = "page", defaultValue = "1")
    protected int pageNumber = 1;

    @JsonProperty(value = "size", defaultValue = "50")
    protected int pageSize = 50;

    @Nullable
    @JsonProperty(value = "filters")
    protected Map<String, Filter> filtersMap;

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

    protected abstract String getDefaultSortColumn();

    public int getOffset() {
        return pageNumber > 1 ? (pageNumber - 1) * pageSize : 0;
    }

    public enum SortDir {
        DESC("desc"),
        ASC("asc");

        String direction;

        SortDir(String direction) {
            this.direction = direction;
        }

        @JsonValue
        public String getDirection() {
            return direction;
        }
    }
}
