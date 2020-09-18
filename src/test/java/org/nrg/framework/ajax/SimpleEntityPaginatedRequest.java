package org.nrg.framework.ajax;

import org.nrg.framework.ajax.hibernate.HibernatePaginatedRequest;

public class SimpleEntityPaginatedRequest extends HibernatePaginatedRequest {
    @Override
    protected String getDefaultSortColumn() {
        return "id";
    }
}
