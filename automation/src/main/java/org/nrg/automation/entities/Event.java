/*
 * automation: org.nrg.automation.entities.Event
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.entities;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.annotation.Nonnull;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * ScriptTrigger class.
 *
 * @author Rick Herrick
 */
@Entity
@Cacheable
public class Event extends AbstractHibernateEntity implements Comparable<Event> {
    private static final long serialVersionUID = 4142166827192931193L;

    public Event() {
        this(null, null);
    }

    public Event(final String eventId, final String eventLabel) {
        _eventId    = eventId;
        _eventLabel = eventLabel;
    }

    @Column(unique = true)
    public String getEventId() {
        return _eventId;
    }

    @SuppressWarnings("unused")
    public void setEventId(final String eventId) {
        _eventId = eventId;
    }

    @Column(unique = true)
    public String getEventLabel() {
        return _eventLabel;
    }

    @SuppressWarnings("unused")
    public void setEventLabel(final String eventLabel) {
        _eventLabel = eventLabel;
    }

    @Override
    public int compareTo(@Nonnull Event other) {
        return StringUtils.compare(_eventId + _eventLabel, other._eventId + other._eventLabel);
    }

    private String _eventId;
    private String _eventLabel;
}
