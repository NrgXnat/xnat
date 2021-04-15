package org.nrg.framework.ajax;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@SuppressWarnings({"JavaDoc", "WeakerAccess"})
public abstract class PaginatedRequest {
    @Nullable
    @JsonProperty(value = "id")
    protected String id;

    @JsonProperty(value = "sort_col")
    protected String sortColumn;

    @JsonProperty(value = "sort_dir", defaultValue = "desc")
    @Builder.Default
    protected SortDir sortDir = SortDir.DESC;

    /**
     * Specifies sort columns and directions. Each pair contains the sort column and direction for the sort, with sort
     * priority set by the order of the pairs in the list. Because {@link #getSortColumn()} and {@link #getSortDir()}
     * have default values so are always set to something, values set for this property <i>override</i> any sort
     * criteria set for those singular properties.
     *
     * @param sortBys One or more pairs of sort column and order.
     *
     * @return A list containing one or more pairs of sort column and order.
     */
    @Nonnull
    @JsonProperty(value = "sortBys")
    @Singular
    protected List<Pair<String, SortDir>> sortBys = new ArrayList<>();

    @JsonProperty(value = "page", defaultValue = "1")
    @Builder.Default
    protected int pageNumber = 1;

    @JsonProperty(value = "size", defaultValue = "50")
    @Builder.Default
    protected int pageSize = 50;

    @Nonnull
    @JsonProperty(value = "filters")
    @Singular("filter")
    protected Map<String, Filter> filtersMap = new HashMap<>();

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
