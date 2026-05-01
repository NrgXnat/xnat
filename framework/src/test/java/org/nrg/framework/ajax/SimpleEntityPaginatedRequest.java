package org.nrg.framework.ajax;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.nrg.framework.ajax.hibernate.HibernatePaginatedRequest;

@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class SimpleEntityPaginatedRequest extends HibernatePaginatedRequest {
    @Override
    protected String getDefaultSortColumn() {
        return "id";
    }
}
