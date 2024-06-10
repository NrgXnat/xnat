package org.nrg.xnat.eventservice.services;

import org.nrg.framework.ajax.hibernate.HibernatePaginatedRequest;

public class SubscriptionDeliveryEntityPaginatedRequest extends HibernatePaginatedRequest {
    @Override
    protected String getDefaultSortColumn() {
        return "id";
    }
}
