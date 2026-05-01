/*
 * automation: org.nrg.automation.entities.Script
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.entities;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.Audited;
import org.nrg.automation.services.ScriptProperty;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Properties;

/**
 * Script class.
 *
 * @author Rick Herrick
 */
@Audited
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"scriptId", "disabled"}))
@Cacheable
@Slf4j
public class Script extends AbstractHibernateEntity {
    private static final long serialVersionUID = 5785174830912337660L;

    public Script() {
        log.debug("Creating default Script object");
    }

    public Script(final String scriptId, final String scriptLabel, final String description, final String language, final String languageVersion, final String content) {
        log.debug("Creating Script object with parameters:\n * Script ID: {}\n * Script Label: \"{}\"\n * Description: {}\n * Language: {}\n * Language version: {}", scriptId, scriptLabel, description, language, languageVersion);
        setScriptId(scriptId);
        setScriptLabel(scriptLabel);
        setDescription(description);
        setLanguage(language);
        setContent(content);
    }

    public String getScriptId() {
        return _scriptId;
    }

    public void setScriptId(final String scriptId) {
        _scriptId = scriptId;
    }

    public String getScriptLabel() {
        return _scriptLabel;
    }

    public void setScriptLabel(final String scriptLabel) {
        _scriptLabel = scriptLabel;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(final String description) {
        _description = description;
    }

    public String getLanguage() {
        return _language;
    }

    public void setLanguage(final String language) {
        _language = language;
    }

    @Column(columnDefinition = "TEXT")
    public String getContent() {
        return _content;
    }

    public void setContent(final String content) {
        _content = content;
    }

    public Properties toProperties() {
        final Properties properties = new Properties();
        properties.setProperty(ScriptProperty.ScriptId.key(), _scriptId);
        properties.setProperty(ScriptProperty.ScriptLabel.key(), _scriptLabel);
        properties.setProperty(ScriptProperty.Description.key(), _description);
        properties.setProperty(ScriptProperty.Language.key(), _language);
        properties.setProperty(ScriptProperty.Script.key(), _content);
        return properties;
    }

    private String _scriptId;
    private String _scriptLabel;
    private String _description;
    private String _language;
    private String _content;
}
