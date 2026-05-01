/*
 * automation: org.nrg.automation.services.EventService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services;

import org.nrg.automation.entities.Event;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.framework.orm.hibernate.BaseHibernateService;

public interface EventService extends BaseHibernateService<Event> {
    /**
     * Indicates whether an event with the indicated ID exists.
     *
     * @param eventId The event ID to test.
     *
     * @return Returns true if an event with the event ID exists, false otherwise.
     */
    boolean hasEvent(String eventId);

    /**
     * Gets the event with the indicated ID.
     *
     * @param eventId The event ID of the event to retrieve.
     *
     * @return Returns the {@link Event} if an event with the event ID exists, null otherwise.
     */
    Event getByEventId(String eventId);

    /**
     * Deletes the {@link Event event} with the indicated ID. If the cascade flag is set to false, the event will only
     * be deleted if there are no {@link ScriptTrigger script triggers} that reference the event. Otherwise, the
     * {@link EventReferencedException} will be thrown. If the cascade flag is set to true, the event and any associated
     * script triggers will be deleted.
     *
     * @param eventId The event ID of the event to delete.
     * @param cascade Whether the delete operation should cascade.
     *
     * @throws EventReferencedException Indicates that there are still script triggers associated with the event.
     */
    void delete(String eventId, boolean cascade) throws EventReferencedException;
}
