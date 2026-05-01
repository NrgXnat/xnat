package org.nrg.automation.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.nrg.automation.event.entities.AutomationEventIds;
import org.nrg.automation.event.entities.AutomationEventIdsIds;
import org.nrg.automation.event.entities.AutomationFilters;
import org.nrg.automation.services.AutomationEventIdsIdsService;
import org.nrg.automation.services.AutomationEventIdsService;
import org.nrg.automation.services.AutomationFiltersService;
import org.nrg.automation.services.AutomationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AutomationServiceImpl implements AutomationService {
    private final AutomationEventIdsService    eventIdsService;
    private final AutomationEventIdsIdsService eventIdsIdsService;
    private final AutomationFiltersService     filtersService;

    @Value("${automation.enabled:true}")
    private boolean automationEnabled;

    @Autowired
    public AutomationServiceImpl(final AutomationEventIdsService eventIdsService,
                                 final AutomationEventIdsIdsService eventIdsIdsService,
                                 final AutomationFiltersService filtersService) {
        this.eventIdsService    = eventIdsService;
        this.eventIdsIdsService = eventIdsIdsService;
        this.filtersService     = filtersService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void incrementEventId(String projectId, String srcEventClass, String eventText) {
        incrementEventId(projectId, srcEventClass, eventText, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void addValueToStoredFilters(String externalId, String srcEventClass, String field, String value) {
        addValueToStoredFilters(externalId, srcEventClass, field, value, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAutomationEnabled() {
        return automationEnabled;
    }

    private void incrementEventId(String projectId, String srcEventClass, String eventText, boolean retry) {
        if (!automationEnabled) {
            log.debug("Automation is disabled, not incrementing event id for project id \"{}\" src event class \"{}\" event text \"{}\"", projectId, srcEventClass, eventText);
            return;
        }

        List<AutomationEventIdsIds> idsIdsList = eventIdsIdsService.getEventIds(projectId, srcEventClass, eventText, true);
        AutomationEventIdsIds idsIds = null;
        if (idsIdsList.isEmpty()) {
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
                log.warn("{}RETRYING - A constraint violation error on {} occurred saving an automation event ids ids entity {}",
                        retry ? "" : "NOT ", e.getConstraintName(), idsIds, e);
            }
        } else {
            log.warn("{}RETRYING - Could not find automation event ids ids entity with project id \"{}\" src event class \"{}\" event text \"{}\"",
                    retry ? "" : "NOT ", projectId, srcEventClass, eventText);
        }

        if (retry) {
            incrementEventId(projectId, srcEventClass, eventText, false);
        }
    }

    private void addValueToStoredFilters(final String externalId, final String srcEventClass, final String field, final String value, final boolean retry) {
        if (!automationEnabled) {
            log.debug("Automation is disabled, not adding value to stored filters for external id \"{}\" src event class \"{}\" field \"{}\" value \"{}\"", externalId, srcEventClass, field, value);
            return;
        }

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
            log.warn("{}RETRYING - A constraint violation error on {} occurred saving a filters entity {}",
                    retry ? "" : "NOT ", e.getConstraintName(), filters, e);
        }

        if (retry) {
            addValueToStoredFilters(externalId, srcEventClass, field, value, false);
        }
    }
}
