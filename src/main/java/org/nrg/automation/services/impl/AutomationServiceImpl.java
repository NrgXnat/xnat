package org.nrg.automation.services.impl;

import org.hibernate.exception.ConstraintViolationException;
import org.nrg.automation.event.entities.AutomationEventIds;
import org.nrg.automation.event.entities.AutomationEventIdsIds;
import org.nrg.automation.event.entities.AutomationFilters;
import org.nrg.automation.services.AutomationEventIdsIdsService;
import org.nrg.automation.services.AutomationEventIdsService;
import org.nrg.automation.services.AutomationFiltersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AutomationServiceImpl implements AutomationService {
    private static final Logger logger = LoggerFactory.getLogger(AutomationServiceImpl.class);

    private final AutomationEventIdsService eventIdsService;
    private final AutomationEventIdsIdsService eventIdsIdsService;
    private final AutomationFiltersService filtersService;

    public AutomationServiceImpl(final AutomationEventIdsService eventIdsService,
                                 final AutomationEventIdsIdsService eventIdsIdsService,
                                 final AutomationFiltersService filtersService) {
        this.eventIdsService = eventIdsService;
        this.eventIdsIdsService = eventIdsIdsService;
        this.filtersService = filtersService;
    }

    @Override
    @Transactional
    public void incrementEventId(String projectId, String srcEventClass, String eventText) {
        incrementEventId(projectId, srcEventClass, eventText, true);
    }

    @Override
    @Transactional
    public void addValueToStoredFilters(String externalId, String srcEventClass, String field, String value) {
        addValueToStoredFilters(externalId, srcEventClass, field, value, true);
    }

    private void incrementEventId(String projectId, String srcEventClass, String eventText, boolean retry) {
        List<AutomationEventIdsIds> idsIdsList = eventIdsIdsService.getEventIds(projectId, srcEventClass, eventText, true);
        AutomationEventIdsIds idsIds = null;
        if (idsIdsList.size() < 1) {
            final List<AutomationEventIds> idsList = eventIdsService.getEventIds(projectId, srcEventClass, true);
            idsIds = new AutomationEventIdsIds(projectId, srcEventClass, eventText, idsList);
        } else {
            for (final AutomationEventIdsIds _idsIds : idsIdsList) {
                if (_idsIds.getEventId().equals(eventText)) {
                    _idsIds.setCounter(_idsIds.getCounter()+1);
                    idsIds = _idsIds;
                    break;
                }
            }
        }

        if (idsIds != null) {
            try {
                eventIdsIdsService.saveOrUpdate(idsIds);
                return;
            } catch (ConstraintViolationException e) {
                logger.warn("{}RETRYING - A constraint violation error on {} occurred saving an automation event ids ids entity {}",
                        retry ? "" : "NOT ", e.getConstraintName(), idsIds, e);
            }
        } else {
            logger.warn("{}RETRYING - Could not find automation event ids ids entity with project id \"{}\" src event class \"{}\" event text \"{}\"",
                    retry ? "" : "NOT ", projectId, srcEventClass, eventText);
        }

        if (retry) {
            incrementEventId(projectId, srcEventClass, eventText, false);
        }
    }

    private void addValueToStoredFilters(final String externalId, final String srcEventClass, final String field, final String value, final boolean retry) {
        AutomationFilters filters = filtersService.getAutomationFilters(externalId, srcEventClass, field, true);
        if (filters == null) {
            filters = new AutomationFilters(externalId, srcEventClass, field);
        }

        final boolean added = filters.addValue(value);

        if (!added) {
            return;
        }

        try {
            filtersService.saveOrUpdate(filters);
            return;
        } catch (ConstraintViolationException e) {
            logger.warn("{}RETRYING - A constraint violation error on {} occurred saving a filters entity {}",
                    retry ? "" : "NOT ", e.getConstraintName(), filters, e);
        }

        if (retry) {
            addValueToStoredFilters(externalId, srcEventClass, field, value, false);
        }
    }
}
