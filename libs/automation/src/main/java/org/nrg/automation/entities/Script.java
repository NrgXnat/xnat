/*
 * automation: org.nrg.automation.entities.Script
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.nrg.automation.services.ScriptProperty;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class Script extends AbstractHibernateEntity {

    public Script() {
        _log.debug("Creating default Script object");
    }

    public Script(final String scriptId, final String scriptLabel, final String description, final String language, final String languageVersion, final String content) {
        _log.debug("Creating Script object with parameters:\n * Script ID: {}\n * Script Label: \" + scriptLabel + \"\n * Description: {}\n * Language: {}\n * Language version: ", scriptId, description, language, languageVersion);
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

    private static final Logger _log = LoggerFactory.getLogger(Script.class);

    private String _scriptId;
    private String _scriptLabel;
    private String _description;
    private String _language;
    private String _content;
}
