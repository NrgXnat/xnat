/**
 * ScriptTrigger
 * (C) 2014 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 9/19/2014 by Rick Herrick
 */
package org.nrg.automation.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jetbrains.annotations.NotNull;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

/**
 * ScriptTrigger class.
 *
 * @author Rick Herrick
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"association", "event"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class ScriptTrigger extends AbstractHibernateEntity implements Comparable<ScriptTrigger> {

    public static final String DEFAULT_EVENT = "Manual";

    public ScriptTrigger() {
        _log.debug("Creating a default ScriptTrigger object.");
    }

    public ScriptTrigger(final String triggerId, final String description, final String scriptId, final String association, final Event event) {
        setTriggerId(triggerId);
        setDescription(description);
        setScriptId(scriptId);
        setAssociation(association); // datatype:xnat:mrSessionData
        setEvent(event);             // archived
        if (_log.isDebugEnabled()) {
            _log.debug("Creating a ScriptTrigger object with the values: {}", toString());
        }
    }

    @Column(nullable = false, unique = true)
    public String getTriggerId() {
        return _triggerId;
    }

    public void setTriggerId(final String triggerId) {
        _triggerId = triggerId;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(final String description) {
        _description = description;
    }

    @Column(nullable = false)
    public String getScriptId() {
        return _scriptId;
    }

    public void setScriptId(final String scriptId) {
        _scriptId = scriptId;
    }

    /**
     * For the current iteration of this API, associations may be XNAT data types (in the form of the xsiType string),
     * a project ID (in the form "prj:ID"), or the containing site (in the form "site").
     *
     * @return The association for this trigger.
     */
    @Column(nullable = false)
    public String getAssociation() {
        return _association;
    }

    public void setAssociation(final String association) {
        _association = association;
    }

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, referencedColumnName = "eventId")
    public Event getEvent() {
        return _event;
    }

    public void setEvent(final Event event) {
        _event = event;
    }

    @Override
    public String toString() {
        return "ScriptTrigger{" +
                "name='" + _triggerId + '\'' +
                ", description='" + _description + '\'' +
                ", scriptId='" + _scriptId + '\'' +
                ", association='" + _association + '\'' +
                ", event='" + _event + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ScriptTrigger)) {
            return false;
        }

        ScriptTrigger trigger = (ScriptTrigger) object;

        return _association.equals(trigger._association) &&
                !(_description != null ? !_description.equals(trigger._description) : trigger._description != null) &&
                _event.equals(trigger._event) &&
                _triggerId.equals(trigger._triggerId) &&
                _scriptId.equals(trigger._scriptId);
    }

    @Override
    public int hashCode() {
        int result = _triggerId.hashCode();
        result = 31 * result + (_description != null ? _description.hashCode() : 0);
        result = 31 * result + _scriptId.hashCode();
        result = 31 * result + _association.hashCode();
        result = 31 * result + _event.hashCode();
        return result;
    }

    @Override
    public int compareTo(@NotNull final ScriptTrigger other) {
        return toString().compareTo(other.toString());
    }

    private static final long serialVersionUID = -2222671438700366388L;
    private static final Logger _log = LoggerFactory.getLogger(ScriptTrigger.class);

    private String _triggerId;
    private String _description;
    private String _scriptId;
    private String _association;
    private Event _event;
}
