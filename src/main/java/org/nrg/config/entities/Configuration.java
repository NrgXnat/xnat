/*
 * config: org.nrg.config.entities.Configuration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.entities;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

import javax.persistence.*;
import java.util.Objects;
import java.util.Properties;

@SuppressWarnings("deprecation")
@Auditable
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class Configuration extends AbstractHibernateEntity {
    public static final String ENABLED_STRING  = "enabled";
    public static final String DISABLED_STRING = "disabled";

    @Transient
    public String getContents() {
        if (configData != null && StringUtils.equals(status, ENABLED_STRING)) {
            return configData.getContents();
        }
        return null;
    }

    public String getXnatUser() {
        return xnatUser;
    }

    public void setXnatUser(String xnatUser) {
        this.xnatUser = xnatUser;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Deprecated
    public Long getProject() {
        return project;
    }

    @Deprecated
    public void setProject(Long project) {
        this.project = project;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(final Scope scope) {
        this.scope = scope;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    public ConfigurationData getConfigData() {
        return configData;
    }

    public void setConfigData(ConfigurationData data) {
        this.configData = data;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Column(columnDefinition = "boolean default false")
    public boolean isUnversioned() {
        return unversioned;
    }

    public void setUnversioned(final boolean unversioned) {
        this.unversioned = unversioned;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Configuration)) {
            return false;
        }

        final Configuration that = (Configuration) o;

        return getVersion() == that.getVersion() &&
               isUnversioned() == that.isUnversioned() &&
               getScope() == that.getScope() &&
               !(getEntityId() != null ? !getEntityId().equals(that.getEntityId()) : that.getEntityId() != null) &&
               getTool().equals(that.getTool()) &&
               getPath().equals(that.getPath()) &&
               !(getConfigData() != null ? !getConfigData().equals(that.getConfigData()) : that.getConfigData() != null) &&
               !(getXnatUser() != null ? !getXnatUser().equals(that.getXnatUser()) : that.getXnatUser() != null) &&
               !(getReason() != null ? !getReason().equals(that.getReason()) : that.getReason() != null) &&
               !(getStatus() != null ? !getStatus().equals(that.getStatus()) : that.getStatus() != null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), project, scope, entityId, tool, path, configData, xnatUser, reason, status, version, unversioned);
    }

    @SuppressWarnings("unused")
    public Properties asProperties() {
        final Properties properties = new Properties();
        setNonblankProperty(properties, "xnatUser", xnatUser);
        setNonblankProperty(properties, "project", project);
        setNonblankProperty(properties, "scope", scope);
        setNonblankProperty(properties, "entityId", entityId);
        setNonblankProperty(properties, "tool", tool);
        setNonblankProperty(properties, "path", path);
        setNonblankProperty(properties, "reason", reason);
        setNonblankProperty(properties, "status", status);
        setNonblankProperty(properties, "version", version);
        setNonblankProperty(properties, "unversioned", unversioned);
        setNonblankProperty(properties, "contents", configData.getContents());
        return properties;
    }

    private void setNonblankProperty(final Properties properties, final String key, final Object value) {
        if (value != null) {
            final String valueString = value.toString();
            if (StringUtils.isNotBlank(valueString)) {
                properties.setProperty(key, valueString.trim());
            }
        }
    }

    private Long              project;
    private Scope             scope;
    private String            entityId;
    private String            tool;
    private String            path;
    private ConfigurationData configData;
    private String            xnatUser;
    private String            reason;
    private String            status;
    private int               version;
    private boolean           unversioned;
}
