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
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
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
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "disabled"}),
        @UniqueConstraint(columnNames = {"scriptId", "association", "event", "disabled"})})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class ScriptTrigger extends AbstractHibernateEntity implements Comparable<ScriptTrigger> {

    public ScriptTrigger() {
        _log.debug("Creating a default ScriptTrigger object.");
    }

    public ScriptTrigger(final String name, final String description, final String scriptId, final String association, final String event) {
        setName(name);
        setDescription(description);
        setScriptId(scriptId);
        setAssociation(association);
        setEvent(event);
        if (_log.isDebugEnabled()) {
            _log.debug("Creating a ScriptTrigger object with the values: {}", toString());
        }
    }

    @Column(nullable = false)
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    @Column(nullable = false)
    public String getScriptId() {
        return _scriptId;
    }

    public void setScriptId(String scriptId) {
        _scriptId = scriptId;
    }

    @Column(nullable = false)
    public String getAssociation() {
        return _association;
    }

    public void setAssociation(String association) {
        _association = association;
    }

    @Column(nullable = false)
    public String getEvent() {
        return _event;
    }

    public void setEvent(String event) {
        _event = event;
    }

    @Override
    public String toString() {
        return "ScriptTrigger{" +
                "name='" + _name + '\'' +
                ", description='" + _description + '\'' +
                ", scriptId='" + _scriptId + '\'' +
                ", association='" + _association + '\'' +
                ", event='" + _event + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
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
                _name.equals(trigger._name) &&
                _scriptId.equals(trigger._scriptId);
    }

    @Override
    public int hashCode() {
        int result = _name.hashCode();
        result = 31 * result + (_description != null ? _description.hashCode() : 0);
        result = 31 * result + _scriptId.hashCode();
        result = 31 * result + _association.hashCode();
        result = 31 * result + _event.hashCode();
        return result;
    }

    @Override
    public int compareTo(final ScriptTrigger other) {
        return toString().compareTo(other.toString());
    }

    private static final long serialVersionUID = -2222671438700366388L;
    private static final Logger _log = LoggerFactory.getLogger(ScriptTrigger.class);

    private String _name;
    private String _description;
    private String _scriptId;
    private String _association;
    private String _event;
}
