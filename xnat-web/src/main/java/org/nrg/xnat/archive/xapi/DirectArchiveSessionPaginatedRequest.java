package org.nrg.xnat.archive.xapi;

import org.nrg.framework.ajax.hibernate.HibernatePaginatedRequest;

public class DirectArchiveSessionPaginatedRequest extends HibernatePaginatedRequest {
    @Override
    public String getDefaultSortColumn() {
        return "lastBuiltDate";
    }
}