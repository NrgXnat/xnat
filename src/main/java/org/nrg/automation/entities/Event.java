package org.nrg.automation.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * ScriptTrigger class.
 *
 * @author Rick Herrick
 */
@Auditable
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"eventId", "disabled"}),
        @UniqueConstraint(columnNames = {"eventLabel", "disabled"})})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class Event extends AbstractHibernateEntity implements Comparable<Event> {

    public Event() {

    }

    public Event(final String eventId, final String eventLabel) {
        _eventId = eventId;
        _eventLabel = eventLabel;
    }

    public String getEventId() {
        return _eventId;
    }

    public void setEventId(final String eventId) {
        _eventId = eventId;
    }

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
