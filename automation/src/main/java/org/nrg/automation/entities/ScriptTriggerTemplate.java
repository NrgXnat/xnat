/*
 * automation: org.nrg.automation.entities.ScriptTriggerTemplate
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.entities;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Set;

/**
 * ScriptTriggerTemplate class.
 *
 * @author Rick Herrick
 */
@Auditable
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"templateId", "disabled"}))
@Cacheable
@Slf4j
public class ScriptTriggerTemplate extends AbstractHibernateEntity {

    @SuppressWarnings("unused")
    public ScriptTriggerTemplate() {
        log.debug("Creating a default ScriptTriggerTemplate object.");
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
        log.debug("Creating a ScriptTriggerTemplate using values: {}", this);
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
     *
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
    public boolean equals(final Object object) {
        if (!super.equals(object)) {
            return false;
        }
        ScriptTriggerTemplate template = (ScriptTriggerTemplate) object;
        return StringUtils.equals(_templateId, template.getTemplateId()) &&
               StringUtils.equals(_description, template.getDescription()) &&
               SetUtils.isEqualSet(_associatedEntities, template.getAssociatedEntities()) &&
               SetUtils.isEqualSet(_triggers, template.getTriggers());
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

    private String             _templateId;
    private String             _description;
    private Set<ScriptTrigger> _triggers;
    private Set<String>        _associatedEntities;
}
