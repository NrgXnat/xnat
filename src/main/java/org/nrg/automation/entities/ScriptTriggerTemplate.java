/*
 * org.nrg.automation.entities.ScriptTriggerTemplate
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Set;

/**
 * ScriptTriggerTemplate class.
 *
 * @author Rick Herrick
 */
@Auditable
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"templateId", "disabled"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class ScriptTriggerTemplate extends AbstractHibernateEntity {

    @SuppressWarnings("unused")
    public ScriptTriggerTemplate() {
        _log.debug("Creating a default ScriptTriggerTemplate object.");
    }

    @SuppressWarnings("unused")
    public ScriptTriggerTemplate(final String templateId, final String description) {
        this(templateId, description, null, null);
    }

    @SuppressWarnings("unused")
    public ScriptTriggerTemplate(final String templateId, final String description, final Set<ScriptTrigger> triggers) {
        this(templateId, description, triggers, null);
    }

    public ScriptTriggerTemplate(final String templateId, final String description, final Set<ScriptTrigger> triggers, final Set<String> associatedEntities) {
        setTemplateId(templateId);
        setDescription(description);
        setTriggers(triggers);
        setAssociatedEntities(associatedEntities);
        if (_log.isDebugEnabled()) {
            _log.debug("Creating a ScriptTriggerTemplate using values: {}", toString());
        }
    }

    public String getTemplateId() {
        return _templateId;
    }

    public void setTemplateId(String templateId) {
        _templateId = templateId;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    @ManyToMany(targetEntity = ScriptTrigger.class, fetch = FetchType.EAGER)
    public Set<ScriptTrigger> getTriggers() {
        return _triggers;
    }

    public void setTriggers(final Set<ScriptTrigger> triggers) {
        _triggers = triggers;
    }

    /**
     * For the current iteration of this API, associated entities are always the project data info attribute for an XNAT
     * project.
     * @return A set of project IDs in the form of the project data info ID.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    public Set<String> getAssociatedEntities() {
        return _associatedEntities;
    }

    public void setAssociatedEntities(final Set<String> associatedEntities) {
        _associatedEntities = associatedEntities;
    }

    @Override
    public String toString() {
        return "ScriptTriggerTemplate{" +
                "templateId='" + _templateId + '\'' +
                ", description='" + _description + '\'' +
                ", triggers=" + _triggers +
                ", associatedEntities=" + _associatedEntities +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScriptTriggerTemplate)) return false;

        ScriptTriggerTemplate template = (ScriptTriggerTemplate) o;

        return _templateId.equals(template._templateId) &&
                !(_associatedEntities != null ? !_associatedEntities.equals(template._associatedEntities) : template._associatedEntities != null) &&
                !(_description != null ? !_description.equals(template._description) : template._description != null) &&
                !(_triggers != null ? !_triggers.equals(template._triggers) : template._triggers != null);
    }

    @Override
    public int hashCode() {
        int result = _templateId.hashCode();
        result = 31 * result + (_description != null ? _description.hashCode() : 0);
        result = 31 * result + (_triggers != null ? _triggers.hashCode() : 0);
        result = 31 * result + (_associatedEntities != null ? _associatedEntities.hashCode() : 0);
        return result;
    }

    private static final long serialVersionUID = -6493849436022689689L;
    private static final Logger _log = LoggerFactory.getLogger(ScriptTriggerTemplate.class);

    private String _templateId;
    private String _description;
    private Set<ScriptTrigger> _triggers;
    private Set<String> _associatedEntities;
}
