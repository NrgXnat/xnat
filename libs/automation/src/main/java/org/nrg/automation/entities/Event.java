/*
 * automation: org.nrg.automation.entities.Event
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.entities;

import com.google.common.collect.Lists;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ScriptTrigger class.
 *
 * @author Rick Herrick
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class Event extends AbstractHibernateEntity implements Comparable<Event> {

    public Event() {

    }

    public Event(final String eventId, final String eventLabel) {
        _eventId = eventId;
        _eventLabel = eventLabel;
    }

    @Column(unique = true)
    public String getEventId() {
        return _eventId;
    }

    public void setEventId(final String eventId) {
        _eventId = eventId;
    }

    @Column(unique = true)
    public String getEventLabel() {
        return _eventLabel;
    }

    public void setEventLabel(final String eventLabel) {
        _eventLabel = eventLabel;
    }

    @Override
    public int compareTo(Event other) {
        return 0;
    }

    private String _eventId;
    private String _eventLabel;
}
