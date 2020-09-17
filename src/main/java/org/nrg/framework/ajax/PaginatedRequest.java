package org.nrg.framework.ajax;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

import javax.annotation.Nullable;
import java.util.Map;

@Data
public abstract class PaginatedRequest {
    protected @Nullable @JsonProperty(value = "id") String id;
    protected @JsonProperty(value = "sort_col") String sortColumn;
    protected @JsonProperty(value = "sort_dir", defaultValue = "desc") SortDir sortDir = SortDir.DESC;
    protected @JsonProperty(value = "page", defaultValue = "1") int pageNumber = 1;
    protected @JsonProperty(value = "size", defaultValue = "50") int pageSize = 50;
    protected @Nullable @JsonProperty(value = "filters") Map<String, Filter> filtersMap;

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
