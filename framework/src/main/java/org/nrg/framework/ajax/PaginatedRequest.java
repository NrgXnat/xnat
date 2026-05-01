package org.nrg.framework.ajax;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@SuppressWarnings({"JavaDoc", "WeakerAccess"})
public abstract class PaginatedRequest {
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        final PaginatedRequest that = (PaginatedRequest) object;
        boolean isEqualTo = new EqualsBuilder().append(getId(), that.getId())
                                               .append(getSortColumn(), that.getSortColumn())
                                               .append(getSortDir(), that.getSortDir())
                                               .append(getSortBys(), that.getSortBys())
                                               .append(getPageNumber(), that.getPageNumber())
                                               .append(getPageSize(), that.getPageSize())
                                               .append(getFiltersMap(), that.getFiltersMap())
                                               .isEquals();
        return isEqualTo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sortColumn, sortDir, sortBys, pageNumber, pageSize, filtersMap);
    }

    @Nullable
    @JsonProperty(value = "id")
    protected String id;

    @JsonProperty(value = "sort_col")
    protected volatile String sortColumn;

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
     * @return A list containing one or more pairs of sort column and order.
     */
    @Nonnull
    @JsonProperty(value = "sortBys")
    @Singular
    protected List<Pair<SortDir, String>> sortBys = new ArrayList<>();

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

        private final String direction;

        SortDir(final String direction) {
            this.direction = direction;
        }

        @JsonValue
        public String getDirection() {
            return direction;
        }
    }
}
