package org.nrg.automation.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.automation.services.ScriptProperty;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Properties;

/**
 * Script class.
 *
 * @author Rick Herrick
 */
@Auditable
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"scriptId", "disabled"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class Script extends AbstractHibernateEntity {

    public Script() {
        if (_log.isDebugEnabled()) {
            _log.debug("Creating default Script object");
        }
    }

    public Script(final String scriptId, final String description, final String language, final String languageVersion, final String content) {
        if (_log.isDebugEnabled()) {
            _log.debug("Creating Script object with parameters:\n * Script ID: " + scriptId + "\n * Description: " + description + "\n * Language: " + language + "\n * Language version: " + languageVersion);
        }
        setScriptId(scriptId);
        setDescription(description);
        setLanguage(language);
        setLanguageVersion(languageVersion);
        setContent(content);
    }

    public String getScriptId() {
        return _scriptId;
    }

    public void setScriptId(final String scriptId) {
        _scriptId = scriptId;
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

    public String getLanguageVersion() {
        return _languageVersion;
    }

    public void setLanguageVersion(final String languageVersion) {
        _languageVersion = languageVersion;
    }

    @Column(columnDefinition = "TEXT")
    public String getContent() {
        return _content;
    }

    public void setContent(final String content) {
        _content = content;
    }

    @Transient
    public Properties getAsProperties() {
        final Properties properties = new Properties();
        properties.setProperty(ScriptProperty.ScriptId.key(), _scriptId);
        properties.setProperty(ScriptProperty.Description.key(), _description);
        properties.setProperty(ScriptProperty.Language.key(), _language);
        properties.setProperty(ScriptProperty.LanguageVersion.key(), _languageVersion);
        properties.setProperty(ScriptProperty.Script.key(), _content);
        return properties;
    }

    private static final Logger _log = LoggerFactory.getLogger(Script.class);

    private String _scriptId;
    private String _description;
    private String _language;
    private String _languageVersion;
    private String _content;

}
